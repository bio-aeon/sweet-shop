package su.wps.sweetshop.auth.impl.repositories.sql

import cats.effect.Sync
import cats.syntax.functor._
import cats.tagless.syntax.functorK._
import cats.tagless.{Derive, FunctorK}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import su.wps.sweetshop.auth.impl.models.SMSCode
import tofu.doobie.LiftConnectionIO

trait SMSCodeSql[DB[_]] {
  def insert(smsCode: SMSCode): DB[Int]

  def findLastByPhone(phone: String): DB[Option[SMSCode]]

  def findByPhoneAndCode(phone: String, code: String): DB[Option[SMSCode]]
}

object SMSCodeSql extends DefaultSMSCodeSql {
  implicit def functorK: FunctorK[SMSCodeSql] = Derive.functorK
}

abstract sealed class DefaultSMSCodeSql {
  def create[I[_]: Sync, DB[_]](implicit L: LiftConnectionIO[DB]): I[SMSCodeSql[DB]] =
    Slf4jDoobieLogHandler.create[I].map(implicit logger => new Impl().mapK(L.liftF))

  private final class Impl(implicit lh: LogHandler) extends SMSCodeSql[ConnectionIO] {
    val tableName: Fragment = Fragment.const("sms_codes")

    def insert(smsCode: SMSCode): ConnectionIO[Int] =
      (fr"""
      insert into""" ++ tableName ++ fr"""(
        phone,
        code,
        created_at
      )
      values (
        ${smsCode.phone},
        ${smsCode.code},
        ${smsCode.createdAt}
      )
    """).update.run

    def findLastByPhone(phone: String): ConnectionIO[Option[SMSCode]] =
      (fr"select * from" ++ tableName ++ fr" where phone = $phone order by created_at desc limit 1")
        .query[SMSCode]
        .option

    def findByPhoneAndCode(phone: String, code: String): ConnectionIO[Option[SMSCode]] =
      (fr"select * from" ++ tableName ++ fr"where phone = $phone and code = $code")
        .query[SMSCode]
        .option
  }
}
