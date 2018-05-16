package core.storage.models

import java.time.Instant
import java.util.UUID

import graphql.types.CommonGraphQLScalarTypes
import play.api.libs.json._
import sangria.macros.derive._
import sangria.schema._
import sangria.marshalling.playJson._

case class File[T <: FileProperties] (
  id: UUID,
  mimeType: Option[String],
  provider: String,
  providerId: Option[String],
  slug: Option[String],
  properties: Option[T],
  businessId: UUID,
  createdBy: UUID,
  createdAt: Option[Instant] = None
)

object File extends CommonGraphQLScalarTypes {
  implicit def fileFormats[T <: FileProperties](implicit propFormats: Format[T]): Format[File[T]] = Json.format[File[T]]
  implicit def ImageFileType[T <: FileProperties](implicit fp: ObjectType[Unit, T]): ObjectType[Unit, File[T]] =
    ObjectType[Unit, File[T]]("ImageFileType", List[Field[Unit, File[T]]](
      Field("id", UUIDType, resolve = c => c.value.id),
      Field("properties", OptionType(FileProperties.FilePropertiesInterfaceType), resolve = c => c.value.properties.asInstanceOf[Some[FileProperties]])
    ))
}

trait FileProperties {
  val fileName: String
  val size: Option[Long]
  val `type`: String
}

object FileProperties {
  implicit val FilePropertiesInterfaceType: InterfaceType[Unit, FileProperties] = InterfaceType[Unit, FileProperties](
    "FilePropertiesInterface",
    None,
    () => List[Field[Unit, FileProperties]](
      Field("fileName", StringType, resolve = c => c.value.fileName),
      Field("size", OptionType(LongType), resolve = c => c.value.size),
      Field("type", StringType, resolve = c => c.value.`type`)
    ),
    Nil,
    () => List(ImageProperties.ImagePropertiesType),
    Vector.empty
  )
}

case class ImageProperties(
  fileName: String,
  size: Option[Long],
  `type`: String = ImageProperties.PropType
) extends FileProperties

trait ImagePropertiesGraphQLTypes {
  implicit val ImagePropertiesType: ObjectType[Unit, ImageProperties] =
    deriveObjectType[Unit, ImageProperties](
      Interfaces(PossibleInterface[Unit, FileProperties, ImageProperties](FileProperties.FilePropertiesInterfaceType))
    )
}

object ImageProperties extends ImagePropertiesGraphQLTypes {
  val PropType = "Image"
  implicit val imagePropertiesFormats: Format[ImageProperties] = Json.format[ImageProperties]
}
