package graphql.schema

import java.util.UUID

import business.models.Business
import graphql.GraphQLSchema
import graphql.middleware.GraphQLAuthentication
import graphql.{RelayInterfaceTypes, SecureContext}
import play.api.libs.json.Json
import sangria.execution.deferred.{Fetcher, Relation}
import sangria.macros.derive._
import sangria.relay._
import sangria.schema._
import sangria.marshalling.playJson._
import vehicle._

trait MetaGraphql extends RelayInterfaceTypes
  with MetaGraphQLTypes
  with MetaFetchers { this: GraphQLSchema =>

  def queries: List[Field[SecureContext, Unit]] = List(
    Field(
      "application",
      applicationDataType,
      resolve = c => ApplicationDataType(
        fuelTypes = FuelType.values.toSeq,
        transmissionTypes = TransmissionType.values.toSeq,
        bodyTypes =  BodyType.values.toSeq,
        conditionType = ConditionType.values.toSeq,
      )
    )
  )
}

trait MetaFetchers {
  val modelToMake = Relation[Model, UUID]("model-make", model => Seq(model.makeId))

  val modelFetcher = Fetcher[SecureContext, Model, UUID](
    (ctx, ids) => ctx.taxonomyService.getModelsByIds(ids)
  )

  val modelsByMakeIdsFetcher = Fetcher.relOnly[SecureContext, Model, Model, UUID](
    (ctx, makes) => ctx.taxonomyService.getModelsByMakes(makes(modelToMake))
  )

  val makeFetcher = Fetcher[SecureContext, Make, UUID](
    (ctx, ids) => ctx.taxonomyService.getMakesByIds(ids)
  )
}

trait MetaGraphQLTypes extends LowPriorityVehicleGraphQLImplicits { this: GraphQLSchema =>
  val applicationDataType = deriveObjectType[SecureContext, ApplicationDataType](
    AddFields(
      Field("makes", makeConnectionType, arguments = Connection.Args.All,
        resolve = c => {
          import c.ctx.ec

          Connection.connectionFromFutureSeq(
            c.ctx.taxonomyService.getAllMakes,
            ConnectionArgs(c)
          )
        })
    )
  )
}

case class ApplicationDataType(
  fuelTypes: Seq[FuelType.Value],
  transmissionTypes: Seq[TransmissionType.Value],
  bodyTypes: Seq[BodyType.Value],
  conditionType: Seq[ConditionType.Value]
)
