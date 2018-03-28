package user

import javax.inject.{Inject, Singleton}
import java.time.Instant

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import common.MappedDBTypes
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class DBPasswordInfo(
  hasher: String,
  password: String,
  salt: Option[String],
  loginInfoId: Long,
  createdAt: Instant
)

trait PasswordInfosTable extends HasDatabaseConfigProvider[JdbcProfile] with MappedDBTypes {
  import profile.api._

  protected class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "PASSWORD_INFO") {
    def hasher = column[String]("HASHER")
    def password = column[String]("PASSWORD")
    def salt = column[Option[String]]("SALT")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")
    def createdAt = column[Instant]("CREATED_AT")

    def * = (hasher, password, salt, loginInfoId, createdAt)  <> ((DBPasswordInfo.apply _).tupled, DBPasswordInfo.unapply)
  }

  val passwordInfos = TableQuery[PasswordInfos]
}

@Singleton
class PasswordInfoDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends DelegableAuthInfoDAO[PasswordInfo]
  with PasswordInfosTable
  with LoginInfosTable
  with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  implicit def dbPasswordInfoToPasswordInfo(db: DBPasswordInfo): PasswordInfo =
    PasswordInfo(db.hasher, db.password, db.salt)

  private def addAction(loginInfo: LoginInfo, authInfo: PasswordInfo) = loginInfoQuery(loginInfo)
    .result
    .head
    .flatMap(dbLoginInfo => passwordInfos +=
      DBPasswordInfo(
        authInfo.hasher,
        authInfo.password,
        authInfo.salt,
        dbLoginInfo.id.get,
        Instant.now()
      )
    )

  private def updateAction(loginInfo: LoginInfo, authInfo: PasswordInfo) = passwordInfos
    .filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))
    .map(dbPasswordInfo => (dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt))
    .update((authInfo.hasher, authInfo.password, authInfo.salt))

  /**
    * Finds the auth info which is linked to the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The found auth info or None if no auth info could be found for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    db.run(passwordInfos
        .filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))
        .result
        .headOption
    ).map(maybeDBPasswordInfo => maybeDBPasswordInfo.map(dbPasswordInfoToPasswordInfo(_)))


  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(addAction(loginInfo, authInfo))
      .map(_ => authInfo)

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(updateAction(loginInfo, authInfo))
      .map(_ => authInfo)

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo The auth info to save.
    * @return The saved auth info.
    */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = loginInfoQuery(loginInfo).joinLeft(passwordInfos).on(_.id === _.loginInfoId)
    val action = query.result.head.flatMap {
      case (_, Some(_)) => updateAction(loginInfo, authInfo)
      case (_, None) => addAction(loginInfo, authInfo)
    }
    db.run(action).map(_ => authInfo)
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(passwordInfos
      .filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))
      .delete
    ).map(_ => ())

}
