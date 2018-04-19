package business

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

class BusinessModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {

  }
}
