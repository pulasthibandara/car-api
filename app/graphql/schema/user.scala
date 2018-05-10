package graphql.schema

import graphql.{GraphQLSchema, SecureContext}
import graphql.utils.RelayUtils
import play.api.libs.json._
import sangria.macros.derive._
import sangria.marshalling.playJson._
import sangria.relay.Mutation
import sangria.schema._
import user.User
import user.User.UserType

trait AuthProviderData
case class AuthProviderCredentials(firstName: String, lastName: String, email: String, password: String)
  extends AuthProviderData
case class SignUpPayload(user: User, clientMutationId: Option[String]) extends Mutation

case class LoginInput(email: String, password: String)

trait AuthGraphQL extends AuthGraphQLImplicits  { this: GraphQLSchema =>

  def mutations: List[Field[SecureContext, Unit]] = List(
    RelayUtils.createSimpleMutation(
      "signUp",
      UserType,
      OptionInputType(AuthProviderCredentialsInputType),
      resolve = (input: Option[AuthProviderCredentials], c) => input match {
        case Some (AuthProviderCredentials (firstName, lastName, email, password)) =>
          c.ctx.authService.signUp (firstName, lastName, email, password)
        case _ => throw new Exception ("Provider not configured.")
      }
    ),
    RelayUtils.createSimpleMutation(
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

  implicit  val AuthProviderCredentialsInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials]()
  implicit val loginInputType: InputObjectType[LoginInput] =
    deriveInputObjectType[LoginInput]()

  implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]]{
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) ⇒ implicitly[Writes[T]].writes(t)
      case None ⇒ JsNull
    }
  }
}

trait AuthGraphQLTypes {

}
