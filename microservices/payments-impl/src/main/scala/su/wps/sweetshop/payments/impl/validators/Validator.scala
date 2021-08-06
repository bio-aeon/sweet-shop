package su.wps.sweetshop.payments.impl.validators

import cats.Applicative
import cats.data.{Nested, NonEmptyList}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.nested._
import mouse.any._
import su.wps.sweetshop.payments.impl.validators.errors.IncorrectParam
import tofu.Raise

trait Validator[F[_], G[_], T, R] {
  implicit val F: Applicative[F]
  implicit val R: Raise[F, NonEmptyList[IncorrectParam]]
  implicit val G: Applicative[G]

  def validate(input: T): G[F[R]]

  protected def success[A](a: A): F[A] =
    F.pure(a)

  protected def failure[A](key: String, message: String): F[A] =
    R.raise(NonEmptyList.of(IncorrectParam(key, message)))

  protected def validate[A](a: A, key: String, message: String)(p: => G[Boolean]): G[F[A]] =
    G.ifF(p)(success(a), failure[A](key, message))

  protected def validateOpt[A](a: Option[A])(_validate: A => G[F[A]]): G[F[Option[A]]] =
    a.map(x => _validate(x).map(_.map(_.some))).getOrElse(success(a).pure[G])

  protected def validateOpt[A](a: Option[A], key: String, message: String)(
    p: A => G[Boolean]
  ): G[F[Option[A]]] =
    a match {
      case Some(x) =>
        G.ifF(p(x))(success(a), failure[Option[A]](key, message))
      case None => success(a).pure[G]
    }

  protected def validateLimited(value: String,
                                key: String,
                                minLength: Int,
                                maxLength: Int): Nested[G, F, String] =
    validate(value, key, s"Length must be between $minLength and $maxLength.")(
      value.length |> (l => l >= minLength && l <= maxLength) |> (_.pure[G])
    ).nested
}
