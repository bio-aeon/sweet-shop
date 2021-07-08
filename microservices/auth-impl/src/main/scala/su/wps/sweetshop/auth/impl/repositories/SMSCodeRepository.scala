package su.wps.sweetshop.auth.impl.repositories

import cats.effect.Sync
import cats.syntax.functor._
import su.wps.sweetshop.auth.impl.models.SMSCode
import su.wps.sweetshop.auth.impl.repositories.sql.SMSCodeSql
import tofu.doobie.LiftConnectionIO

trait SMSCodeRepository[DB[_]] {
  def insert(smsCode: SMSCode): DB[Int]

  def findLastByPhone(phone: String): DB[Option[SMSCode]]

  def findByPhoneAndCode(phone: String, code: String): DB[Option[SMSCode]]
}

object SMSCodeRepository {
  def create[I[_]: Sync, DB[_]: LiftConnectionIO]: I[SMSCodeRepository[DB]] =
    SMSCodeSql.create[I, DB].map { sql =>
      new Impl[DB](sql)
    }

  private final class Impl[DB[_]](sql: SMSCodeSql[DB]) extends SMSCodeRepository[DB] {
    def insert(smsCode: SMSCode): DB[Int] =
      sql.insert(smsCode)

    def findLastByPhone(phone: String): DB[Option[SMSCode]] =
      sql.findLastByPhone(phone)

    def findByPhoneAndCode(phone: String, code: String): DB[Option[SMSCode]] =
      sql.findByPhoneAndCode(phone, code)
  }
}
