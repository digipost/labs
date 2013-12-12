package no.digipost.labs.openid

import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.ax.{FetchResponse, AxMessage, FetchRequest}
import org.openid4java.message.{AuthSuccess, ParameterList}
import javax.servlet.http.{HttpSession, HttpServletRequest}
import org.openid4java.discovery.DiscoveryInformation
import OpenIdConsumer._
import scala.util.Try

class OpenIdConsumer {

  lazy val manager = new ConsumerManager

  def authenticationProviderRedirectEndpoint(discoveryEndpoint: String, returnUrl: String, realm: String, session: HttpSession): Try[String] = {
    Try {
      val discoveries = manager.discover(discoveryEndpoint)
      val discovered = manager.associate(discoveries)
      session.setAttribute(SETTINGS_KEY, OpenIdSettings(discoveryEndpoint, discovered, returnUrl))
      val authReq = manager.authenticate(discovered, returnUrl, realm)
      val fetch = FetchRequest.createFetchRequest()


      discoveryEndpoint match {
        case GOOGLE_ENDPOINT => {
          fetch.addAttribute("firstname", "http://axschema.org/namePerson/first", true)
          fetch.addAttribute("lastname", "http://axschema.org/namePerson/last", true)
          fetch.addAttribute("email", "http://schema.openid.net/contact/email", true)
        }
        case YAHOO_ENDPOINT => {
          fetch.addAttribute("fullname", "http://axschema.org/namePerson", true)
          fetch.addAttribute("email", "http://axschema.org/contact/email", true)
        }
        case _ => {
          fetch.addAttribute("fullname", "http://schema.openid.net/namePerson", true)
          fetch.addAttribute("email", "http://schema.openid.net/contact/email", true)
        }
      }

      authReq.addExtension(fetch)
      authReq.getDestinationUrl(true)
    }
  }

  def authenticate(request: HttpServletRequest): Try[Option[OpenIdUser]] = {
    Try {
      val session = request.getSession
      val settings = session.getAttribute(SETTINGS_KEY).asInstanceOf[OpenIdSettings]
      clearSessionKeys(session)
      val openidResp = new ParameterList(request.getParameterMap)
      val verification = manager.verify(settings.returnUrl, openidResp, settings.discovered)
      val verified = verification.getVerifiedId

      if (verified != null) {
        val authSuccess = verification.getAuthResponse.asInstanceOf[AuthSuccess]
        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
          val fetchResp = authSuccess.getExtension(AxMessage.OPENID_NS_AX).asInstanceOf[FetchResponse]
          val identifier = verified.getIdentifier
          val emails = fetchResp.getAttributeValues("email")
          val email = emails.get(0).asInstanceOf[String]
          val name = settings.discoveryEndpoint match {
            case GOOGLE_ENDPOINT => {
              val firstName = fetchResp.getAttributeValue("firstname")
              val lastName = fetchResp.getAttributeValue("lastname")
              s"$firstName $lastName"
            }
            case _ => {
              fetchResp.getAttributeValue("fullname")
            }
          }

          Some(OpenIdUser(identifier, name, email))
        } else None
      } else None
    }
  }
}

object OpenIdConsumer {
  private final val SETTINGS_KEY = "openid_settings"

  final val GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id"
  final val YAHOO_ENDPOINT = "https://me.yahoo.com"

  private def clearSessionKeys(session: HttpSession) = session.removeAttribute(SETTINGS_KEY)
}

case class OpenIdSettings(discoveryEndpoint: String, discovered: DiscoveryInformation, returnUrl: String)

case class OpenIdUser(id: String, name: String, email: String)
