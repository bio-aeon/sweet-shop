package su.wps.sweetshop.auth.impl.services

import java.time.ZonedDateTime

import cats.data.EitherT
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, MonadError, ~>}
import io.circe.syntax._
import pdi.jwt.{JwtAlgorithm, JwtCirce}
import su.wps.sweetshop.auth.api.dto.TokenDto
import su.wps.sweetshop.auth.impl.config.AuthConfig
import su.wps.sweetshop.auth.impl.dto.JwtPayloadDto
import su.wps.sweetshop.auth.impl.models.{AuthUser, UserId}
import su.wps.sweetshop.auth.impl.repositories.{AuthUserRepository, SMSCodeRepository}
import su.wps.sweetshop.auth.impl.utils.CatsSupport._

class AuthServiceImpl[F[_], G[_]](
  settings: AuthConfig,
  smsCodeRepo: SMSCodeRepository[F],
  authUserRepo: AuthUserRepository[F]
)(implicit transform: F ~> G, G: MonadError[G, Throwable], F: MonadError[F, Throwable])
    extends AuthService[G] {
  def createTokenBySMSCode(phone: String, code: String): G[Either[String, TokenDto]] =
    (for {
      smsCode <- EitherT.fromOptionF(
        smsCodeRepo.findByPhoneAndCode(phone, code).liftT[G],
        "Code not found."
      )
      now = ZonedDateTime.now
      _ <- EitherT.cond(
        smsCode.phone == phone && smsCode.createdAt.isAfter(now.minusMinutes(10)),
        (),
        "Incorrect or expired code"
      )(Monad[G])
      userId <- EitherT.liftF(getOrCreateUser(phone))
      jwt <- EitherT.rightT[G, String](createJWT(userId, now))
    } yield jwt).value

  private def getOrCreateUser(phone: String) = {
    val now = ZonedDateTime.now
    authUserRepo.findByPhone(phone).liftT[G].flatMap {
      case Some(authUser) =>
        authUser.id.pure[G]
      case _ =>
        val res = for {
          authUser <- authUserRepo
            .insert(AuthUser(UserId(0), phone, now))
        } yield authUser.id
        res.liftT[G]
    }
  }

  private def createJWT(userId: UserId, now: ZonedDateTime): TokenDto = {
    val payload = JwtPayloadDto(userId, now)
    TokenDto(JwtCirce.encode(payload.asJson, settings.secretKey, JwtAlgorithm.HS256))
  }
}
