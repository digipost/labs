package no.digipost.labs.oauth

import com.ning.http.util.Base64
import no.digipost.labs.oauth.LoginWithDigipost.{RefreshToken, AuthorisationCodeToken, Token}

case class AccessTokenRequest(url: String, parameters: Map[String, String], headers: Map[String, String])

object AccessTokenRequest {
  def apply(token: Token, settings: OAuthSettings, nonce: String): AccessTokenRequest = {
    val authentication = new String(Base64.encode(s"${settings.clientId}:${settings.secret}".getBytes))

    val grantParameters = token match {
      case AuthorisationCodeToken(code) => Map("grant_type" -> "code", "code" -> code, "redirect_uri" -> settings.redirectUrl, "nonce" -> nonce)
      case RefreshToken(t) => Map("grant_type" -> "refresh_token", "refresh_token" -> t)
    }
    val headers = Map("Content-Type" -> "application/x-www-form-urlencoded", "Authorization" -> ("Basic " + authentication))

    AccessTokenRequest(settings.accessTokenUrl, grantParameters, headers)
  }
}
