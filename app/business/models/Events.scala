package business.models

import core.akka.BaseEvent
import user.User

trait BusinessEvent extends BaseEvent

case class BusinessCreated(business: Business, user: User) extends BusinessEvent
