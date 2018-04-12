package models

import sangria.schema._
import user._
import vehicle.ListingService
import vehicle.{ GraphQL => VehicleGraphql }

case class SecureContext(
  identity: Option[User],
  authService: AuthService,
  listingService: ListingService
)

object GraphqlSchema {
  val QueryType = ObjectType(
    "Query",
    fields[SecureContext, Unit](
      Field(
        "test",
        StringType,
        Some("Fetch all users"),
        tags = AuthGraphQL.Authorized :: Nil,
        resolve = c => "test"
      )
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[SecureContext, Unit](
      AuthGraphQL.mutations() ++
      VehicleGraphql.mutations:_*
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
