package su.wps.sweetshop.payments.impl.testing

import cats.Monad
import cats.implicits._
import fs2.concurrent.InspectableQueue

object syntax {
  implicit final class DequeueCurrent[F[_]: Monad, A](val q: InspectableQueue[F, A]) {
    def dequeueCurrent: F[List[A]] =
      for {
        c <- q.getSize
        // not quite right if c > chunkSize
        elems <- q.tryDequeueChunk1(c)
      } yield elems.map(_.toList).orEmpty
  }
}
