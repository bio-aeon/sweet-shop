package su.wps.sweetshop.payments.impl.processes

import aecor.data.{Committable, ConsumerId}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Defer, Functor, Monad}
import fs2.Stream
import io.circe.Error
import su.wps.sweetshop.auth.api.models.UserCreated
import su.wps.sweetshop.payments.impl.gateways.PaytureGateway
import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.es.Customers
import su.wps.sweetshop.utils.syntax.handle._
import su.wps.sweetshop.utils.syntax.uuid._
import tofu.Catches
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

trait CustomerCreationProcess[F[_]] {
  def run: Stream[F, Unit]
}

object CustomerCreationProcess {
  val consumerId: ConsumerId = ConsumerId("CustomerCreationProcess")

  def create[I[_]: Functor, F[_]: Monad: Defer: Catches](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, UserCreated]]],
    paytureGateway: PaytureGateway[F],
    customers: Customers[F]
  )(implicit logs: Logs[I, F]): I[CustomerCreationProcess[F]] =
    logs
      .forService[CustomerCreationProcess[F]]
      .map(implicit log => new Impl[F](source, paytureGateway, customers))

  final private class Impl[F[_]: Monad: Defer: Catches: Logging](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, UserCreated]]],
    paytureGateway: PaytureGateway[F],
    customers: Customers[F]
  ) extends CustomerCreationProcess[F] {

    def run: Stream[F, Unit] =
      source(consumerId)
        .evalMap(c => handleEvent(c.value).handled >> c.commit)

    private[processes] def handleEvent(event: Either[Error, UserCreated]) =
      event match {
        case Right(e) =>
          val login = e.contact.value

          for {
            password <- randomUUIDString[F]
            id = CustomerId(e.id)
            _ <- customers(id).create(login, password) >>= {
              case Right(_) => info"Customer created successfully for user ${e.id}"
              case Left(r) =>
                error"Customer creation process got unexpected rejection for user ${e.id}. Reason: ${r.toString}"
            }
            _ <- paytureGateway.registerCustomer(login, password)
            _ <- customers(id).register >>= {
              case Right(_) => info"Customer registered successfully for user ${e.id}"
              case Left(r) =>
                error"Customer registration process got unexpected rejection for ${id.toString}: ${r.toString}"
            }

          } yield ()
        case Left(err) => errorCause"Failed to decode user created event." (err)
      }
  }
}
