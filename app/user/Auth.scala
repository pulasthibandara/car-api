package user

import play.api.libs.json.Json
import sangria.macros.derive.{InputObjectTypeName, deriveInputObjectType}
import sangria.schema.InputObjectType

case class AuthProviderCredentials(email: String, password: String)
case class AuthProviderSignupData(credentials: AuthProviderCredentials)

trait AuthGraphQLImplicits {
  implicit val jsonFormatCredentials = Json.format[AuthProviderCredentials]

  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderCredentials] =
    deriveInputObjectType[AuthProviderCredentials](
      InputObjectTypeName("AUTH_PROVIDER_EMAIL")
    )

  implicit val jsonFormatSignupData = Json.format[AuthProviderSignupData]

  implicit val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] =
    deriveInputObjectType[AuthProviderSignupData]()
}
