package core.akka

import scala.concurrent.ExecutionContext

abstract class EventListener(tags: Class[_]*)(implicit ec: ExecutionContext)
  extends RestartableActor with core.Logger {

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    tags.foreach { tag =>
      context.system.eventStream.subscribe(context.self, tag)
    }
    super.preStart()
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(context.self)
    super.postStop()
  }
}
