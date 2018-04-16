package user

import java.time.Instant

import com.mohiva.play.silhouette.api.LoginInfo
import common.MappedDBTypes
import common.database.PgSlickProfile
import play.api.db.slick.HasDatabaseConfigProvider

case class DBLoginInfo(
  id: Option[Long],
  providerId: String,
  providerKey: String,
  createdAt: Option[Instant]
)

case class DBUserLoginInfo (
  userID: String,
  loginInfoId: Long
)


trait LoginInfosTable extends HasDatabaseConfigProvider[PgSlickProfile] {
  import profile.api._

  protected class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def createdAt = column[Option[Instant]]("created_at")

    def * = (id.?, providerId, providerKey, createdAt).mapTo[DBLoginInfo]
  }

  protected class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "user_login_info") {
    def userID = column[String]("user_id")
    def loginInfoId = column[Long]("login_info_id")

    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  val loginInfos = TableQuery[LoginInfos]
  val userLoginInfos = TableQuery[UserLoginInfos]

  /**
    * A composable query for selection based on loginInfo
    */
  protected def loginInfoQuery(loginInfo: LoginInfo) = loginInfos
    .filter(dbLoginInfo => dbLoginInfo.providerId === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}


