package models

import sangria.schema._
import sangria.macros.derive._
import play.api.libs.json._
import sangria.marshalling.playJson._
import user._
import User._

case class GraphqlContext (userRepository: UserRepository)

object GraphqlSchema {
  val QueryType = ObjectType(
    "Query",
    fields[GraphqlContext, Unit](
      Field(
        "users",
        ListType(UserType),
        Some("Fetch all users"),
        resolve = c => c.ctx.userRepository.getAll
      )
    )
  )

  case class AuthProviderEmail(email: String, password: String)

  object AuthProviderEmail {
    implicit val jsonFormat = Json.format[AuthProviderEmail]

    implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] =
      deriveInputObjectType[AuthProviderEmail](
        InputObjectTypeName("AUTH_PROVIDER_EMAIL")
      )
  }

  case class AuthProviderSignupData(email: AuthProviderEmail)

  object AuthProviderSignupData {
    implicit val jsonFormat = Json.format[AuthProviderSignupData]

    implicit val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] =
      deriveInputObjectType[AuthProviderSignupData]()
  }

  import AuthProviderSignupData._

  val NameArg = Argument("name", StringType)
  val AuthProviderArg = Argument("authProvider", AuthProviderSignupDataInputType)

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphqlContext, Unit](
      Field(
        "createUser",
        UserType,
        arguments = NameArg :: AuthProviderArg :: Nil,
        resolve = c => c.ctx.userRepository.create(c.arg(NameArg), c.arg(AuthProviderArg))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType, Some(MutationType))
}
