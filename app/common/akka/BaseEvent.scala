package common.akka

import java.time.Instant
import java.util.UUID

trait BaseEvent {
  val id: UUID = UUID.randomUUID()
  val timestamp: Instant = Instant.now()
}
