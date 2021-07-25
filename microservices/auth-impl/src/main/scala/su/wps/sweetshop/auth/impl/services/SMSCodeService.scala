package su.wps.sweetshop.auth.impl.services

import java.time.ZonedDateTime
import cats.effect.Clock
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.apply._
import cats.{Functor, Monad}
import su.wps.sweetshop.auth.impl.gateways.SMSGateway
import su.wps.sweetshop.auth.impl.models.{SMSCode, TemplateVar}
import su.wps.sweetshop.auth.impl.repositories.SMSCodeRepository
import su.wps.sweetshop.utils.syntax.clock._
import tofu.doobie.transactor.Txr
import mouse.anyf._
import su.wps.sweetshop.auth.impl.models.errors.{AppErr, TooFrequentSMSCodeGeneration}
import tofu.Raise
import tofu.logging.{Logging, Logs}

import scala.io.Source.fromInputStream
import scala.util.Random

trait SMSCodeService[F[_]] {
  def createSMSCode(phone: String): F[Unit]
}

object SMSCodeService {
  def create[I[_]: Functor, F[_]: Clock: Monad: Raise[*[_], AppErr], DB[_]](
    repo: SMSCodeRepository[DB],
    smsGateway: SMSGateway[F],
    xa: Txr.Aux[F, DB]
  )(implicit logs: Logs[I, F]): I[SMSCodeService[F]] =
    logs
      .forService[AuthService[F]]
      .map(implicit log => new Impl[F, DB](repo, smsGateway, xa))

  private final class Impl[F[_]: Clock: Monad: Logging, DB[_]](
    repo: SMSCodeRepository[DB],
    smsGateway: SMSGateway[F],
    xa: Txr.Aux[F, DB]
  )(implicit R: Raise[F, AppErr])
      extends SMSCodeService[F] {
    def createSMSCode(phone: String): F[Unit] =
      Clock[F].realZonedDt >>= { now =>
        repo
          .findLastByPhone(phone)
          .thrushK(xa.trans)
          .map {
            _.forall(x => if (x.createdAt.isAfter(now.minusMinutes(2))) false else true)
          }
          .ifM(
            {
              val code = s"${Random.nextInt(9000) + 1000}"
              val template = fromInputStream(
                getClass.getClassLoader
                  .getResourceAsStream("sms/code.txt")
              ).getLines().mkString
              repo.insert(SMSCode(phone, code, ZonedDateTime.now)).thrushK(xa.trans) *> smsGateway
                .send(phone, template, List(TemplateVar("code", code)))
                .void
            },
            R.raise(TooFrequentSMSCodeGeneration)
          )
      }
  }
}
