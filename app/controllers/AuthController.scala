package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider, SocialProviderRegistry}
import core.Logger
import play.api.mvc.{AbstractController, ControllerComponents}
import user.{AuthService, DefaultEnv}

import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authService: AuthService,
  socialProviderRegistry: SocialProviderRegistry,
) (implicit ec: ExecutionContext) extends AbstractController(components) with Logger {

  def externalAuth(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- authService.createUser(profile, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            value <- silhouette.env.authenticatorService.init(authenticator)
          } yield Ok(value)

        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        // decide what to do when this fails
        logger.info(s"Authentication failed ($provider):", e)
        Unauthorized("Authentication failed.")
    }
  }

}
