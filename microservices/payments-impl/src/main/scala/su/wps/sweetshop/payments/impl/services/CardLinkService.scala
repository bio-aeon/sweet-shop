package su.wps.sweetshop.payments.impl.services

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Defer, Functor, Monad}
import su.wps.sweetshop.payments.api.models.InitCardLinkRequest
import su.wps.sweetshop.payments.impl.gateways.PaytureGateway
import su.wps.sweetshop.payments.impl.models.domain.es.{CardLinks, CustomerInfo, Customers}
import su.wps.sweetshop.payments.impl.models.{CardLinkId, CustomerId}
import su.wps.sweetshop.payments.impl.repositories.CardLinkViewRepository
import su.wps.sweetshop.utils.syntax.uuid._
import tofu.Throws
import tofu.lift.Lift
import tofu.logging.Logs
import tofu.syntax.lift._

trait CardLinkService[F[_]] {
  def initCardLink(request: InitCardLinkRequest): F[String]

  def userHasCardLinks(userId: Int): F[Boolean]
}

object CardLinkService {
  def create[I[_]: Functor, F[_]: Defer: Monad: Throws, DB[_]: Lift[*[_], F]](
    cardLinkViewRepo: CardLinkViewRepository[DB],
    paytureGateway: PaytureGateway[F],
    customers: Customers[F],
    cardLinks: CardLinks[F]
  )(implicit logs: Logs[I, F]): I[CardLinkService[F]] =
    logs
      .forService[CardLinkService[F]]
      .map(implicit log => new Impl[F, DB](cardLinkViewRepo, paytureGateway, customers, cardLinks))

  private final class Impl[F[_]: Defer, DB[_]: Lift[*[_], F]](
    cardLinkViewRepo: CardLinkViewRepository[DB],
    paytureGateway: PaytureGateway[F],
    customers: Customers[F],
    cardLinks: CardLinks[F]
  )(implicit F: Monad[F], R: Throws[F])
      extends CardLinkService[F] {

    def initCardLink(request: InitCardLinkRequest): F[String] =
      for {
        customer <- customers(CustomerId(request.userId)).info >>= (_.fold(
          r => R.raise[CustomerInfo](new Exception(r.toString)),
          F.pure
        ))
        id <- randomUUIDString
        initRes <- paytureGateway.initCardLink(
          id,
          customer.email,
          customer.password,
          request.ip,
          request.template,
          request.returnUrl
        )
        _ <- cardLinks(CardLinkId(id)).create(request.userId) >>= (_.fold(
          r => R.raise[Unit](new Exception(r.toString)),
          F.pure
        ))
      } yield initRes.SessionId

    def userHasCardLinks(userId: Int): F[Boolean] =
      cardLinkViewRepo.findCountByUserId(userId).lift[F].map(_ > 0)
  }
}
