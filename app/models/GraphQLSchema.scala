package models

import javax.inject.Singleton

import com.google.inject.Inject
import sangria.relay.{GlobalId, Identifiable, Node, NodeDefinition}
import sangria.schema._
import user._
import vehicle.ListingService
import vehicle.VehicleGraphQL

case class SecureContext(
  identity: Option[User],
  authService: AuthService,
  listingService: ListingService
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

@Singleton
class GraphQLSchema @Inject() (
  vehicleGraphQL: VehicleGraphQL,
  authGraphQL: AuthGarphQL
) {

  val QueryType = ObjectType(
    "Query",
    fields[SecureContext, Unit](
      Field(
        "test",
        StringType,
        Some("Fetch all users"),
        tags = GraphQLAuthentication.Authorized :: Nil,
        resolve = c => "test"
      )
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[SecureContext, Unit](
      authGraphQL.mutations() ++
        vehicleGraphQL.mutations:_*
//      Field(
//        "createUser",
//        UserType,
//        arguments = NameArg :: AuthProviderArg :: Nil,
//        resolve = c => c.ctx.userRepository.create(c.arg(NameArg), c.arg(AuthProviderArg))
//      )
    )
  )

  val SchemaDefinition = Schema(QueryType, Some(MutationType))
}
