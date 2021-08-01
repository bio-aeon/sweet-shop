package su.wps.sweetshop.payments.impl.security

object errors {
  sealed trait SecurityErr extends Throwable {
    override def getMessage: String = toString
  }

  final case class DecryptionFailure(msg: String) extends SecurityErr {
    override def toString: String = msg
  }
}
