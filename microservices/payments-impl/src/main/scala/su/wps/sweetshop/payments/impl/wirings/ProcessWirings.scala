package su.wps.sweetshop.payments.impl.wirings

import cats.effect.{Concurrent, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.{Monad, Parallel, ~>}
import doobie._
import fs2.Stream
import su.wps.sweetshop.payments.impl.processes._
import tofu.lift.Lift
import tofu.logging.Logs

final case class ProcessWirings[F[_]: Parallel](processes: List[Stream[F, Unit]])(
  implicit F: Concurrent[F]
) {
  val launchProcesses: Resource[F, List[Unit]] =
    processes.parTraverse(runProcess)

  private def runProcess(stream: Stream[F, Unit]): Resource[F, Unit] =
    Resource.make(F.start(stream.compile.drain))(_.cancel).void
}

object ProcessWirings {
  def create[I[_]: Monad, F[_]: Concurrent: Parallel, DB[_]](
    dbWirings: DbWirings[F, DB],
    kafkaWirings: KafkaWirings[F],
    serviceWirings: ServiceWirings[F],
    entityWirings: EntityWirings[F]
  )(implicit toConnectionIO: DB ~> ConnectionIO, logs: Logs[I, F]): I[ProcessWirings[F]] = {
    import dbWirings._
    import entityWirings._
    import kafkaWirings._
    import serviceWirings._

    implicit val lift: Lift[DB, F] = Lift.byFunK(toConnectionIO.andThen(readXa.trans))

    for {
      cardLinkViewProjection <- CardLinkViewProjection.create[I, F, DB](
        cardLinkEventsStream,
        cardLinkViewRepo
      )
      customerViewProjection <- CustomerViewProjection.create[I, F, DB](
        customerEventsStream,
        customerViewRepo
      )
      customerCreationProcess <- CustomerCreationProcess
        .create[I, F](userCreatedEventsStream, paytureGateway, customers)
      paymentAuthorizationProcess <- PaymentAuthorizationProcess.create[I, F, DB](
        paymentCreatedEventsStream,
        paytureGateway,
        cardLinkViewRepo,
        customerViewRepo,
        payments
      )
      paymentChargeProcess <- PaymentChargeProcess
        .create[I, F](chargeRequiredEventsStream, paytureGateway, payments)
      processes = List(
        cardLinkViewProjection.run,
        customerViewProjection.run,
        customerCreationProcess.run,
        paymentAuthorizationProcess.run,
        paymentChargeProcess.run
      )
    } yield ProcessWirings(processes)
  }
}
