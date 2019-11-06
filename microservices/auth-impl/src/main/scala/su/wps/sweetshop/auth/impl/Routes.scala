package su.wps.sweetshop.auth.impl

import cats.effect.Sync
import cats.syntax.flatMap._
import io.circe.Encoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import su.wps.sweetshop.auth.api.dto.TokenDto
import su.wps.sweetshop.auth.api.requests.{CreateSMSCodeRequest, CreateTokenBySMSCodeRequest}
import su.wps.sweetshop.auth.impl.models.ErrorResult
import su.wps.sweetshop.auth.impl.models.ErrorResult.BusinessLogicError
import su.wps.sweetshop.auth.impl.services.{AuthService, SMSCodeService}

final class Routes[F[_]: Sync](smsCodeService: SMSCodeService[F], authService: AuthService[F])
    extends Http4sDsl[F] {
  implicit val tokenDtoEncoder: Encoder[TokenDto] = deriveEncoder

  implicit val createSMSCoderequestDecoder =
    jsonOf[F, CreateSMSCodeRequest](implicitly, deriveDecoder[CreateSMSCodeRequest])

  implicit val createTokenBySMSCodeRequestDecoder =
    jsonOf[F, CreateTokenBySMSCodeRequest](implicitly, deriveDecoder[CreateTokenBySMSCodeRequest])

  def routes = HttpRoutes.of[F] {
    case req @ POST -> Root / "api" / "sms-codes" =>
      req.as[CreateSMSCodeRequest].flatMap { request =>
        smsCodeService.createSMSCode(request.phone).flatMap {
          case Right(x) => Ok(x.asJson)
          case Left(err) => BadRequest((BusinessLogicError(err): ErrorResult).asJson)
        }
      }
    case req @ POST -> Root / "api" / "sms-code-tokens" =>
      req.as[CreateTokenBySMSCodeRequest].flatMap { input =>
        authService.createTokenBySMSCode(input.phone, input.code).flatMap {
          case Right(x) => Ok(x.asJson)
          case Left(err) => BadRequest((BusinessLogicError(err): ErrorResult).asJson)
        }
      }
  }
}
