package vehicle

final case class VehicleModelNotFoundException(message: String) extends Exception(message)

final case class UserHasNoBusinessDefined(message: String) extends Exception(message)
