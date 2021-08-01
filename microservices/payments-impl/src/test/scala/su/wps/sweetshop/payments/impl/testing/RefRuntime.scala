package su.wps.sweetshop.payments.impl.testing

import aecor.data.{ActionT, EitherK, EventsourcedBehavior}
import aecor.runtime.Eventsourced.Entities
import cats.Monad
import cats.arrow.FunctionK
import cats.data.Chain
import cats.effect.concurrent.Ref
import cats.effect.{IO, LiftIO, Sync}
import cats.implicits._
import cats.mtl.MonadState
import cats.tagless.FunctorK
import fs2.concurrent.InspectableQueue

object RefRuntime {
  type ActionRunner[F[_], S, E] = FunctionK[ActionT[F, S, E, *], F]

  private def loop[F[_]: Monad, A](x: F[(Boolean, A)]): F[A] = x.flatMap {
    case (succeeded, result) =>
      if (succeeded) {
        result.pure[F]
      } else {
        loop(x)
      }
  }

  final case class InnerState[K, E](store: Ref[IO, Map[K, Chain[E]]],
                                    queue: InspectableQueue[IO, (K, E)])

  final class Runner[F[_], K] {
    def apply[M[_[_]]: FunctorK, S, E, R](
      behaviour: EventsourcedBehavior[EitherK[M, R, *[_]], F, Option[S], E]
    )(implicit F: Sync[F],
      Flift: LiftIO[F],
      MS: MonadState[F, InnerState[K, E]]): Entities.Rejectable[K, M, F, R] = {

      def actionRunner(key: K): ActionRunner[F, Option[S], E] =
        new FunctionK[ActionT[F, Option[S], E, *], F] {
          def apply[A](fa: ActionT[F, Option[S], E, A]): F[A] = MS.get.flatMap { state =>
            val tryRun =
              Flift.liftIO(state.store.access).flatMap {
                case (store, setter) =>
                  val currentEvents = store.getOrElse(key, Chain.empty)
                  for {
                    currentState <- currentEvents
                      .foldM(behaviour.fold.initial)(behaviour.fold.reduce)
                      .fold(F.raiseError[Option[S]](new RuntimeException("Impossible fold")))(
                        F.pure
                      )
                    actionResult <- fa
                      .run(behaviour.fold.init(currentState))
                      .flatMap(
                        _.fold(
                          F.raiseError[(Chain[E], A)](new RuntimeException("Impossible fold"))
                        )(F.pure)
                      )

                    actionResultEvents = actionResult._1
                    updatedStore = store.updated(
                      key,
                      store
                        .get(key)
                        .map(_ ++ actionResultEvents)
                        .getOrElse(actionResultEvents)
                    )
                    succeed <- Flift.liftIO(setter(updatedStore))
                    _ <- if (succeed) {
                      Flift.liftIO(actionResultEvents.traverse(e => state.queue.enqueue1((key, e))))
                    } else ().pure[F]
                  } yield (succeed, actionResult._2)

              }
            loop(tryRun)
          }
        }

      Entities.rejectable((key: K) => behaviour.actions.mapK(actionRunner(key)))

    }
  }

  def apply[F[_], K]: Runner[F, K] = new Runner[F, K]

}
