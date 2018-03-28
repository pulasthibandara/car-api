package user

import java.time.Instant
import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
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
  loginInfo: Option[LoginInfo] = None,
  createdAt: Option[Instant] = None
) extends Identity

object User {
  case object DateTimeCoerceViolation extends ValueCoercionViolation("Date value expected")

  def parseDateTime(s: String) = Try(Instant.parse(s)) match {
    case Success(date) => Right(date)
    case Failure(_) => Left(DateTimeCoerceViolation)
  }

  implicit val DateTimeType = ScalarType[Instant]("DateTime",
    coerceUserInput = {
      case s: String => parseDateTime(s)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceOutput = (dt, _) => ast.StringValue(dt.toString),
    coerceInput = {
      case ast.StringValue(s, _, _, _, _) => parseDateTime(s)
      case _ => Left(DateTimeCoerceViolation)
    }
  )

  val UserType = deriveObjectType[Unit, User](
    ExcludeFields("loginInfo", "id")
  )
}
