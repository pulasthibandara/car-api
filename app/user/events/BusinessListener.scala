package user.events

import business.models.{BusinessCreated, BusinessEvent}
import com.google.inject.Inject
import core.akka.EventListener
import user.UserService

import scala.concurrent.ExecutionContext

class BusinessListener @Inject() (
  userService: UserService
) (implicit ec: ExecutionContext) extends EventListener(classOf[BusinessEvent]) {
  override def receive: Receive = {
    case BusinessCreated(business, user) => {
      userService.addBusiness(user, business)
    }
  }
}
