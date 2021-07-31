package su.wps.sweetshop.payments.impl.models.domain.es

import aecor.data.Folded
import aecor.data.Folded.syntax._
import su.wps.sweetshop.payments.impl.models.domain.CustomerStatus

final case class CustomerState(email: String, password: String, status: CustomerStatus) {
  def applyEvent(event: CustomerEvent): Folded[CustomerState] = event match {
    case _: CustomerCreated => impossible
    case CustomerRegistered => copy(status = CustomerStatus.Registered).next
  }
}

object CustomerState {
  def fromEvent(event: CustomerEvent): Folded[CustomerState] = event match {
    case e: CustomerCreated =>
      CustomerState(e.email, e.password, CustomerStatus.New).next
    case _ => impossible
  }
}
