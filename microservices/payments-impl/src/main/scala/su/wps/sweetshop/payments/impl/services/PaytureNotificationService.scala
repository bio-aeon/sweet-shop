package su.wps.sweetshop.payments.impl.services

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, Monad}
import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.es.CardLinks
import su.wps.sweetshop.payments.impl.models.payture.SuccessCardLinkResult
import su.wps.sweetshop.payments.impl.security.Crypto
import su.wps.sweetshop.utils.syntax.product._
import tofu.Throws
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.syntax.raise._

import scala.util.matching.Regex

trait PaytureNotificationService[F[_]] {
  def handle(payload: String): F[Unit]
}

object PaytureNotificationService {
  private val payloadPattern: Regex = "^DATA=(.+)$".r

  def create[I[_]: Functor, F[_]: Monad: Throws](
    cryptKey: String,
    crypto: Crypto[F],
    cardLinks: CardLinks[F]
  )(implicit logs: Logs[I, F]): I[PaytureNotificationService[F]] =
    logs
      .forService[PaytureNotificationService[F]]
      .map(implicit log => new Impl[F](cryptKey, crypto, cardLinks))

  private final class Impl[F[_]: Monad: Logging](cryptKey: String,
                                                 crypto: Crypto[F],
                                                 cardLinks: CardLinks[F])(implicit R: Throws[F])
      extends PaytureNotificationService[F] {
    def handle(payload: String): F[Unit] = {
      val payloadPattern(encryptedData) = payload

      crypto.decodeAes(encryptedData, cryptKey) >>= { data =>
        val params = parseParams(data)
        params.get("Notification") match {
          case Some("CustomerAddSuccess") =>
            for {
              res <- to[SuccessCardLinkResult].from(params).orRaise(new Exception("Parsing error"))
              _ <- cardLinks(CardLinkId(res.OrderId)).activate(
                res.CardNumber,
                res.CardId,
                res.CardHolder,
                res.ExpDate,
                res.TransactionDate
              ) >>= (_.fold(
                r =>
                  error"Card activation process got unexpected rejection for ${res.OrderId} (${res.CardNumber}): ${r.toString}" *> R
                    .raise[Unit](new Exception(r.toString)),
                _ => info"Card ${res.OrderId} (${res.CardNumber}) activated successfully"
              ))
            } yield ()
          case _ =>
            val errMsg = "Unknown notification type."
            error"$errMsg" *> R.raise(new Exception(errMsg))
        }
      }
    }

    private[services] def parseParams(paramsStr: String) =
      paramsStr
        .split(";")
        .map(_.split("=", 2))
        .map {
          case Array(key, value) => (key, value)
        }
        .toMap
  }
}
