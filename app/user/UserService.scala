package user

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import common.UUIDImplitits

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(
  userDAO: UserDAO,
  passwordInfoDAO: PasswordInfoDAO,
  passwordHasherRegistry: PasswordHasherRegistry)
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
    * Sign-up the user if not exists. Otherwise throw an error
    */
  def signUp(firstName: String, lastName: String, email: String, password: String): Future[User] = {
      val userId = UUID.randomUUID()
      val loginInfo = LoginInfo(CredentialsProvider.ID, userId)
      val user = User(userId, firstName, lastName, email, Some(loginInfo))

      userDAO.find(email).flatMap {
        case Some(_) => throw new UserAlreadyExists(user.email)
        case None => createUser(user, password)
      }
  }

  /**
    * Creates the user and the relevant authInfo
    */
  private def createUser(user: User, password: String): Future[User] =  {
    val authInfo = passwordHasherRegistry.current.hash(password)

    for {
      user <- userDAO.save(user)
      _ <- passwordInfoDAO.add(user.loginInfo.get, authInfo)
    } yield user
  }
}

class UserAlreadyExists(email: String)
  extends Exception(s"User with email: ($email) already exists!")
