package graphql.schema

import graphql.{GraphQLSchema, SecureContext}
import graphql.utils.RelayUtils._
import play.api.libs.json._
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.marshalling.FromInput.InputObjectResult
import sangria.marshalling.playJson._
import sangria.relay.Mutation
import sangria.schema._
import sangria.util.tag.@@
import user.User
import user.User.UserType

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData

case class LoginInput(email: String, password: String)

case class SignUpRes(token: String, user: User)

trait AuthGraphQL extends AuthGraphQLImplicits  { this: GraphQLSchema =>

  def mutations: List[Field[SecureContext, Unit]] = List(
    createSimpleMutation(
      "signUp",
      signUpResType,
      OptionInputType(AuthProviderCredentialsInputType),
      resolve = (input: Option[AuthProviderCredentials], c) => {
        implicit val ec = c.ctx.ec

        input match {
          case Some (AuthProviderCredentials (firstName, lastName, email, password)) => for {
            user <- c.ctx.authService.signUp (firstName, lastName, email, password)
            token <- c.ctx.authService.createToken(user.loginInfo.get)
          } yield SignUpRes(token, user)
          case _ => throw new Exception ("Provider not configured.")
        }
      }
    ),
    createSimpleMutation(
      "login",
      StringType,
      loginInputType,
      resolve = (input: LoginInput, c) => c.ctx.authService.authenticate(input.email, input.password)
    )
  )
}

trait AuthGraphQLImplicits {
  implicit val authProviderCredentialsFormats: OFormat[AuthProviderCredentials] = Json.format[AuthProviderCredentials]
  implicit val loginInputFormats: OFormat[LoginInput] = Json.format[LoginInput]
  implicit val signUpResFormats: OFormat[SignUpRes] = Json.format[SignUpRes]

  implicit  val AuthProviderCredentialsInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials]()
  implicit val loginInputType: InputObjectType[LoginInput] =
    deriveInputObjectType[LoginInput]()
  implicit val signUpResType: OutputType[SignUpRes] = deriveObjectType[SecureContext, SignUpRes]()
}

trait AuthGraphQLTypes {

}
