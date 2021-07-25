package su.wps.sweetshop.utils.syntax

import cats.Functor
import cats.effect.Clock
import cats.syntax.functor._

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.concurrent.duration.MILLISECONDS

trait ClockSyntax {
  final implicit def syntaxClock[F[_]](clock: Clock[F]): ClockOps[F] = new ClockOps(clock)
}

private[syntax] final class ClockOps[F[_]](private val clock: Clock[F]) extends AnyVal {
  def realZonedDt(implicit F: Functor[F]): F[ZonedDateTime] =
    clock
      .realTime(MILLISECONDS)
      .map(Instant.ofEpochMilli)
      .map(ZonedDateTime.ofInstant(_, ZoneId.systemDefault))
}
