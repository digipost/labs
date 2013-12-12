package no.digipost.labs.oauth

import scala.concurrent.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

case class DigipostUser(id: String, name: String, emailAddress: String, digipostAddress: String)

trait DigipostService {
  def getBasicUserDetails(uri: String, accessToken: AccessToken): Future[DigipostUser]
}

class HttpDigipostService extends DigipostService {
  implicit val jsonFormats = DefaultFormats

  def getBasicUserDetails(uri: String, accessToken: AccessToken): Future[DigipostUser] = {
    Http(url(uri) <:< Seq("Accept" -> "application/vnd.digipost-v2+json", "Authorization" -> s"Bearer ${accessToken.access_token}") > {
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