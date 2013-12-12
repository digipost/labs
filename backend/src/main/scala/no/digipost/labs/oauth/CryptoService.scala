package no.digipost.labs.oauth

import javax.crypto.spec.SecretKeySpec
import javax.crypto
import scala.Predef.String
import com.ning.http.util.Base64
import java.util.UUID

trait CryptoService {

  def signWithHmacSha256(tokenValue: String, secret: String): String

  def randomNonce: String
}

class DefaultCryptoService extends CryptoService {

  def signWithHmacSha256(tokenValue: String, secret: String) = {
    val HmacSHA256 = "HmacSHA256"
    val key = new SecretKeySpec(secret.getBytes, HmacSHA256)
    val mac = crypto.Mac.getInstance(HmacSHA256)
    mac.init(key)
    new String(Base64.encode(mac.doFinal(tokenValue.getBytes)))
  }

  def randomNonce = UUID.randomUUID.toString
}
