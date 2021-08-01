package su.wps.sweetshop.auth.api.models

import su.wps.sweetshop.auth.api.models.UserCreated.UserContact

final case class UserCreated(id: Int, contact: UserContact, isVerified: Boolean = true)

object UserCreated {
  sealed trait UserContact {
    val value: String
  }

  object UserContact {
    final case class Email(value: String) extends UserContact
    final case class Phone(value: String) extends UserContact
  }
}
