package su.wps.sweetshop.utils.syntax

import cats.Applicative
import cats.effect.Resource

trait ResourceSyntax {
  final implicit def syntaxResource[F[_], A](fa: F[A]): ResourceOps[F, A] = new ResourceOps(fa)
}

private[syntax] final class ResourceOps[F[_], A](private val fa: F[A]) extends AnyVal {
  def toResource(implicit F: Applicative[F]): Resource[F, A] = Resource.eval(fa)
}
