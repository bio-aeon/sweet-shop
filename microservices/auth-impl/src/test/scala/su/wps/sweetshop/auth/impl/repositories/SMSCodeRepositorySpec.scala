package su.wps.sweetshop.auth.impl.repositories

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.ScalacheckShapeless._
import org.specs2.mutable.Specification
import su.wps.sweetshop.auth.impl.models.SMSCode
import su.wps.sweetshop.auth.impl.testing.DbTest
import su.wps.sweetshop.auth.impl.testing.scalacheck._
import su.wps.sweetshop.auth.impl.testing.syntax._

import java.time.{ZoneOffset, ZonedDateTime}

class SMSCodeRepositorySpec extends Specification with DbTest {
  sequential

  lazy val repo: SMSCodeRepository[doobie.ConnectionIO] =
    SMSCodeRepository.create[IO, ConnectionIO].unsafeRunSync()

  implicit val genSMSCode: Gen[SMSCode] =
    arbitrary[SMSCode].map(x => x.copy(code = x.code.take(10)))

  "SMSCodeRepository should" >> {
    "insert sms code successfully" >> {
      val test = for {
        r0 <- findSMSCodesCount
        _ <- repo.insert(random[SMSCode])
        r1 <- findSMSCodesCount
      } yield (r0, r1)

      val (r0, r1) = test.runWithIO()
      r0 mustEqual 0
      r1 mustEqual 1
    }

    "find last sms code by phone" >> {
      val phone = "9111111111"
      val now = ZonedDateTime.now
      val first = random[SMSCode].copy(phone = phone, createdAt = now.minusMinutes(5))
      val last = random[SMSCode].copy(phone = phone, createdAt = now)
      val test = for {
        _ <- repo.insert(first)
        _ <- repo.insert(last)
        r <- repo.findLastByPhone(phone)
      } yield r

      val r = test.runWithIO()
      r must beSome.which(_.createdAt.isEqual(now.withZoneSameLocal(ZoneOffset.UTC)))
    }

    "find sms code by phone and code" >> {
      val phone = "9111111111"
      val code = "abcde"
      val smsCode = random[SMSCode].copy(phone = phone, code = code)
      val test = for {
        _ <- repo.insert(smsCode)
        _ <- repo.insert(random[SMSCode])
        r <- repo.findByPhoneAndCode(phone, code)
      } yield r

      val r = test.runWithIO()
      r must beSome.which(_.code == code)
    }
  }

  private def findSMSCodesCount: ConnectionIO[Int] =
    sql"select count(*) from sms_codes".query[Int].unique

  protected def truncateAll(): Unit =
    sql"""
         |delete from sms_codes;
         |""".stripMargin.update.run
      .transact(xa)
      .unsafeRunSync()
}
