package no.digipost.labs.users

import no.digipost.labs.Settings.Proxy
import org.scalatest.Assertions
import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import no.digipost.labs.oauth._
import scala.concurrent.Future
import com.ning.http.util.Base64
import no.digipost.labs.oauth.AccessToken
import no.digipost.labs.oauth.AccessTokenRequest
import org.scalatra.test.HttpComponentsClient
import no.digipost.labs.Settings
import no.digipost.labs.oauth.AccessToken.IdToken
import no.digipost.labs.oauth.DigipostUser
import org.json4s.mongo.ObjectIdSerializer
import org.bson.types.ObjectId
import no.digipost.labs.login.SessionsResource

object SessionHelper extends Assertions {
  implicit val jsonFormats = DefaultFormats + new ObjectIdSerializer

  val settings = new Settings(Settings.load().config)

  val userRepository = new TestUsersRepository

  createTestUsers()

  class TestAccessToken(val admin: Boolean, access_token: String, refresh_token: String, expires_in: String, token_type: String, id_token: String)
    extends AccessToken(access_token, refresh_token, expires_in, token_type, Some(id_token))

  val oauthService = new OAuthService {
    def idToken = IdToken(settings.oauthClientId, "180", "frode", "https://www.digipost.no/", cryptoService.randomNonce)
    def base64IdTokenHash = Base64.encode(write(idToken).getBytes)

    override def getAccessToken(request: AccessTokenRequest): Future[AccessToken] = {
      val admin = request.parameters("code") == "admin"
      Future.successful(
        new TestAccessToken(admin,
          "S8d79_zKHBzFOMQ0kHCwJ0o9ukynG0q29L7-Tc1_IrU",
          "Abd89789KGAS78khjkasd-asdu_klasqw09jkasqwuz",
          "180",
          "bearer",
          cryptoService.signWithHmacSha256(base64IdTokenHash, settings.oauthSecret) + "." + base64IdTokenHash))
    }
  }

  val cryptoService = new CryptoService {
    def signWithHmacSha256(tokenValue: String, secret: String) = tokenValue

    def randomNonce: String = "stubbed-nonce"
  }

  val digipostService = new DigipostService {
    def getBasicUserDetails(uri: String, proxy: Option[Proxy], accessToken: AccessToken): Future[DigipostUser] = {
      val user = if (accessToken.asInstanceOf[TestAccessToken].admin)
        DigipostUser("6e948923eb19443fae21355b99bde581", "Admin Nordmann", "admin@example.com", "admin.nordmann#1234")
      else
        DigipostUser("6e048923eb19443fae11355b99bde552", "Regular Nordmann", "regular@example.com", "regular.nordmann#1234")
      Future.successful(user)
    }

  }

  def sessionServletWithMocks = new SessionsResource(settings, new LoginWithDigipost(oauthService, digipostService, cryptoService), null, userRepository)

  def loginUser(client: HttpComponentsClient, admin: Boolean = false): UserInfo = {
    client.get("/sessions") {
      assert(client.response.status === 302)
    }
    val code = if (admin) "admin" else "regular"
    client.get("/sessions/oauth", "code" -> code, "state" -> cryptoService.randomNonce) {
      assert(client.response.status === 302)
    }
    client.get("/sessions/user") {
      assert(client.response.status === 200)
      parse(client.response.body).extract[UserInfo]
    }
  }

  def loginUserAndGetCsrfToken(client: HttpComponentsClient, admin: Boolean = false) = {
    loginUser(client, admin).token
  }

  def createTestUsers() {
    userRepository.insert(DbUser(_id = new ObjectId, name = "Admin Nordmann", email = Some("admin@example.com"), digipostAddress = Some("admin.nordmann#1234"), digipostId = Some("6e948923eb19443fae21355b99bde581"), admin = true))
  }
}
