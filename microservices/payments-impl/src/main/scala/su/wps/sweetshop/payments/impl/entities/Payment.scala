package su.wps.sweetshop.payments.impl.entities

import cats.tagless.{Derive, FunctorK}
import io.circe.Json
import su.wps.sweetshop.payments.impl.models.domain.PaymentStatus
import su.wps.sweetshop.payments.impl.models.domain.es.PaymentInfo

trait Payment[F[_]] {
  def create(userId: Int, amount: Int, details: Json): F[Unit]

  def authorize: F[Unit]

  def preCharge: F[Unit]

  def charge: F[Unit]

  def fail: F[Unit]

  def info: F[PaymentInfo]

  def status: F[PaymentStatus]
}

object Payment {
  implicit def functorK: FunctorK[Payment] = Derive.functorK
}
