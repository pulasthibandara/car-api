package models

import java.time.Instant
import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private implicit val dateTimeColumnType = MappedColumnType.base[Instant, Timestamp](
    dt => Timestamp.from(dt),
    ts => ts.toInstant
  )

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def email = column[String]("EMAIL")
    def password = column[String]("PASSWORD")
    def createdAt = column[Instant]("CREATED_AT")

    def * = (id, firstName, lastName, email, password, createdAt).mapTo[User]
  }

  private val users = TableQuery[UsersTable]

  def getAll: Future[Seq[User]] = db.run(users.result)
}
