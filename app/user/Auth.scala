package user

import models.SecureContext
import com.mohiva.play.silhouette.api.exceptions.NotAuthorizedException
import play.api.libs.json.Json
import sangria.execution._
import sangria.macros.derive.deriveInputObjectType
import sangria.schema._
import sangria.marshalling.playJson._
import user.User.UserType

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData
case class SignupData(credentials: Option[AuthProviderCredentials])

// Field Tags

object AuthGraphQL {
  implicit val jsonFormatCredentials = Json.format[AuthProviderCredentials]
  implicit val jsonFormatSignupData = Json.format[SignupData]
  implicit val jsonReadSignupData = Json.reads[SignupData]

  implicit  val AuthProviderCredentialsInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials]()
  implicit val SignupDataInputType: InputObjectType[SignupData] =
    deriveInputObjectType[SignupData]()

  val signUpDataArg = Argument("authProvider", SignupDataInputType)
  val emailArg = Argument("email", StringType)
  val passwordArg = Argument("password", StringType)

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

  case object Authorized extends FieldTag

  object SecurityEnforcer extends Middleware[SecureContext] with MiddlewareBeforeField[SecureContext] {
    type QueryVal = Unit
    type FieldVal = Unit

    override def beforeQuery(context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

    override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SecureContext, _, _]): Unit = ()

    override def beforeField(
      queryVal: QueryVal,
      mctx: MiddlewareQueryContext[SecureContext, _, _],
      ctx: Context[SecureContext, _]
    ): BeforeFieldResult[SecureContext, Unit] = {
      val requireAuth = ctx.field.tags contains Authorized

      if (requireAuth)
        ctx.ctx.identity.fold(throw new NotAuthorizedException("Needs Authentication!"))(identity)

      continue
    }
  }

}
