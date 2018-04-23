package user

import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import common.MappedDBTypes
import common.database.PgSlickProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

case class DBUser (id: String, firstName: String, lastName: String, email: String, businessId: Option[UUID], createdAt: Instant)

trait UserTable extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  implicit def toUser(user: DBUser, loginInfo: Option[LoginInfo] = None): User =
    User(UUID.fromString(user.id), user.firstName, user.lastName, user.email, None, loginInfo, Some(user.createdAt))

  protected class UsersTable(tag: Tag) extends Table[DBUser](tag, "users") {
    def id = column[String]("id", O.PrimaryKey)
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def email = column[String]("email")
    def businessId = column[Option[UUID]]("business_id")
    def createdAt = column[Instant]("created_at")

    def * = (id, firstName, lastName, email, businessId, createdAt) <> ((DBUser.apply _).tupled, DBUser.unapply)
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
      businessId = user.businessId,
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
          user.businessId,
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

  /**
    * Adds a business to the user.
    */
  def updateBusiness(user: User, businessId: UUID): Future[User] = db.run {
    users.filter(_.id === user.id.toString).map(_.businessId)
      .update(Some(businessId))
  }.map(_ => user.copy(businessId = Some(businessId)))
}
