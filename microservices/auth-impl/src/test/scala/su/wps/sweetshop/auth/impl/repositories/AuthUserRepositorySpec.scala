package su.wps.sweetshop.auth.impl.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.ScalacheckShapeless._
import org.specs2.mutable.Specification
import su.wps.sweetshop.auth.impl.models.AuthUser
import su.wps.sweetshop.auth.impl.testing.DbTest
import su.wps.sweetshop.auth.impl.testing.scalacheck._
import su.wps.sweetshop.auth.impl.testing.syntax._

class AuthUserRepositorySpec extends Specification with DbTest {
  sequential

  lazy val repo: AuthUserRepository[doobie.ConnectionIO] =
    AuthUserRepository.create[IO, ConnectionIO].unsafeRunSync()

  implicit val genUser: Gen[AuthUser] = arbitrary[AuthUser]

  "AuthUserRepository should" >> {
    "insert user successfully" >> {
      val test = for {
        r0 <- findUsersCount
        _ <- repo.insert(random[AuthUser])
        r1 <- findUsersCount
      } yield (r0, r1)

      val (r0, r1) = test.runWithIO()
      r0 mustEqual 0
      r1 mustEqual 1
    }

    "find user by phone" >> {
      val phone = "9111111111"
      val test = for {
        _ <- repo.insert(random[AuthUser].copy(phone = Some(phone)))
        r <- repo.findByPhone(phone)
      } yield r

      val r = test.runWithIO()
      r must beSome
    }
  }

  private def findUsersCount: ConnectionIO[Int] =
    sql"select count(id) from auth_users".query[Int].unique

  protected def truncateAll(): Unit =
    sql"""
         |delete from auth_users;
         |""".stripMargin.update.run
      .transact(xa)
      .unsafeRunSync()
}
