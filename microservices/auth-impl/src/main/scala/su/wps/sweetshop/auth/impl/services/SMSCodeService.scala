package su.wps.sweetshop.auth.impl.services

trait SMSCodeService[F[_]] {
  def createSMSCode(phone: String): F[Either[String, Unit]]
}
