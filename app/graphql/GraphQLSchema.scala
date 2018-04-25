package graphql

import business.services.BusinessService
import graphql.schema.{AuthGarphQL, BusinessGraphQL, VehicleGraphQL}
import sangria.relay.{GlobalId, Identifiable, Node, NodeDefinition}
import sangria.schema._
import user._
import vehicle.ListingService

import scala.concurrent.ExecutionContext

case class SecureContext(
  identity: Option[User],
  authService: AuthService,
  listingService: ListingService,
  businessService: BusinessService,
  implicit val ec: ExecutionContext
)

trait RelayInterfaceTypes {
  /**
    * We get the node interface and field from the relay library.
    *
    * The first method is the way we resolve an ID to its object. The second is the
    * way we resolve an object that implements node to its type.
    */
  val NodeDefinition(nodeInterface, nodeField, nodesField) =
    Node.definition((globalId: GlobalId, ctx: Context[SecureContext, Unit]) => {
      if (globalId.typeName == "Vehicle")
        ???
      else if (globalId.typeName == "User")
        ???
      else
        None
    }, Node.possibleNodeTypes[SecureContext, Node]())

  /**
    * Extracts id value from an Identifiable type
    */
  def idFields[T: Identifiable] = fields[Unit, T](
    Node.globalIdField,
    Field("rawId", StringType, resolve = ctx => implicitly[Identifiable[T]].id(ctx.value))
  )
}

object GraphQLSchema {

  val QueryType = ObjectType(
    "Query",
    fields[SecureContext, Unit](
//      Field(
//        "test",
//        StringType,
//        Some("Fetch all users"),
//        tags = GraphQLAuthentication.Authorized :: Nil,
//        resolve = c => "test"
//      ),
      BusinessGraphQL.queries: _*
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[SecureContext, Unit](
      AuthGarphQL.mutations() ++
        VehicleGraphQL.mutations ++
        BusinessGraphQL.mutations:_*
    )
  )

  val SchemaDefinition = Schema(QueryType, Some(MutationType))
}
