package su.wps.sweetshop.payments.impl.wirings

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import doobie._
import su.wps.sweetshop.payments.impl.gateways.PaytureGateway
import su.wps.sweetshop.payments.impl.services.{
  CardLinkService,
  PaymentService,
  PaytureNotificationService
}
import tofu.lift.Lift
import tofu.logging.Logs

final case class ServiceWirings[F[_]](paytureGateway: PaytureGateway[F],
                                      cardLinkService: CardLinkService[F],
                                      paymentService: PaymentService[F],
                                      paytureNotificationService: PaytureNotificationService[F])

object ServiceWirings {

  def create[I[_]: Monad, F[_]: Sync, DB[_]](
    commonWirings: CommonWirings[F],
    dbWirings: DbWirings[F, DB],
    entityWirings: EntityWirings[F]
  )(implicit toConnectionIO: DB ~> ConnectionIO, logs: Logs[I, F]) = {
    import commonWirings._
    import dbWirings._
    import entityWirings._

    implicit val lift: Lift[DB, F] = Lift.byFunK(toConnectionIO.andThen(readXa.trans))

    for {
      paytureGateway <- PaytureGateway.create[I, F](appConfig.payture, sttpBackend)
      cardLinkService <- CardLinkService.create[I, F, DB](
        cardLinkViewRepo,
        paytureGateway,
        customers,
        cardLinks
      )
      paymentService <- PaymentService.create[I, F](customers, payments)
      paytureNotificationService <- PaytureNotificationService
        .create[I, F](appConfig.payture.cryptKey, crypto, cardLinks)
    } yield
      ServiceWirings(paytureGateway, cardLinkService, paymentService, paytureNotificationService)
  }
}
