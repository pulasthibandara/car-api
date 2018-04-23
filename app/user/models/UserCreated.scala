package user.models

import user.User

case class UserCreated(user: User) extends UserEvent
