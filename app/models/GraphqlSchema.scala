package models

import sangria.schema._
import sangria.marshalling.playJson._
import user._
import user.User._

case class GraphqlContext (userRepository: UserDAO)

trait GraphqlSchema extends
  AuthGraphQLImplicits {

}

object GraphqlSchema extends GraphqlSchema {
  val NameArg = Argument("name", StringType)
  val AuthProviderArg = Argument("authProvider", AuthProviderSignupDataInputType)

  val QueryType = ObjectType(
    "Query",
    fields[GraphqlContext, Unit](
//       Field(
//         "users",
//         ListType(UserType),
//        Some("Fetch all users"),
//        resolve = c => c.ctx.userRepository.getAll
//       )
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphqlContext, Unit](
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
