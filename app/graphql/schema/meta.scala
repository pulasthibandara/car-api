package graphql.schema

import java.util.UUID

import business.models.Business
import graphql.middleware.GraphQLAuthentication
import graphql.{RelayInterfaceTypes, SecureContext}
import play.api.libs.json.Json
import sangria.macros.derive._
import sangria.relay._
import sangria.schema._
import sangria.marshalling.playJson._
import vehicle._

object MetaGraphql extends RelayInterfaceTypes
  with MetaGraphQLTypes {

  def queries: List[Field[SecureContext, Unit]] = List(
    Field(
      "application",
      applicationDataType,
      resolve = c => ???
    )
  )

}

trait MetaGraphQLTypes extends LowPriorityVehicleGraphQLImplicits {
  val applicationDataType = deriveObjectType[SecureContext, ApplicationDataType]()
}

case class ApplicationDataType(
//  makes: Seq[Make],
  fuelTypes: Seq[FuelType.Value],
  transmissionTypes: Seq[TransmissionType.Value],
  bodyTypes: Seq[BodyType.Value],
  conditionType: Seq[ConditionType.Value]
)
