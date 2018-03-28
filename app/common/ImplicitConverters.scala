package common

import java.util.UUID

trait UUIDImplitits {
  implicit def uuidToString(id: UUID): String = id.toString
  implicit def stringToUUid(id: String): String = UUID.fromString(id)
}
