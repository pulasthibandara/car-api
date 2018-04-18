package business.models

import java.util.UUID

case class Business(id: UUID, name: String, subdomain: Option[String], domain: Option[String])
