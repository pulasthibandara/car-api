package core.storage

import com.cloudinary.Cloudinary
import com.google.inject.{AbstractModule, Provides}
import core.storage.providers.CloudinaryConfig
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

import scala.collection.JavaConverters._

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {

  }

  @Provides
  def provideCloudinaryClient(configuration: Configuration): Cloudinary = {
    val conf = configuration.underlying.as[CloudinaryConfig]("storage.cloudinary")
    new Cloudinary(Map(
      "cloud_name" -> conf.cloudName,
      "api_key" -> conf.apiKey,
      "api_secret" -> conf.apiSecret
    ).asJava)
  }
}
