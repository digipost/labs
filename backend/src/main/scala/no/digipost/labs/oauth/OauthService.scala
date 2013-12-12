package no.digipost.labs.oauth

import scala.concurrent.Future
import dispatch._
import org.json4s.jackson.JsonMethods._
import org.json4s._
import concurrent.ExecutionContext.Implicits.global

trait OAuthService {
  def getAccessToken(request: AccessTokenRequest): Future[AccessToken]
}

class HttpOAuthService extends OAuthService {

  implicit val jsonFormats = DefaultFormats

  def getAccessToken(request: AccessTokenRequest) =
    Http(url(request.url) << request.parameters <:< request.headers > {
      response =>
        parse(response.getResponseBody).extract[AccessToken]
    })
}

