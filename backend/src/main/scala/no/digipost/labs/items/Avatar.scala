package no.digipost.labs.items

import java.security.MessageDigest

object Avatar {

  def emailToAvatar(email: Option[String], name: String): String = {
    val force = email.isEmpty
    val input = email.getOrElse(name)
    val hash = toHex(toMd5(input.trim.toLowerCase))
    val avatar = s"https://secure.gravatar.com/avatar/$hash?d=identicon"
    if(force) s"$avatar&f=y" else avatar
  }

  private def toMd5(message: String): Array[Byte] = MessageDigest.getInstance("MD5").digest(message.getBytes())

  private def toHex(byteArray : Array[Byte]): String = {
    val sb = new StringBuffer()
    byteArray.foreach(b => sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1,3)))
    sb.toString
  }
}
