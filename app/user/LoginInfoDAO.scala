package user

import java.time.Instant

import com.mohiva.play.silhouette.api.LoginInfo
import common.MappedDBTypes
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

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


trait LoginInfosTable extends HasDatabaseConfigProvider[JdbcProfile] with MappedDBTypes {
  import profile.api._

  protected class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "LOGIN_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("PROVIDER_ID")
    def providerKey = column[String]("PROVIDER_KEY")
    def createdAt = column[Option[Instant]]("CREATED_AT")

    def * = (id.?, providerId, providerKey, createdAt).mapTo[DBLoginInfo]
  }

  protected class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USER_LOGIN_INFO") {
    def userID = column[String]("USER_ID")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")

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


