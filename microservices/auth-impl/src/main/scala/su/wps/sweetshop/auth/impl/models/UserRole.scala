package su.wps.sweetshop.auth.impl.models

import doobie.Meta
import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class UserRole(val value: String) extends StringEnumEntry

object UserRole extends StringEnum[UserRole] {
  case object User extends UserRole("USER")
  case object Staff extends UserRole("STAFF")

  def values: immutable.IndexedSeq[UserRole] = findValues

  implicit val meta: Meta[UserRole] = Meta[String].timap(UserRole.withValue)(_.value)
}
