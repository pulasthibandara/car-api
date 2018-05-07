package vehicle

import java.time.Instant
import java.util.UUID

import common.CommonGraphQLScalarTypes
import common.database.PgSlickProfile
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.json.Reads
import sangria.macros.derive._
import sangria.relay._

case class Listing(
  id: UUID,
  makeId: UUID,
  modelId: UUID,
  businessId: UUID,
  title: String,
  slug: String,
  description: String,
  year: Option[Int],
  kilometers: Option[Long],
  color: Option[String],
  bodyType: Option[BodyType.Value],
  fuelType: Option[FuelType.Value],
  transmissionType: Option[TransmissionType.Value],
  cylinders: Option[Int],
  engineSize: Option[Int],
  conditionType: Option[ConditionType.Value],
  features: List[String],
  createdBy: UUID,
  createdAt: Option[Instant]
)

case class Model(id: UUID, name: String, slug: String, makeId: UUID, make: Option[Make])

case class Make(id: UUID, name: String, slug: String, models: Option[Seq[Model]] = None)



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
