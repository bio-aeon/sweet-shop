package su.wps.sweetshop.auth.impl.utils

import cats.~>

trait CatsSupport {
  implicit class NaturalTransformation[F[_], A](fa: F[A]) {
    def liftT[G[_]](implicit transform: F ~> G): G[A] =
      transform(fa)
  }
}

object CatsSupport extends CatsSupport
