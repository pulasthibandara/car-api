package common

import java.util.UUID

import sangria.schema._

object CommonGraphQLScalarTypes {
  val UUIDType = ScalarAlias[UUID, String](StringType,
    toScalar = _.toString,
    fromScalar = idString ⇒ try Right(UUID.fromString(idString)) catch {
      case _: IllegalArgumentException ⇒ Left(IDViolation)
    })
}


