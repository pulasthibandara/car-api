package vehicle

import sangria.execution._
import sangria.macros.derive.deriveInputObjectType
import sangria.schema._
import sangria.marshalling.playJson._
import models.SecureContext
import common.CommonGraphQLScalarTypes._

object GraphQL {
  val listingIdArg = Argument("id", OptionInputType(UUIDType))
  val listingNameArg = Argument("title", StringType)
  val listingDescriptionArg = Argument("description", StringType)
  val listingModelArg = Argument("modelId", UUIDType)

  val mutations: List[Field[SecureContext, Unit]] = List(
    Field(
      "addCar",
      StringType,
      arguments = listingIdArg :: listingNameArg :: listingDescriptionArg :: listingModelArg :: Nil,
      resolve = c => ???
    )
  )
}
