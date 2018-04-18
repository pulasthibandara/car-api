package user

import com.google.inject.{Inject, Singleton}
import models.SecureContext
import com.mohiva.play.silhouette.api.exceptions.NotAuthorizedException
import play.api.libs.json.Json
import sangria.marshalling.playJson._
import sangria.execution._
import sangria.macros.derive._
import sangria.relay.Connection
import sangria.schema._
import user.User.UserType
import vehicle.Listing

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData
case class SignupData(credentials: Option[AuthProviderCredentials])

case class Business(
  listings: Connection[Listing]
)

@Singleton
class AuthGarphQL @Inject() () extends AuthGraphQLImplicits  {
//  def queries(): List[Field[SecureContext, Unit]] = List(
//    Field(
//      "business",
//
//    )
//  )

  def mutations(): List[Field[SecureContext, Unit]] = List(
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
