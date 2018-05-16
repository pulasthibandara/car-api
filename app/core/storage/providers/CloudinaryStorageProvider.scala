package core.storage.providers

import java.io.File

import com.cloudinary.Cloudinary
import com.google.inject.{Inject, Singleton}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

case class CloudinaryConfig(cloudName: String, apiKey: String, apiSecret: String)

@Singleton
class CloudinaryStorageProvider @Inject() (client: Cloudinary)(implicit ec: ExecutionContext) extends StorageProvider {

  val Provider = "cloudinary"

  def save(file: File, path: String, options: Map[String, Any] = Map()): Future[UploadResultType] =
    Future {
      val cloudinaryOptions = options + ("public_id" -> path)
      val result = client.uploader().upload(file, cloudinaryOptions.asJava)
        .asInstanceOf[java.util.Map[String, String]]
        .asScala
      UploadResult(result.get("public_id"), result.get("version"))
    }
}
