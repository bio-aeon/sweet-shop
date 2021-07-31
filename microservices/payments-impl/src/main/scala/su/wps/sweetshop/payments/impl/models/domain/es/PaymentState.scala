package su.wps.sweetshop.payments.impl.models.domain.es

import aecor.data.Folded
import aecor.data.Folded.syntax._
import io.circe.Json
import su.wps.sweetshop.payments.impl.models.domain.PaymentStatus

case class PaymentState(userId: Int, amount: Int, details: Json, status: PaymentStatus) {
  def applyEvent(event: PaymentEvent): Folded[PaymentState] = event match {
    case _: PaymentCreated => impossible
    case PaymentAuthorized => copy(status = PaymentStatus.Authorized).next
    case PaymentPreCharged => copy(status = PaymentStatus.PreCharged).next
    case PaymentCharged => copy(status = PaymentStatus.Charged).next
    case PaymentFailed => copy(status = PaymentStatus.Failed).next
  }
}

object PaymentState {
  def fromEvent(event: PaymentEvent): Folded[PaymentState] = event match {
    case e: PaymentCreated =>
      PaymentState(e.userId, e.amount, e.details, PaymentStatus.New).next
    case _ => impossible
  }
}
