package su.wps.sweetshop.auth.impl.testing

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary, Gen}
import su.wps.sweetshop.auth.impl.models.{UserId, UserRole}

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.reflect.runtime.universe._
import scala.util.{Success, Try}

final case class RandomDataException(msg: String) extends Exception(msg)

object scalacheck {
  implicit val arbZonedDt: Arbitrary[ZonedDateTime] = Arbitrary {
    Gen.chooseNum(10, 1000).map { secondsAgo =>
      val now = Instant.now.getEpochSecond
      ZonedDateTime.ofInstant(Instant.ofEpochSecond(now - secondsAgo), ZoneId.systemDefault)
    }
  }

  implicit val arbUserId: Arbitrary[UserId] = Arbitrary(arbitrary[Int].map(UserId(_)))

  implicit val arbUserRole: Arbitrary[UserRole] = Arbitrary {
    import su.wps.sweetshop.auth.impl.models.UserRole._
    Gen.oneOf(User, Staff)
  }

  def random[T: WeakTypeTag: Gen]: T = random(1).head

  def random[T: WeakTypeTag](n: Int)(implicit gen: Gen[T]): List[T] = {
    val randomLong = scala.util.Random.nextLong()
    val streamGen = Gen.infiniteStream(gen)
    Try(streamGen.apply(Gen.Parameters.default, Seed(randomLong))) match {
      case Success(Some(x)) => x.take(n).toList
      case _ => raise[T]
    }
  }

  private def raise[T: WeakTypeTag] = {
    val tpe = implicitly[WeakTypeTag[T]].tpe
    val msg = s"Could not generate a random value for $tpe."
    throw RandomDataException(msg)
  }
}
