package graphql

import business.models.Business
import business.services.BusinessService
import graphql.schema.{AuthGraphQL, BusinessGraphQL, MetaGraphql, VehicleGraphQL}
import sangria.execution.deferred.{DeferredResolver, Fetcher}
import sangria.relay.{GlobalId, Identifiable, Node, NodeDefinition}
import sangria.schema._
import user._
import vehicle.services.{ListingService, TaxonomyService}

import scala.concurrent.ExecutionContext

case class SecureContext(
  identity: Option[User],
  business: Option[Business],
  authService: AuthService,
  listingService: ListingService,
  businessService: BusinessService,
  taxonomyService: TaxonomyService,
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

trait GraphQLSchema extends
  VehicleGraphQL with
  MetaGraphql with
  BusinessGraphQL with
  AuthGraphQL {

  override def queries: List[Field[SecureContext, Unit]] =
    super[MetaGraphql].queries ++
    super[BusinessGraphQL].queries

  val QueryType: ObjectType[SecureContext, Unit] = ObjectType(
    "Query",
    fields[SecureContext, Unit](queries: _*)
  )

  override def mutations: List[Field[SecureContext, Unit]] =
    super[AuthGraphQL].mutations ++
    super[VehicleGraphQL].mutations ++
    super[BusinessGraphQL].mutations

  val MutationType: ObjectType[SecureContext, Unit] = ObjectType(
    "Mutation",
    fields[SecureContext, Unit](mutations: _*)
  )

  val deferredResolvers: DeferredResolver[SecureContext] =
    DeferredResolver.fetchers(modelFetcher, makeFetcher, modelsByMakeIdsFetcher)

  val SchemaDefinition = Schema(QueryType, Some(MutationType))
}

object GraphQLSchema extends GraphQLSchema
