package graphql.types

import java.time.Instant
import java.util.UUID

import sangria.ast
import sangria.schema._
import sangria.validation.ValueCoercionViolation

import scala.util.{Failure, Success, Try}

case object IDViolation extends ValueCoercionViolation("Invalid ID")
case object DateTimeCoerceViolation extends ValueCoercionViolation("Date value expected")

trait CommonGraphQLScalarTypes {
  def parseDateTime(s: String) = Try(Instant.parse(s)) match {
    case Success(date) => Right(date)
    case Failure(_) => Left(DateTimeCoerceViolation)
  }

  implicit val UUIDType: ScalarAlias[UUID, String] = ScalarAlias[UUID, String](StringType,
    toScalar = _.toString,
    fromScalar = idString ⇒ try Right(UUID.fromString(idString)) catch {
      case _: IllegalArgumentException ⇒ Left(IDViolation)
    })

  implicit val DateTimeType: ScalarType[Instant] = ScalarType[Instant]("DateTime",
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
}


