package user

import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import common.MappedDBTypes
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class DBUser (id: String, firstName: String, lastName: String, email: String, createdAt: Instant)

trait UserTable extends HasDatabaseConfigProvider[JdbcProfile] with MappedDBTypes {
  import profile.api._

  implicit def toUser(user: DBUser, loginInfo: Option[LoginInfo] = None): User =
    User(UUID.fromString(user.id), user.firstName, user.lastName, user.email, loginInfo, Some(user.createdAt))

  protected class UsersTable(tag: Tag) extends Table[DBUser](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def email = column[String]("EMAIL")
    def createdAt = column[Instant]("CREATED_AT")

    def * = (id, firstName, lastName, email, createdAt) <> ((DBUser.apply _).tupled, DBUser.unapply)
  }

  val users = TableQuery[UsersTable]
}

@Singleton
class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  (implicit ec: ExecutionContext) extends UserTable
  with LoginInfosTable
  with PasswordInfosTable {
  import profile.api._

  /**
    * creates a new user, and returns a user instance.
    *
    * @return The new user instance.
    */
  def save(user: User): Future[User] = {
    val dbUser: DBUser = DBUser(
      id = randomUUID.toString,
      firstName = user.firstName,
      lastName = user.lastName,
      email = user.email,
      createdAt = Instant.now
    )
    val dbLoginInfo: DBLoginInfo = user.loginInfo
      .map(l => DBLoginInfo(None, l.providerID, l.providerKey, None))
      .get

    val createAction = for {
      _ <- users += dbUser
      loginInfoId <- loginInfos += dbLoginInfo
      _ <- userLoginInfos += DBUserLoginInfo(dbUser.id, loginInfoId)
    } yield user

    db.run { createAction } map { _ => user }
  }

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- userLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- users.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(
          UUID.fromString(user.id),
          user.firstName,
          user.lastName,
          user.email,
          Some(loginInfo),
          Some(user.createdAt)
        )
      }
    }
  }

  /**
    * Finds user by email as email is unique.
    * @return maybe user.
    */
  def find(email: String): Future[Option[User]] = {
    db.run { users.filter(_.email === email).result.headOption }
      .map { _.map(toUser(_)) }
  }
}
