package business.graphql

import business.models.Business
import com.google.inject.{Inject, Singleton}
import models.{GraphQLAuthentication, RelayInterfaceTypes, SecureContext}
import play.api.libs.json.Json
import sangria.schema._
import sangria.macros.derive._
import sangria.marshalling.playJson._
import sangria.relay.{Connection, Identifiable, Mutation, Node}
import vehicle.VehicleGraphQLTypes

import scala.concurrent.ExecutionContext

@Singleton
class BusinessGraphQL @Inject() (implicit ec: ExecutionContext) extends RelayInterfaceTypes
  with BusinessGraphQLTypes {

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
          c.ctx.businessService.createBusiness(b.name, b.domain, b.subdomain, c.ctx.identity.get)
            .map(CreateBusinessPayload(_, clientMutationId))
      }
    )
  )

  def queries: List[Field[SecureContext, Unit]] = List(

  )

}

trait BusinessGraphQLTypes extends VehicleGraphQLTypes {
  implicit object BusinessIdentifiable extends Identifiable[Business] {
    def id(b: Business) = b.id.toString
  }

  implicit val businessType = deriveObjectType[Unit, Business](
    ObjectTypeDescription("This is the main entry point for the public facing graph"),
    Interfaces[Unit, Business](nodeInterface.asInstanceOf[InterfaceType[Unit, Business]]),
    ReplaceField[Unit, Business]("id", Node.globalIdField[Unit, Business]),
    AddFields(
      Field("listings", listingConnectionType, arguments = Connection.Args.All,
        resolve = c => ???)
    )
  )

  implicit val createBusinessArgReads = Json.reads[CreateBusinessArgType]
  implicit val createBusinessInputReads = Json.reads[CreateBusinessInputType]
  implicit val createBusinessArgType = deriveInputObjectType[CreateBusinessArgType]()
}

case class CreateBusinessArgType(name: String, domain: Option[String], subdomain: Option[String])
case class CreateBusinessInputType(business: CreateBusinessArgType, clientMutationId: Option[String])
case class CreateBusinessPayload(business: Business, clientMutationId: Option[String]) extends Mutation
