package no.digipost.labs.login

import no.digipost.labs.oauth._
import org.scalatra.Ok
import no.digipost.labs.{DigipostLabsStack, Settings}
import no.digipost.labs.openid.OpenIdConsumer
import no.digipost.labs.security.AuthenticatedUserSupport
import no.digipost.labs.util.Logging
import no.digipost.labs.users.{DbUser, UsersRepository}
import no.digipost.labs.users.Converters._
import scala.util.Success
import no.digipost.labs.oauth.OAuthSettings
import scala.util.Failure
import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime
import ExecutionContext.Implicits.global

class SessionsResource(settings: Settings, loginWithDigipost: LoginWithDigipost, loginWithOpenId: OpenIdConsumer, userRepository: UsersRepository) extends DigipostLabsStack with AuthenticatedUserSupport with Logging {

  final val SETTINGS_KEY = "oauth_settings"

  /**
   * Initiate oauth authentication
   */
  get("/") {
    params.get("returnUrl").foreach(url => session.put("returnUrl", url))
    val oauthSettings = OAuthSettings(
      settings.oauthClientId,
      settings.oauthSecret,
      settings.oauthRedirectUrl,
      loginWithDigipost.randomNonce,
      settings.oauthDigipostHost,
      settings.proxy)
    session.put(SETTINGS_KEY, oauthSettings)
    redirect(oauthSettings.authorizeUrl)
  }

  /**
   * Return url for oauth service
   * User will be redirected here
   */
  get("/oauth") {
    val digipostUser = for {
      oauthSettings <- session.getAs[OAuthSettings](SETTINGS_KEY)
      state <- params.get("state") if state == oauthSettings.state
      code <- params.get("code")
    } yield loginWithDigipost.loginWithDigipost(code, oauthSettings)

    digipostUser.flatMap { u =>
      userRepository.findByDigipostId(u.id) match {
        case Some(dbUser) => {
          val updatedDbUser = userRepository.update(digipostUserToDbUser(u), dbUser._id.toHexString)
          updatedDbUser.map(createSessionUserAndUpdateLastLogin)
        }
        case None => userRepository.insert(digipostUserToDbUser(u)).map(createSessionUserAndUpdateLastLogin)
      }
    } match {
      case None => redirect("/")
      case Some(dpUser) =>
        session.put(SessionUser.sessionKey, dpUser)
        redirect(session.getAs[String]("returnUrl").getOrElse("/"))
    }
  }

  /**
   * Log out
   */
  delete("/") {
    debug("logging out")
    session.remove(SessionUser.sessionKey)
  }

  /**
   * Initiate openid auth using google
   */
  get("/google") {
    openIdDiscoveryAndRedirect(OpenIdConsumer.GOOGLE_ENDPOINT)
  }

  /**
   * Initiate openid auth using yahoo
   */
  get("/yahoo") {
    openIdDiscoveryAndRedirect(OpenIdConsumer.YAHOO_ENDPOINT)
  }

  /**
   * Initiate generic openid auth
   */
  get("/openid") {
    params.get("endpoint") match {
      case None => redirect("/")
      case Some(endpoint) => openIdDiscoveryAndRedirect(endpoint)
    }
  }

  /**
   * Return url for openid service
   * User will be redirected here by oauth provider
   */
  get("/openidauth") {
    loginWithOpenId.authenticate(request) match {
      case Failure(ex) =>
        warn("Feilet openid autentisering", ex)
        redirect("/")
      case Success(openIdUser) =>
        openIdUser.flatMap { u =>
          userRepository.findByOpenId(u.id) match {
            case Some(dbUser) => {
              val updatedDbUser = userRepository.update(openIdUserToDbUser(u), dbUser._id.toHexString)
              updatedDbUser.map(createSessionUserAndUpdateLastLogin)
            }
            case None => userRepository.insert(openIdUserToDbUser(u)).map(createSessionUserAndUpdateLastLogin)
          }
        } match {
          case None => redirect("/")
          case Some(dpUser) =>
            session.put(SessionUser.sessionKey, dpUser)
            redirect(session.getAs[String]("returnUrl").getOrElse("/"))
        }
    }
  }

  /**
   * Used by frontend to check if user is logged in
   */
  get("/user") {
    requireAuthentication(user => Ok(sessionUserToUserInfo(user)))
  }

  private def openIdDiscoveryAndRedirect(endpoint: String) {
    params.get("returnUrl").foreach(url => session.put("returnUrl", url))
    val redirectUrl = settings.openIdRedirectUrl
    val realm = settings.openIdRealm
    loginWithOpenId.authenticationProviderRedirectEndpoint(endpoint, redirectUrl, realm, session) match {
      case Failure(_) => redirect("/")
      case Success(url) => redirect(url)
    }
  }

  private def createSessionUserAndUpdateLastLogin(dbUser: DbUser) = {
    Future {
      userRepository.updateLastLogin(dbUser._id.toHexString, DateTime.now.toDate)
    }
    SessionUser(dbUser, loginWithDigipost.randomNonce)
  }
}