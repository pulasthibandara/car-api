package graphql.schema

import graphql.{GraphQLSchema, SecureContext}
import play.api.libs.json.Json
import sangria.macros.derive._
import sangria.marshalling.playJson._
import sangria.schema._
import user.User.UserType

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData
case class SignupData(credentials: Option[AuthProviderCredentials])


trait AuthGarphQL extends AuthGraphQLImplicits  { this: GraphQLSchema =>
//  def queries(): List[Field[SecureContext, Unit]] = List(
//    Field(
//      "business",
//
//    )
//  )

  def mutations: List[Field[SecureContext, Unit]] = List(
    Field(
      "signUp",
      UserType,
      arguments = signUpDataArg :: Nil,
      resolve = c => c.args.arg(signUpDataArg) match {
        case SignupData(Some(AuthProviderCredentials(firstName, lastName, email, password))) =>
          c.ctx.authService.signUp(firstName, lastName, email, password)
        case _ => throw new Exception("Provider not configured.")
      }
    ),
    Field(
      "login",
      StringType,
      arguments = emailArg :: passwordArg :: Nil,
      resolve = c => c.ctx.authService.authenticate(c arg emailArg, c arg passwordArg)
    )
  )
}

trait AuthGraphQLImplicits {
  implicit val authProviderCredentialsFormats = Json.format[AuthProviderCredentials]
  implicit val signUpDataFormats = Json.format[SignupData]

  implicit  val AuthProviderCredentialsInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials]()
  implicit val SignupDataInputType: InputObjectType[SignupData] =
    deriveInputObjectType[SignupData]()

  protected val signUpDataArg = Argument("authProvider", SignupDataInputType)
  protected val emailArg = Argument("email", StringType)
  protected val passwordArg = Argument("password", StringType)
}

trait AuthGraphQLTypes {

}
