package core

trait Logger {
  val logger = play.api.Logger(this.getClass)
}
