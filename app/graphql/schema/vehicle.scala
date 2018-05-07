package graphql.schema

import java.util.UUID

import common.CommonGraphQLScalarTypes
import graphql.middleware.GraphQLAuthentication
import graphql.{RelayInterfaceTypes, SecureContext}
import play.api.libs.json.{Json, Reads}
import sangria.macros.derive._
import sangria.relay._
import sangria.marshalling.playJson._
import sangria.schema._
import vehicle.{BodyType, ConditionType, FuelType, TransmissionType, _}

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

trait VehicleGraphQL extends VehicleGraphQLImplicits {

  def mutations: List[Field[SecureContext, Unit]] = List(
    Mutation.fieldWithClientMutationId[SecureContext, Unit, CreateListingPayload, CreateListingInputType](
      fieldName = "createListing",
      typeName = "CreateListingMutation",
      tags = GraphQLAuthentication.Authorized :: Nil,
      inputFields = List(InputField("listing", CreateListingArgsInputType)),
      outputFields = fields(
        Field("listing", ListingType, resolve = ctx => ctx.value.listing)
      ),
      mutateAndGetPayload = (input, c) ⇒ {
        import c.ctx.ec

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
  implicit val createListingArgsReads: Reads[CreateListingArgs] = Json.reads[CreateListingArgs]
  implicit val createListingInputTypeReads: Reads[CreateListingInputType] = Json.reads[CreateListingInputType]

  implicit val CreateListingArgsInputType: InputObjectType[CreateListingArgs] = deriveInputObjectType[CreateListingArgs]()
}

trait VehicleGraphQLTypes extends LowPriorityVehicleGraphQLImplicits with RelayInterfaceTypes {
  implicit lazy val MakeType: ObjectType[SecureContext, Make] = deriveObjectType[SecureContext, Make](
    ReplaceField[SecureContext, Make]("id", Node.globalIdField[SecureContext, Make]),
    ReplaceField[SecureContext, Make]("models", Field("models", modelConnectionType, arguments = Connection.Args.All,
      resolve = c => {
        import c.ctx.ec

        Connection.connectionFromFutureSeq(
          c.ctx.taxonomyService.getModelsByMakes(List(c.value.id)),
          ConnectionArgs(c)
        )
      }
    )),
    Interfaces[SecureContext, Make](nodeInterface.asInstanceOf[InterfaceType[SecureContext, Make]])
  )

  implicit lazy val ModelType: ObjectType[Unit, Model] = deriveObjectType[Unit, Model](
    ReplaceField[Unit, Model]("id", Node.globalIdField[Unit, Model]),
    Interfaces[Unit, Model](nodeInterface.asInstanceOf[InterfaceType[Unit, Model]])
  )

  implicit lazy val ListingType: ObjectType[Unit, Listing] = deriveObjectType[Unit, Listing](
    ReplaceField[Unit, Listing]("id", Node.globalIdField[Unit, Listing]),
    Interfaces[Unit, Listing](nodeInterface.asInstanceOf[InterfaceType[Unit, Listing]])
  )

  val ConnectionDefinition(_, modelConnectionType) =
    Connection.definition[SecureContext, Connection, Model]("Model", ModelType)

  val ConnectionDefinition(_, makeConnectionType) =
    Connection.definition[SecureContext, Connection, Make]("Make", MakeType)

  val ConnectionDefinition(_, listingConnectionType) =
    Connection.definition[SecureContext, Connection, Listing]("Listing", ListingType)
}

trait LowPriorityVehicleGraphQLImplicits extends CommonGraphQLScalarTypes {
  implicit lazy val BodyTypeType: EnumType[BodyType.Value] = deriveEnumType[BodyType.Value]()
  implicit lazy val FuelTypeType: EnumType[FuelType.Value] = deriveEnumType[FuelType.Value]()
  implicit lazy val TransmissionTypeType: EnumType[TransmissionType.Value] = deriveEnumType[TransmissionType.Value]()
  implicit lazy val ConditionTypeType: EnumType[ConditionType.Value] = deriveEnumType[ConditionType.Value]()

  implicit lazy val bodyTypeReads: Reads[BodyType.Value] = Reads.enumNameReads(BodyType)
  implicit lazy val fuelTypeReads: Reads[FuelType.Value] = Reads.enumNameReads(FuelType)
  implicit lazy val transmissionTypeReads: Reads[TransmissionType.Value] = Reads.enumNameReads(TransmissionType)
  implicit lazy val conditionTypeReads: Reads[ConditionType.Value] = Reads.enumNameReads(ConditionType)

  implicit lazy val listingIdentifiable: Identifiable[Listing] = (listing: Listing) => listing.id.toString

  implicit lazy val makeIdentifiable: Identifiable[Make] = (make: Make) => make.id.toString

  implicit lazy val ModelIdentifiable: Identifiable[Model] = (model: Model) => model.id.toString

}
