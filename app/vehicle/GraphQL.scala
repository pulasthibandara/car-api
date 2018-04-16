package vehicle

import sangria.schema._
import sangria.marshalling.playJson._
import _root_.models.SecureContext
import user.AuthGraphQL

object GraphQL extends VehicleJsonImplicits {
  val listingIdType = Argument("id", OptionInputType(UUIDType))
  val listingModelId = Argument("modelId", UUIDType)
  val listingTitle = Argument("title", StringType)
  val listingDescription = Argument("description", StringType)
  val listingYear = Argument("year", OptionInputType(IntType))
  val listingKilometers = Argument("kilometers", OptionInputType(LongType))
  val listingColor = Argument("color", OptionInputType(StringType))
  val listingBodyType = Argument("bodyType", OptionInputType(BodyTypeType))
  val listingFuelType = Argument("fuelType", OptionInputType(FuelTypeType))
  val listingTransmissionType = Argument("transmissionType", OptionInputType(TransmissionTypeType))
  val listingCylinders = Argument("cylinders", OptionInputType(IntType))
  val listingEngineSize = Argument("engineSize", OptionInputType(IntType))
  val listingConditionType = Argument("conditionType", OptionInputType(ConditionTypeType))
  val listingFeatures = Argument("features", ListInputType(StringType))

  val mutations: List[Field[SecureContext, Unit]] = List(
    Field(
      "createListing",
      ListingType,
      tags = AuthGraphQL.Authorized :: Nil,
      arguments = List(
        listingIdType,
        listingModelId,
        listingTitle,
        listingDescription,
        listingYear,
        listingKilometers,
        listingColor,
        listingBodyType,
        listingFuelType,
        listingTransmissionType,
        listingCylinders,
        listingEngineSize,
        listingConditionType,
        listingFeatures
      ),
      resolve = c => {
        c.ctx.listingService.createListing(
          c arg listingIdType,
          c.ctx.identity.get.id,
          c arg listingModelId,
          c arg listingTitle,
          c arg listingDescription,
          c arg listingYear,
          c arg listingKilometers,
          c arg listingColor,
          c arg listingBodyType,
          c arg listingFuelType,
          c arg listingTransmissionType,
          c arg listingCylinders,
          c arg listingEngineSize,
          c arg listingConditionType,
          c.args.arg(listingFeatures).asInstanceOf[Seq[String]].toList
        )
      }
    )
  )
}
