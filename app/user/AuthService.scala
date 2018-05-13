package user

import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import core.UUIDImplitits
import user.models.UserCreated

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthService @Inject()(
  userDAO: UserDAO,
  passwordInfoDAO: PasswordInfoDAO,
  passwordHasherRegistry: PasswordHasherRegistry,
  credentialsProvider: CredentialsProvider,
  userService: UserService,
  silhouette: Silhouette[DefaultEnv],
  actorSystem: ActorSystem
)(implicit ec: ExecutionContext) extends UUIDImplitits {

  /**
    * Sign-up the user if not exists. Otherwise throw an error
    */
  def signUp(firstName: String, lastName: String, email: String, password: String): Future[User] = {
    val userId = UUID.randomUUID()
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    val user = User(userId, firstName, lastName, email, None, Some(loginInfo))

    val maybeCreatedUser = userDAO.find(email).flatMap {
      case Some(_) => throw new UserAlreadyExists(user.email)
      case None => createUser(user, password)
    }

    maybeCreatedUser.foreach { case u => actorSystem
      .eventStream
      .publish(UserCreated(u))
    }

    maybeCreatedUser
  }

  /**
    * Creates the user and the relevant authInfo
    */
  private def createUser(user: User, password: String): Future[User] = {
    val authInfo = passwordHasherRegistry.current.hash(password)

    for {
      user <- userDAO.save(user)
      _ <- passwordInfoDAO.add(user.loginInfo.get, authInfo)
    } yield user
  }

  /**
    * Authenticate and return a token
    */
  def authenticate(email: String, password: String): Future[String] = {
    val credentials = Credentials(email, password)
    for {
      loginInfo <- credentialsProvider.authenticate(credentials)
      user <- userService.retrieve(loginInfo)
      token <- user match {
        case Some(_) => silhouette.env.authenticatorService.create(loginInfo)(null)
          .flatMap(silhouette.env.authenticatorService.init(_)(null))
        case None => throw new IdentityNotFoundException("User not found.")
      }
    } yield token
  }
}

class UserAlreadyExists(email: String)
  extends Exception(s"User with email: ($email) already exists!")
