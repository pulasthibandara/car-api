package common

trait Logger {
  val logger = play.api.Logger(this.getClass)
}
