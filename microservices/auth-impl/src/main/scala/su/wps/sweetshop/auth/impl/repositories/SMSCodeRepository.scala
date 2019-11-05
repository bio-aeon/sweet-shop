package su.wps.sweetshop.auth.impl.repositories

import su.wps.sweetshop.auth.impl.models.SMSCode

trait SMSCodeRepository[F[_]] {
  def insert(smsCode: SMSCode): F[Int]

  def findLastByPhone(phone: String): F[Option[SMSCode]]

  def findByPhoneAndCode(phone: String, code: String): F[Option[SMSCode]]
}
