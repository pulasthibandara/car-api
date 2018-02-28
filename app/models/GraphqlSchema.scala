package models

import sangria.schema._

case class GraphqlContext (userRepository: UserRepository)

object GraphqlSchema {
  val QueryType = ObjectType(
    "Query",
    fields[GraphqlContext, Unit](
      Field(
        "users",
        ListType(graphql.UserType),
        Some("Fetch all users"),
        resolve = c => c.ctx.userRepository.getAll
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
