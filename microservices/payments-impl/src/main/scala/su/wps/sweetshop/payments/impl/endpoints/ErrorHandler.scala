package su.wps.sweetshop.payments.impl.endpoints

import cats.data.{Kleisli, NonEmptyList, OptionT}
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Applicative, Functor}
import io.circe.Json
import io.circe.syntax._
import mouse.any._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import su.wps.sweetshop.payments.impl.validators.errors.ValidationFailed
import tofu.Handle
import tofu.logging.Logging
import tofu.syntax.handle._
import tofu.syntax.logging._

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

object RoutesHttpErrorHandler {

  def apply[F[_]: Functor: Handle[*[_], E], E <: Throwable](
    routes: HttpRoutes[F]
  )(handler: E => F[Response[F]]): HttpRoutes[F] =
    Kleisli { req =>
      OptionT {
        routes.run(req).value.handleWith[E](e => handler(e).map(Option(_)))
      }
    }
}

object HttpErrorHandler {
  def apply[F[_], E <: Throwable](implicit ev: HttpErrorHandler[F, E]): HttpErrorHandler[F, E] = ev

  def create[F[_]: Functor: Handle[*[_], E], E <: Throwable](
    handler: E => F[Response[F]]
  ): HttpErrorHandler[F, E] =
    (routes: HttpRoutes[F]) => RoutesHttpErrorHandler(routes)(handler)

  def mkResponse[F[_]: Applicative](
    respF: Json => F[Response[F]],
    errorMessage: String,
    errorFields: Option[NonEmptyList[String]] = None
  ): F[Response[F]] =
    respF(
      Json.obj(
        "error" -> Json
          .obj("descr" -> Json.fromString(errorMessage))
          .deepMerge(
            errorFields.fold(Json.obj())(
              ls => Json.obj("errorFields" -> Json.fromValues(ls.toList.map(Json.fromString)))
            )
          )
      )
    )
}

object ValidationHttpErrorHandler {

  def apply[F[_]: Applicative: Handle[*[_], ValidationFailed]](
    implicit
    logging: Logging[F]
  ): HttpErrorHandler[F, ValidationFailed] =
    HttpErrorHandler.create[F, ValidationFailed] { err =>
      val dsl = new Http4sDsl[F] {}
      import dsl._

      val errorMessage =
        err.errors.map(x => x.key -> x.message).toList.toMap.asJson.noSpaces |> (
          x => s"errorFields: $x"
        )
      val errorFields = err.errors.map(_.key)
      error"$errorMessage" *> HttpErrorHandler.mkResponse(
        BadRequest(_),
        "Invalid request parameters",
        errorFields.some
      )
    }
}
