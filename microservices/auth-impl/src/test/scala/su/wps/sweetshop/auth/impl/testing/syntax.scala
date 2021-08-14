package su.wps.sweetshop.auth.impl.testing

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object syntax {
  implicit def toRunConnectionIOOps[A](ca: ConnectionIO[A]): RunConnectionIOOps[A] =
    new RunConnectionIOOps[A](ca)

  private[syntax] final class RunConnectionIOOps[A](private val ca: ConnectionIO[A])
      extends AnyVal {

    def runWithIO()(implicit xa: Transactor[IO]): A =
      ca.transact(xa).unsafeRunSync()
  }
}
