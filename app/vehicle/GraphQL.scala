package vehicle

import sangria.execution._
import sangria.macros.derive.deriveInputObjectType
import sangria.schema._
import sangria.marshalling.playJson._
import _root_.models.SecureContext
import user.AuthGraphQL

object GraphQL extends VehicleImplicits {
  val listingIdArg = Argument("id", OptionInputType(UUIDType))
  val listingTitleArg = Argument("title", StringType)
  val listingDescriptionArg = Argument("description", StringType)
  val listingModelArg = Argument("modelId", UUIDType)

  val mutations: List[Field[SecureContext, Unit]] = List(
    Field(
      "createListing",
      ListingType,
      tags = AuthGraphQL.Authorized :: Nil,
      arguments = listingIdArg :: listingTitleArg :: listingDescriptionArg :: listingModelArg :: Nil,
      resolve = c => c.ctx.listingService.createListing(
        c.args.arg(listingIdArg),
        c.args.arg(listingTitleArg),
        c.args.arg(listingDescriptionArg),
        c.args.arg(listingModelArg),
        c.ctx.identity.get.id)
    )
  )
}
