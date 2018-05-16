package vehicle

import java.time.Instant
import java.util.UUID

import core.database.PgSlickProfile
import core.storage.models.{File, ImageProperties}
import graphql.types.CommonGraphQLScalarTypes
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.json.{Format, Json}
import sangria.execution.deferred.HasId
import sangria.macros.derive._
import sangria.schema.ObjectType

case class ImageFileRef(fileId: UUID, file: Option[File[ImageProperties]])

object ImageFileRef extends CommonGraphQLScalarTypes {
  implicit val listingImageRefFormats: Format[ImageFileRef] = Json.format[ImageFileRef]

  implicit val fileImageRefGraphQLType: ObjectType[Unit, ImageFileRef] = deriveObjectType[Unit, ImageFileRef]()
}

case class Listing(
  id: UUID,
  makeId: UUID,
  modelId: UUID,
  businessId: UUID,
  title: String,
  slug: String,
  description: String,
  year: Option[Int] = None,
  kilometers: Option[Long] = None,
  color: Option[String] = None,
  bodyType: Option[BodyType.Value] = None,
  fuelType: Option[FuelType.Value] = None,
  transmissionType: Option[TransmissionType.Value] = None,
  cylinders: Option[Int] = None,
  engineSize: Option[Int] = None,
  conditionType: Option[ConditionType.Value] = None,
  features: List[String] = Nil,
  images: List[ImageFileRef] = Nil,
  createdBy: UUID,
  createdAt: Option[Instant]
)

case class Model(id: UUID, name: String, slug: String, makeId: UUID, make: Option[Make])

object Model {
  implicit val modelHasId: HasId[Model, UUID] = HasId(_.id)
}

case class Make(id: UUID, name: String, slug: String, models: Option[Seq[Model]] = None)

object Make {
  implicit val makeHasId: HasId[Make, UUID] = HasId(_.id)
}

object FuelType extends Enumeration {
  val Petrol, Diesel, Hybrid, Electric = Value
}

object TransmissionType extends Enumeration {
  val Manual, Tiptronic, Auto = Value
}

object BodyType extends Enumeration {
  val Convertible, Coupe, Hatchback, Sedan, StationWagon, SUV, UTE, Van = Value
}

object ConditionType extends Enumeration {
  val New, ReConditioned, Imported = Value
}

trait VehicleEnumDBMappings extends HasDatabaseConfigProvider[PgSlickProfile] {

  import profile.api._

  implicit val fuelTypeMapper = MappedColumnType.base[FuelType.Value, String](
    e => e.toString,
    s => FuelType.withName(s)
  )

  implicit val bodyTypeMapper = MappedColumnType.base[BodyType.Value, String](
    e => e.toString,
    s => BodyType.withName(s)
  )

  implicit val transmissionTypeMapper = MappedColumnType.base[TransmissionType.Value, String](
    e => e.toString,
    s => TransmissionType.withName(s)
  )

  implicit val conditionTypeMapper = MappedColumnType.base[ConditionType.Value, String](
    e => e.toString,
    s => ConditionType.withName(s)
  )
}
