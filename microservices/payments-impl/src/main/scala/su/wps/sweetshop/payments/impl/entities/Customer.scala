package su.wps.sweetshop.payments.impl.entities

import cats.tagless.{Derive, FunctorK}
import su.wps.sweetshop.payments.impl.models.domain.es.CustomerInfo

trait Customer[F[_]] {
  def create(email: String, password: String): F[Unit]

  def register: F[Unit]

  def info: F[CustomerInfo]
}

object Customer {
  implicit def functorK: FunctorK[Customer] = Derive.functorK
}
