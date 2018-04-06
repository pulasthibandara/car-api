package models

import sangria.schema._
import user._

case class SecureContext(identity: Option[User], authService: AuthService)

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
      AuthGraphQL.mutations():_*
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
