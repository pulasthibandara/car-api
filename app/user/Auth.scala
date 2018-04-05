package user

import com.google.inject.Singleton
import models.GraphqlContext
import play.api.libs.json.Json
import sangria.macros.derive.deriveInputObjectType
import sangria.schema._
import sangria.marshalling.playJson._
import user.User.UserType

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData
case class SignupData(credentials: Option[AuthProviderCredentials])

object AuthGraphQL {
  implicit val jsonFormatCredentials = Json.format[AuthProviderCredentials]
  implicit val jsonFormatSignupData = Json.format[SignupData]
  implicit val jsonReadSignupData = Json.reads[SignupData]

  implicit  val AuthProviderCredentialsInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials]()
  implicit val SignupDataInputType: InputObjectType[SignupData] =
    deriveInputObjectType[SignupData]()

  val signUpDataArg = Argument("authProvider", SignupDataInputType)

  def mutations(): List[Field[GraphqlContext, Unit]] = List(
    Field(
      "signUp",
      UserType,
      arguments = signUpDataArg :: Nil,
      resolve = c => c.args.arg(signUpDataArg) match {
        case SignupData(Some(AuthProviderCredentials(firstName, lastName, email, password))) =>
          c.ctx.userService.signUp(firstName, lastName, email, password)
      }
    )
  )



}
