package vehicle

import java.time.Instant
import java.util.UUID

import common.CommonGraphQLScalarTypes
import sangria.macros.derive.deriveObjectType

case class Listing(
  id: UUID,
  makeId: UUID,
  modelId: UUID,
  userId: UUID,
  title: String,
  description: String,
  slug: String,
  createdAt: Option[Instant]
)

case class Model(id: UUID, name: String, slug: String, make: Make)

case class Make(id: UUID, name: String, slug: String)

trait VehicleImplicits extends CommonGraphQLScalarTypes {
  implicit lazy val ListingType = deriveObjectType[Unit, Listing]()
  implicit lazy val MakeType = deriveObjectType[Unit, Make]()
  implicit lazy val ModelType = deriveObjectType[Unit, Model]()
}
