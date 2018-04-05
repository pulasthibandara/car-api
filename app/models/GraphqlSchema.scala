package models

import sangria.schema._
import user._

case class GraphqlContext (userService: UserService)

object GraphqlSchema {
  val QueryType = ObjectType(
    "Query",
    fields[GraphqlContext, Unit](
       Field(
         "test",
         StringType,
        Some("Fetch all users"),
        resolve = c => "test"
       )
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphqlContext, Unit](
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
