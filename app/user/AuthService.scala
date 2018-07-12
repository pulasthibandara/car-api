package user

import java.util.UUID

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, CredentialsProvider}
import core.UUIDImplitits
import user.models.UserCreated

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthService @Inject()(
  userDAO: UserDAO,
  authInfoRepository: AuthInfoRepository,
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

    val futUser = userDAO.find(email).flatMap {
      case Some(_) => throw new UserAlreadyExists(user.email)
      case None => createUser(user, password)
    }

    futUser.foreach { case u => actorSystem
      .eventStream
      .publish(UserCreated(u))
    }

    futUser
  }

  /**
    * Creates the user and the relevant authInfo
    */
  private def createUser(user: User, password: String): Future[User] = {
    val authInfo = passwordHasherRegistry.current.hash(password)

    for {
      user <- userDAO.save(user)
      _ <- authInfoRepository.add(user.loginInfo.get, authInfo)
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
        case Some(_) => createToken(loginInfo)
        case None => throw new IdentityNotFoundException("User not found.")
      }
    } yield token
  }

  /**
    * Creates an authentication toekn.
    */
  def createToken(loginInfo: LoginInfo): Future[String] = for {
    authenticator <- silhouette.env.authenticatorService.create(loginInfo)(null)
    token <- silhouette.env.authenticatorService.init(authenticator)(null)
  } yield token

  /**
    * Creates a user from a social profile.
    */
  def createUser[T <: AuthInfo](profile: CommonSocialProfile, authInfo: T): Future[User] = for {
    user <- userDAO.find(profile.loginInfo) flatMap {
      // User already exists, just return the existing record
      case Some(user) => Future.successful(user)
      case None => userDAO.save(User(
        id = UUID.randomUUID(),
        firstName = profile.firstName.getOrElse(profile.email.get),
        lastName = profile.lastName.getOrElse(""),
        email = profile.email.get,
        businessId = None,
        loginInfo = Some(profile.loginInfo)
      ))
    }
    _ <- authInfoRepository.add(user.loginInfo.get, authInfo)
  } yield user
}

class UserAlreadyExists(email: String)
  extends Exception(s"User with email: ($email) already exists!")
