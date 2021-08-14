package su.wps.sweetshop.auth.impl.services

import cats.data.OptionT
import cats.effect.Clock
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.option._
import cats.{Functor, Monad}
import io.circe.syntax._
import mouse.anyf._
import pdi.jwt.{JwtAlgorithm, JwtCirce}
import su.wps.sweetshop.auth.impl.models.implicits.results._
import su.wps.sweetshop.auth.api.models.{JwtPayload, TokenResult}
import su.wps.sweetshop.auth.impl.config.AuthConfig
import su.wps.sweetshop.auth.impl.models.{AuthUser, UserId}
import su.wps.sweetshop.auth.impl.repositories.{AuthUserRepository, SMSCodeRepository}
import su.wps.sweetshop.auth.impl.models.errors.{AppErr, IncorrectSMSCode, SMSCodeNotFound}
import su.wps.sweetshop.utils.syntax.clock._
import tofu.Raise
import tofu.doobie.transactor.Txr
import tofu.logging.{Logging, Logs}
import tofu.syntax.raise._

import java.time.ZonedDateTime

trait AuthService[F[_]] {
  def createTokenBySMSCode(phone: String, code: String): F[TokenResult]
}

object AuthService {
  def create[I[_]: Functor, F[_]: Clock: Monad: Raise[*[_], AppErr], DB[_]: Monad](
    config: AuthConfig,
    smsCodeRepo: SMSCodeRepository[DB],
    authUserRepo: AuthUserRepository[DB],
    xa: Txr.Aux[F, DB]
  )(implicit logs: Logs[I, F]): I[AuthService[F]] =
    logs
      .forService[AuthService[F]]
      .map(implicit log => new Impl[F, DB](config, smsCodeRepo, authUserRepo, xa))

  private final class Impl[F[_]: Clock: Logging, DB[_]: Monad](
    settings: AuthConfig,
    smsCodeRepo: SMSCodeRepository[DB],
    authUserRepo: AuthUserRepository[DB],
    xa: Txr.Aux[F, DB]
  )(implicit F: Monad[F], R: Raise[F, AppErr])
      extends AuthService[F] {
    def createTokenBySMSCode(phone: String, code: String): F[TokenResult] =
      for {
        smsCode <- smsCodeRepo.findByPhoneAndCode(phone, code).thrushK(xa.trans) >>= (_.orRaise[F](
          SMSCodeNotFound(code)
        ))
        now <- Clock[F].realZonedDt
        _ <- F.unlessA(smsCode.phone == phone && smsCode.createdAt.isAfter(now.minusMinutes(10)))(
          R.raise(IncorrectSMSCode(code))
        )
        userId <- getOrCreateUser(phone, now)
      } yield createJWT(userId, now)

    private[services] def getOrCreateUser(phone: String, now: ZonedDateTime) =
      OptionT(authUserRepo.findByPhone(phone).thrushK(xa.trans)).map(_.pk).getOrElseF {
        authUserRepo
          .insert(AuthUser(phone = phone.some, createdAt = now))
          .thrushK(xa.trans)
          .map(_.pk)
      }

    private[services] def createJWT(userId: UserId, now: ZonedDateTime): TokenResult = {
      val payload = JwtPayload(userId.value, now)
      TokenResult(JwtCirce.encode(payload.asJson, settings.secretKey, JwtAlgorithm.HS256))
    }
  }
}
