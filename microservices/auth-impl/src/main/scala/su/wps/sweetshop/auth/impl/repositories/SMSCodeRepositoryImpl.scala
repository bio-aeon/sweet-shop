package su.wps.sweetshop.auth.impl.repositories

import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import su.wps.sweetshop.auth.impl.models.SMSCode

class SMSCodeRepositoryImpl extends DoobieRepository with SMSCodeRepository[ConnectionIO] {
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
