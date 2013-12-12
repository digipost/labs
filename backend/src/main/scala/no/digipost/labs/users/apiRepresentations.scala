package no.digipost.labs.users


case class UserInfo(name: String, avatar: Option[String], admin: Boolean, token: String)

case class Profile(name: String,
                   icon: String,
                   title: String,
                   expertise: Option[String] = None,
                   avatar: String,
                   about: Option[String] = None,
                   contacts: List[ContactCard] = Nil)

case class ContactCard(icon: String, url: String, name: String)

