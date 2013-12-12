package no.digipost.labs.login

import java.security.MessageDigest
import java.math.BigInteger
import no.digipost.labs.users.DbUser

case class SessionUser(id: String, name: String, emailAddress: Option[String], digipostAddress: Option[String], admin: Boolean, csrfToken: String) {

  def emailHash = {
    val hash = emailAddress.map(email => MessageDigest.getInstance("MD5").digest(email.toLowerCase.getBytes))
    hash.map(hash => new BigInteger(1, hash).toString(16))
  }
}

object SessionUser {
  val sessionKey = "logged_in_user"

  def apply(user: DbUser, csrfToken: String): SessionUser = SessionUser(user._id.toStringMongod, user.name, user.email, user.digipostAddress, user.admin, csrfToken)
}
