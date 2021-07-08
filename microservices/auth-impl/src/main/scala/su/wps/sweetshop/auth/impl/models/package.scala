package su.wps.sweetshop.auth.impl

import doobie.Meta
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

package object models {
  @newtype final case class UserId(value: Int)

  object UserId {
    implicit val meta: Meta[UserId] = deriving
    implicit val encoder: Encoder[UserId] = deriving
    implicit val decoder: Decoder[UserId] = deriving
  }
}
