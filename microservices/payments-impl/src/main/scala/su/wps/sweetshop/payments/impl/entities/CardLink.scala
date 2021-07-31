package su.wps.sweetshop.payments.impl.entities

import cats.tagless.{Derive, FunctorK}
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus

trait CardLink[F[_]] {
  def create(userId: Int): F[Unit]

  def activate(maskedPan: String,
               extCardId: String,
               cardHolder: String,
               expDate: String,
               linkedAt: String): F[Unit]

  def status: F[CardLinkStatus]
}

object CardLink {
  implicit def functorK: FunctorK[CardLink] = Derive.functorK
}
