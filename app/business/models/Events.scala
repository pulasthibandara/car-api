package business.models

import common.akka.BaseEvent
import user.User

trait BusinessEvent extends BaseEvent

case class BusinessCreated(business: Business, user: User) extends BusinessEvent
