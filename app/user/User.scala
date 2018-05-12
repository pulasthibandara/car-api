package user

import java.time.Instant
import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import graphql.types.CommonGraphQLScalarTypes
import sangria.ast
import sangria.macros.derive._
import sangria.schema.ScalarType
import sangria.validation.ValueCoercionViolation

import scala.util.{Failure, Success, Try}

case class User (
  id: UUID,
  firstName: String,
  lastName: String,
  email: String,
  businessId: Option[UUID] = None,
  loginInfo: Option[LoginInfo] = None,
  createdAt: Option[Instant] = None
) extends Identity

object User extends  CommonGraphQLScalarTypes {
  val UserType = deriveObjectType[Unit, User](
    ExcludeFields("loginInfo", "id")
  )
}
