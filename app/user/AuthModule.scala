package user

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import play.api.libs.concurrent.AkkaGuiceSupport
import user.events.BusinessListener

import scala.concurrent.ExecutionContext

class AuthModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    // Crypto
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))

    // Social + Password
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]

    // Akka actors
    // -- event listeners
    bindActor[BusinessListener]("user:business-event-listener")
  }

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient)(implicit ec: ExecutionContext): HTTPLayer =
    new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[JWTAuthenticator],
    eventBus: EventBus) (implicit ec: ExecutionContext): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val settings = JcaCrypterSettings(configuration.underlying.as[String]("silhouette.jwt.authenticator.sharedSecret"))
    new JcaCrypter(settings)
  }

  /**
    * Provides the random ID generator
    * @return The random ID generator instance
    */
  @Provides
  def provideIDGenerator()(implicit ec: ExecutionContext): IDGenerator = {
    new SecureRandomIDGenerator()
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo])
    (implicit ec: ExecutionContext): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordInfoDAO)

  /**
    * Provides the authenticator service.
    *
    * @param crypter The crypter implementation.
    * @param fingerprintGenerator The fingerprint generator implementation.
    * @param idGenerator The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(
    crypter: Crypter,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock)(implicit  ec: ExecutionContext): AuthenticatorService[JWTAuthenticator] = {

    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, authenticatorEncoder, idGenerator, clock)
  }

  /**
    * Provides the password hasher registry.
    *
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Nil)
  }

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository The auth info repository implementation.
    * @param passwordHasherRegistry The password hasher registry.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry)(implicit ec: ExecutionContext): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

}
