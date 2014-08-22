package no.digipost.labs.oauth

import com.ning.http.client.ProxyServer
import com.ning.http.client.ProxyServer.Protocol

import scala.concurrent.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import no.digipost.labs.Settings.Proxy

case class DigipostUser(id: String, name: String, emailAddress: String, digipostAddress: String)

trait DigipostService {
  def getBasicUserDetails(uri: String, proxy: Option[Proxy], accessToken: AccessToken): Future[DigipostUser]
}

class HttpDigipostService extends DigipostService {
  implicit val jsonFormats = DefaultFormats

  def getBasicUserDetails(uri: String, proxy: Option[Proxy], accessToken: AccessToken): Future[DigipostUser] = {
    val request = url(uri)
    val requestWithProxy = proxy.map(proxy => request.setProxyServer(new ProxyServer(Protocol.HTTPS, proxy.host, proxy.port))).getOrElse(request)
    Http(requestWithProxy <:< Seq("Accept" -> "application/vnd.digipost-v2+json", "Authorization" -> s"Bearer ${accessToken.access_token}") > {
      response =>
        val rootJson = parse(response.getResponseBody("utf-8"))
        val name = (rootJson \ "primaryAccount" \ "fullName").extract[String]
        val emailAddress = (rootJson \ "primaryAccount" \ "email").extractOpt[List[String]]
        val digipostAddress = (rootJson \ "primaryAccount" \ "digipostaddress").extract[String]

        AccessToken.getUserId(accessToken) match {
          case Success(id) => DigipostUser(id, name, emailAddress.flatMap(_.headOption).getOrElse(""), digipostAddress)
          case Failure(error) => throw error
        }
    })
  }
}