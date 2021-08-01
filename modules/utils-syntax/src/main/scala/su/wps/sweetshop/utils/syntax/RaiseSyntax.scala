package su.wps.sweetshop.utils.syntax

import cats.Applicative
import tofu.Raise.ContravariantRaise

import scala.util.control.NonFatal

trait RaiseSyntax {
  def catchNonFatal[F[_], A, E](a: => A)(f: Throwable => E)(implicit A: Applicative[F],
                                                            R: ContravariantRaise[F, E]): F[A] =
    try A.pure(a)
    catch {
      case NonFatal(ex) => R.raise(f(ex))
    }
}
