package su.wps.sweetshop.utils.syntax

import cats.syntax.applicative._
import cats.syntax.functor._
import cats.{Applicative, Defer}

import java.util.UUID

trait UUIDSyntax {
  def randomUUID[F[_]: Applicative](implicit F: Defer[F]): F[UUID] =
    F.defer(UUID.randomUUID().pure[F])

  def randomUUIDString[F[_]: Applicative: Defer]: F[String] = randomUUID[F].map(_.toString)
}
