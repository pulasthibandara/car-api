package core

import java.sql.Timestamp
import java.time.Instant

import core.database.PgSlickProfile
import play.api.db.slick.HasDatabaseConfigProvider

trait MappedDBTypes extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  implicit val dateTimeColumnType = MappedColumnType.base[Instant, Timestamp](
    dt => Timestamp.from(dt),
    ts => ts.toInstant
  )
}
