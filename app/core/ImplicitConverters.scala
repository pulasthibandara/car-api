package core

import java.util.UUID

trait UUIDImplitits {
  implicit def uuidToString(id: UUID): String = id.toString
  implicit def stringToUUid(id: String): String = UUID.fromString(id)
}

object TypeTransformer {
  implicit class ToOther[A](from: A) {
    def transformTo[B](implicit transformer: A => B): B = transformer(from)
  }
}
