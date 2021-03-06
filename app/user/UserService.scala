package user

import javax.inject.{Inject, Singleton}

import business.models.Business
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import core.UUIDImplitits

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(
  userDAO: UserDAO,
)
  (implicit ec: ExecutionContext) extends IdentityService[User]
  with UUIDImplitits {

  /**
    * Retrieves an identity that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve an identity.
    * @return The retrieved identity or None if no identity could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
    * Adds a business to a given user.
    */
  def addBusiness(user: User, business: Business): Future[User] =
    userDAO.updateBusiness(user, business.id)
}

