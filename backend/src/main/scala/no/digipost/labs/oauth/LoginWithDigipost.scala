package no.digipost.labs.oauth

import dispatch._
import no.digipost.labs.Settings.Proxy
import no.digipost.labs.oauth.LoginWithDigipost.{AuthorisationCodeToken, RefreshToken, Token}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class LoginWithDigipost(oauthService: OAuthService, digipostService: DigipostService, cryptoService: CryptoService) {

  def this() = this(new HttpOAuthService, new HttpDigipostService, new DefaultCryptoService)

  def loginWithDigipost(authCode: String, oauthSettings: OAuthSettings): DigipostUser = {
    val accessTokenFuture = getAccessToken(AuthorisationCodeToken(authCode), oauthSettings)
    val digipostUserFuture = accessTokenFuture.flatMap {accessToken =>
      digipostService.getBasicUserDetails(oauthSettings.userDetailsUrl, oauthSettings.proxy, accessToken)
    }
    Await.result(digipostUserFuture, 30 seconds)
  }

  def randomNonce = cryptoService.randomNonce

  private def getAccessToken(token: Token, settings: OAuthSettings): Future[AccessToken] = {
    val nonce = randomNonce
    val req = AccessTokenRequest(token, settings, nonce)
    val accessTokenFuture = oauthService.getAccessToken(req)
    accessTokenFuture.flatMap { accessToken =>
      validateToken(token, accessToken, nonce, settings, cryptoService) match {
        case Success(_) => Future.successful(accessToken)
        case Failure(error) => Future.failed(error)
      }
    }
  }

  private def validateToken(token: Token, accessToken: AccessToken, nonce: String, settings: OAuthSettings, cryptoService: CryptoService): Try[Unit] = {
    token match {
      case AuthorisationCodeToken(_) => AccessToken.validate(accessToken, nonce, settings, cryptoService)
      case RefreshToken(_) => Success(Unit)
    }
  }
}

object LoginWithDigipost {
  sealed trait Token
  case class RefreshToken(value: String) extends Token
  case class AuthorisationCodeToken(value: String) extends Token
}

case class OAuthSettings(clientId: String, secret: String, redirectUrl: String, state: String, host: String, proxy: Option[Proxy]) {
  val authorizeUrl = s"https://$host/post/api/oauth/authorize/new?response_type=code&client_id=$clientId&redirect_uri=$redirectUrl&state=$state"
  val accessTokenUrl = s"https://$host/post/api/oauth/accesstoken"
  val userDetailsUrl = s"https://$host/post/api"
}