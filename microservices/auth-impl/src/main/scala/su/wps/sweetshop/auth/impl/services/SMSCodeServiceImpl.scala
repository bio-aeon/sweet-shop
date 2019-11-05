package su.wps.sweetshop.auth.impl.services

import java.time.ZonedDateTime

import cats.data.EitherT
import cats.syntax.apply._
import cats.syntax.functor._
import cats.{Monad, ~>}
import su.wps.sweetshop.auth.impl.gateways.SMSGateway
import su.wps.sweetshop.auth.impl.models.{SMSCode, TemplateVar}
import su.wps.sweetshop.auth.impl.repositories.SMSCodeRepository
import su.wps.sweetshop.auth.impl.utils.CatsSupport._

import scala.io.Source.fromInputStream
import scala.util.Random

class SMSCodeServiceImpl[F[_], G[_]: Monad](repo: SMSCodeRepository[F], smsGateway: SMSGateway[G])(
  implicit transform: F ~> G
) extends SMSCodeService[G] {
  def createSMSCode(phone: String): G[Either[String, Unit]] = {
    val now = ZonedDateTime.now
    val allowedF = repo.findLastByPhone(phone).liftT[G].map {
      _.forall(x => if (x.createdAt.isAfter(now.minusMinutes(2))) false else true)
    }

    EitherT
      .liftF(allowedF)
      .flatMap { allowed =>
        if (allowed) {
          val code = s"${Random.nextInt(9000) + 1000}"
          val template = fromInputStream(
            getClass.getClassLoader
              .getResourceAsStream("sms/code.txt")
          ).getLines.mkString
          EitherT.liftF[G, String, Unit](
            repo.insert(SMSCode(phone, code, ZonedDateTime.now)).liftT[G] *> smsGateway
              .send(phone, template, List(TemplateVar("code", code)))
              .void
          )
        } else {
          EitherT.leftT[G, Unit]("Code can be generated no more than once every 2 minutes.")
        }
      }
      .value
  }
}
