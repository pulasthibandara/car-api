package testhelpers

import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Around
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Injecting}

trait WithApplication extends Injecting with Around with FutureAwaits with DefaultAwaitTimeout {
  lazy val app: Application = GuiceApplicationBuilder().build()

  override def around[T: AsResult](t: => T): Result = {
    try AsResult(t)
    finally await(app.stop())
  }
}
