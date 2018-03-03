package user

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}

import models.GraphqlSchema.AuthProviderSignupData
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

    def * = (id, firstName, lastName, email, password, createdAt) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UsersTable]

  def getAll: Future[Seq[User]] = db.run(users.result)

  def create(name: String, authProvider: AuthProviderSignupData): Future[User] = {
    val newUser = User(randomUUID.toString, name, name, authProvider.email.email, authProvider.email.password, Instant.now)

    db.run {
      users += newUser
    } map {
      case _ => newUser
    }
  }
}
