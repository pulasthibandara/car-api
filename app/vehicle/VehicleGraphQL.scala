package vehicle

import java.util.UUID
import javax.inject.Singleton

import sangria.schema._
import sangria.macros.derive._
import sangria.relay.Mutation
import sangria.marshalling.playJson._
import _root_.models.{GraphQLAuthentication, SecureContext}
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

trait VehicleGraphQLImplicits extends LowPriorityVehicleGraphQLImplicits {
  implicit val createListingArgsReads = Json.reads[CreateListingArgs]
  implicit val createListingInputTypeReads = Json.reads[CreateListingInputType]

  implicit val CreateListingArgsInputType = deriveInputObjectType[CreateListingArgs]()
}
