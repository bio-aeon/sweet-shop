package su.wps.sweetshop.auth.impl.wirings

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.Sync
import su.wps.sweetshop.auth.impl.repositories.{AuthUserRepository, SMSCodeRepository}
import tofu.doobie.LiftConnectionIO

final case class RepositoryWirings[DB[_]](authUserRepo: AuthUserRepository[DB],
                                          smsCodeRepo: SMSCodeRepository[DB])

object RepositoryWirings {

  def create[I[_]: Sync, DB[_]: LiftConnectionIO]: I[RepositoryWirings[DB]] =
    for {
      authUserRepo <- AuthUserRepository.create[I, DB]
      smsCodeRepo <- SMSCodeRepository.create[I, DB]
    } yield RepositoryWirings(authUserRepo, smsCodeRepo)
}
