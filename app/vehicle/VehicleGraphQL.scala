package vehicle

import java.util.UUID
import javax.inject.Singleton

import sangria.schema._
import sangria.macros.derive._
import sangria.relay.{Connection, ConnectionDefinition, Mutation, Node}
import sangria.marshalling.playJson._
import _root_.models.{GraphQLAuthentication, RelayInterfaceTypes, SecureContext}
import com.google.inject.Inject
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

case class CreateListingArgs(
  id: Option[UUID],
  modelId: UUID,
  title: String,
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
)

case class CreateListingInputType(listing: CreateListingArgs, clientMutationId: Option[String])

case class CreateListingPayload(
  listing: Listing,
  clientMutationId: Option[String]
) extends Mutation

@Singleton
class VehicleGraphQL @Inject() (
  implicit ec: ExecutionContext
) extends VehicleGraphQLImplicits {

  val mutations: List[Field[SecureContext, Unit]] = List(
    Mutation.fieldWithClientMutationId[SecureContext, Unit, CreateListingPayload, CreateListingInputType](
      fieldName = "createListing",
      typeName = "CreateListingMutation",
      tags = GraphQLAuthentication.Authorized :: Nil,
      inputFields = List(InputField("listing", CreateListingArgsInputType)),
      outputFields = fields(
        Field("listing", ListingType, resolve = ctx => ctx.value.listing)
      ),
      mutateAndGetPayload = (input, c) ⇒ {
        val CreateListingInputType(listing, clientMutationId) = input
        c.ctx.listingService.createListing(
          listing.id,
          c.ctx.identity.get.businessId
            .getOrElse(throw new UserHasNoBusinessDefined("User needs to have a business to create a listing.")),
          c.ctx.identity.get.id,
          listing.modelId,
          listing.title,
          listing.description,
          listing.year,
          listing.kilometers,
          listing.color,
          listing.bodyType,
          listing.fuelType,
          listing.transmissionType,
          listing.cylinders,
          listing.engineSize,
          listing.conditionType,
          listing.features
        ).map(CreateListingPayload(_, clientMutationId))
      }
    )
  )
}

trait VehicleGraphQLImplicits extends VehicleGraphQLTypes {
  implicit val createListingArgsReads = Json.reads[CreateListingArgs]
  implicit val createListingInputTypeReads = Json.reads[CreateListingInputType]

  implicit val CreateListingArgsInputType = deriveInputObjectType[CreateListingArgs]()
}

trait VehicleGraphQLTypes extends LowPriorityVehicleGraphQLImplicits with RelayInterfaceTypes {
  implicit lazy val MakeType = deriveObjectType[Unit, Make](
    ReplaceField[Unit, Make]("id", Node.globalIdField[Unit, Make]),
    Interfaces[Unit, Make](nodeInterface.asInstanceOf[InterfaceType[Unit, Make]])
  )

  implicit lazy val ModelType = deriveObjectType[Unit, Model](
    ReplaceField[Unit, Model]("id", Node.globalIdField[Unit, Model]),
    Interfaces[Unit, Model](nodeInterface.asInstanceOf[InterfaceType[Unit, Model]])
  )

  implicit lazy val ListingType: ObjectType[_, Listing] = deriveObjectType[Unit, Listing](
    ReplaceField[Unit, Listing]("id", Node.globalIdField[Unit, Listing]),
    Interfaces[Unit, Listing](nodeInterface.asInstanceOf[InterfaceType[Unit, Listing]])
  )

  val ConnectionDefinition(_, listingConnectionType) =
    Connection.definition[SecureContext, Connection, Listing]("Listing", ListingType)
}
