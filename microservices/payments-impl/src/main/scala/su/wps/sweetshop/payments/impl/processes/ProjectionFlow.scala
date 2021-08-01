package su.wps.sweetshop.payments.impl.processes

import aecor.data.{Committable, EntityEvent}
import cats.Monad
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Pipe
import io.circe.Error
import tofu.Throws
import tofu.logging.Logging
import tofu.syntax.logging._

trait ProjectionFlow[F[_], K, E, S] {
  self: Projection[F, EntityEvent[K, E], S] =>

  def pipe(implicit F: Monad[F],
           R: Throws[F],
           log: Logging[F]): Pipe[F, Committable[F, Either[Error, EntityEvent[K, E]]], Unit] = {
    def foldEvent(event: EntityEvent[K, E], state: Option[S]): F[Option[S]] = {
      val newVersion = applyEvent(state)(event)
      debug"New version [${newVersion.toString}]" >>
        newVersion
          .fold(
            R.raise[Option[S]](
              new IllegalStateException(s"Projection failed for state = [$state], event = [$event]")
            )
          )(_.pure[F])
    }

    def runProjection(event: EntityEvent[K, E]): F[Unit] =
      for {
        (currentVersion, currentState) <- fetchVersionAndState(event)
        _ <- debug"Current ${currentVersion.toString} [${currentState.toString}]"
        _ <- F.whenA(currentVersion.value < event.sequenceNr) {
          foldEvent(event, currentState).flatMap {
            case None => F.unit
            case Some(state) =>
              saveNewVersion(state, currentVersion.next)
          }
        }
      } yield ()

    _.evalTap { c =>
      c.value match {
        case Right(e) => runProjection(e)
        case Left(err) => errorCause"Failed to decode event." (err)
      }
    }.evalMap(_.commit)
  }
}
