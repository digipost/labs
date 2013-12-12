package no.digipost.labs.oauth

import scala.language.postfixOps
import scala.util.{Try, Failure, Success}
import dispatch._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import no.digipost.labs.oauth.LoginWithDigipost.{RefreshToken, AuthorisationCodeToken, Token}

class LoginWithDigipost(oauthService: OAuthService, digipostService: DigipostService, cryptoService: CryptoService) {

  def this() = this(new HttpOAuthService, new HttpDigipostService, new DefaultCryptoService)

  def loginWithDigipost(authCode: String, oauthSettings: OAuthSettings): DigipostUser = {
    val accessTokenFuture = getAccessToken(AuthorisationCodeToken(authCode), oauthSettings, cryptoService.randomNonce)
    val digipostUserFuture = accessTokenFuture.flatMap {accessToken =>
      digipostService.getBasicUserDetails("https://www.digipost.no/post/api", accessToken)
    }
    Await.result(digipostUserFuture, 30 seconds)
  }

  def randomNonce = cryptoService.randomNonce

  private def getAccessToken(token: Token, settings: OAuthSettings, nonce: String): Future[AccessToken] = {
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
      case RefreshToken(_) => Success()
    }
  }
}

object LoginWithDigipost {
  sealed trait Token
  case class RefreshToken(value: String) extends Token
  case class AuthorisationCodeToken(value: String) extends Token
}

case class OAuthSettings(clientId: String, secret: String, redirectUrl: String, accessTokenUrl: String, state: String) {
  val authorizeUrl = s"https://www.digipost.no/post/api/oauth/authorize/new?response_type=code&client_id=$clientId&redirect_uri=$redirectUrl&state=$state"
}