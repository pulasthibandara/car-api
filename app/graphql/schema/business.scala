package graphql.schema

import java.util.UUID

import business.models.Business
import graphql.middleware.GraphQLAuthentication
import graphql.schema.VehicleGraphQL.UUIDType
import graphql.{RelayInterfaceTypes, SecureContext}
import play.api.libs.json.Json
import sangria.macros.derive._
import sangria.relay._
import sangria.schema._
import sangria.marshalling.playJson._

object BusinessGraphQL extends RelayInterfaceTypes
  with BusinessGraphQLTypes {

  val businessId = Argument("businessId", OptionInputType(UUIDType))

  def mutations: List[Field[SecureContext, Unit]] = List(
    Mutation.fieldWithClientMutationId[SecureContext, Unit, CreateBusinessPayload, CreateBusinessInputType](
      fieldName = "createBusiness",
      typeName = "CreateBusinessMutation",
      tags = GraphQLAuthentication.Authorized :: Nil,
      inputFields = List(InputField("business", createBusinessArgType)),
      outputFields = fields(
        Field("business", businessType, resolve = _.value.business)
      ),
      mutateAndGetPayload = {
        case (CreateBusinessInputType(b, clientMutationId), c) =>
          import c.ctx.ec
          c.ctx.businessService.createBusiness(b.name, b.domain, b.subdomain, c.ctx.identity.get)
            .map(CreateBusinessPayload(_, clientMutationId))
      }
    )
  )

  def queries: List[Field[SecureContext, Unit]] = List(
    Field(
      "business",
      businessType,
      arguments = businessId :: Nil,
      resolve = c => c.ctx
        .businessService
        .getBusinessById(c.arg(businessId)
          .getOrElse(c.ctx.business.get.id)))
  )

}

trait BusinessGraphQLTypes extends VehicleGraphQLTypes {
  implicit object BusinessIdentifiable extends Identifiable[Business] {
    def id(b: Business) = b.id.toString
  }

  implicit val businessType = deriveObjectType[SecureContext, Business](
    ObjectTypeDescription("This is the main entry point for the public facing graph"),
    Interfaces[SecureContext, Business](nodeInterface.asInstanceOf[InterfaceType[SecureContext, Business]]),
    ReplaceField[SecureContext, Business]("id", Node.globalIdField[SecureContext, Business]),
    AddFields(
      Field("listings", listingConnectionType, arguments = Connection.Args.All,
        resolve = c => {
          import c.ctx.ec

          Connection.connectionFromFutureSeq(
            c.ctx.listingService.listingsByBusinessId(c.value.id),
            ConnectionArgs(c)
          )
        })
    )
  )

  implicit val createBusinessArgReads = Json.reads[CreateBusinessArgType]
  implicit val createBusinessInputReads = Json.reads[CreateBusinessInputType]
  implicit val createBusinessArgType = deriveInputObjectType[CreateBusinessArgType]()
}

case class CreateBusinessArgType(name: String, domain: Option[String], subdomain: Option[String])
case class CreateBusinessInputType(business: CreateBusinessArgType, clientMutationId: Option[String])
case class CreateBusinessPayload(business: Business, clientMutationId: Option[String]) extends Mutation
