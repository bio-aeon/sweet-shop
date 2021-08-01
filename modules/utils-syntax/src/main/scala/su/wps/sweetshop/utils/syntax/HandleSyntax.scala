package su.wps.sweetshop.utils.syntax

import tofu.Catches
import tofu.logging.Logging
import tofu.syntax.logging._

trait HandleSyntax {
  final implicit def syntaxHandle[F[_]](fa: F[Unit]): HandleOps[F] = new HandleOps(fa)
}

private[syntax] final class HandleOps[F[_]](private val fa: F[Unit]) extends AnyVal {
  def handled(implicit H: Catches[F], log: Logging[F]): F[Unit] =
    H.handleWith(fa) { err =>
      errorCause"${err.getMessage}" (err)
    }
}
