package no.digipost.labs.oauth

import com.ning.http.client.ProxyServer
import com.ning.http.client.ProxyServer.Protocol
import dispatch._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait OAuthService {
  def getAccessToken(request: AccessTokenRequest): Future[AccessToken]
}

class HttpOAuthService extends OAuthService {

  implicit val jsonFormats = DefaultFormats

  def getAccessToken(request: AccessTokenRequest) = {
    val req = url(request.url)
    val reqWithProxy = request.proxy.map(proxy => req.setProxyServer(new ProxyServer(Protocol.HTTPS, proxy.host, proxy.port))).getOrElse(req)
    Http(reqWithProxy << request.parameters <:< request.headers > {
      response =>
        parse(response.getResponseBody).extract[AccessToken]
    })
  }
}

