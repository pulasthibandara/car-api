package user.events

import common.akka.BaseEvent
import user.User

trait UserEvent extends BaseEvent

case class UserCreated(user: User) extends UserEvent
