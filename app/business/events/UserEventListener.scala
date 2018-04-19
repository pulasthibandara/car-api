package business.events


import com.google.inject.Inject
import common.akka.EventListener
import user.events.UserEvent

import scala.concurrent.ExecutionContext

class UserEventListener @Inject() (implicit ec: ExecutionContext) extends EventListener(classOf[UserEvent]) {
  override def receive: Receive = {
    case e: UserEvent => logger.info("user creation received at business.")
  }
}
