package business.events


import com.google.inject.Inject
import core.akka.EventListener
import user.models.UserEvent

import scala.concurrent.ExecutionContext

class UserEventListener @Inject() (implicit ec: ExecutionContext) extends EventListener(classOf[UserEvent]) {
  override def receive: Receive = {
    case e: UserEvent => logger.info("user creation received at business.")
  }
}
