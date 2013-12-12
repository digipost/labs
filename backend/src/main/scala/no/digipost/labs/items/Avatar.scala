package no.digipost.labs.items

import java.math.BigInteger
import java.security.MessageDigest

object Avatar {
  def emailToAvatar(email: Option[String], name: String): String = {
    val force = email.isEmpty
    val input = email.getOrElse(name)
    val hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(input.trim.toLowerCase.getBytes)).toString(16)
    val avatar = s"https://secure.gravatar.com/avatar/$hash?d=identicon"
    if(force) {
      s"$avatar&f=y"
    } else {
      avatar
    }
  }
}
