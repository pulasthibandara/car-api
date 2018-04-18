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
  userId: UUID,
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
  createdAt: Option[Instant]
)

case class Model(id: UUID, name: String, slug: String, make: Make)

case class Make(id: UUID, name: String, slug: String)

trait LowPriorityVehicleGraphQLImplicits extends CommonGraphQLScalarTypes {
  implicit lazy val BodyTypeType = deriveEnumType[BodyType.Value]()
  implicit lazy val FuelTypeType = deriveEnumType[FuelType.Value]()
  implicit lazy val TransmissionTypeType = deriveEnumType[TransmissionType.Value]()
  implicit lazy val ConditionTypeType = deriveEnumType[ConditionType.Value]()

  implicit lazy val bodyTypeReads = Reads.enumNameReads(BodyType)
  implicit lazy val fuelTypeReads = Reads.enumNameReads(FuelType)
  implicit lazy val transmissionTypeReads = Reads.enumNameReads(TransmissionType)
  implicit lazy val conditionTypeReads = Reads.enumNameReads(ConditionType)

  implicit object ListingIdentifiable extends Identifiable[Listing] {
    def id(listing: Listing) = listing.id.toString
  }

  implicit object MakeIdentifiable extends Identifiable[Make] {
    def id(make: Make) = make.id.toString
  }

  implicit object ModelIdentifiable extends Identifiable[Model] {
    def id(model: Model) = model.id.toString
  }

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
