package common

import java.sql.Timestamp
import java.time.Instant

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait MappedDBTypes extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  implicit val dateTimeColumnType = MappedColumnType.base[Instant, Timestamp](
    dt => Timestamp.from(dt),
    ts => ts.toInstant
  )
}
