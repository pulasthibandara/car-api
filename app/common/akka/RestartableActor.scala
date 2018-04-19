package common.akka

import akka.actor.{ ActorLogging, Actor, ActorContext }

trait RestartableActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.debug(s"${who(context)}preStart(${context.self.toString()}}).")
    super.preStart()
  }

  override def postStop(): Unit = {
    log.debug(s"${who(context)}postStop().")
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.warning(s"${who(context)}preRestart(${reason.getMessage}, $message).")
    super.preRestart(reason, message)

    val config = context.system.settings.config
    if (config.getBoolean("app.terminate-on-actor-restart")) {
      log.error("Restarts are not allowed by configuration")
      sys.exit(1)
    }
  }

  override def postRestart(reason: Throwable): Unit = {
    log.warning(s"${who(context)}postRestart(${reason.getMessage}).")
    super.postRestart(reason)
  }

  def who(context: ActorContext): String = s"[${context.self.path}] "
}

