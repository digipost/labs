package no.digipost.labs.users

import no.digipost.labs.items.Avatar
import no.digipost.labs.oauth.DigipostUser
import org.bson.types.ObjectId
import no.digipost.labs.openid.OpenIdUser
import no.digipost.labs.login.SessionUser

object Converters {

  def sessionUserToUserInfo(user: SessionUser): UserInfo = UserInfo(user.name, user.emailHash, user.admin, user.csrfToken)

  def dbUserToProfile(u: DbUser): Profile =
    u.profile map { p =>
        Profile(
          name = u.name,
          icon = p.icon,
          title = p.title,
          expertise = Some(p.expertise),
          avatar = p.avatar getOrElse Avatar.emailToAvatar(u.email, u.name),
          about = Some(p.about),
          contacts = p.contactCards.map(cc => ContactCard(cc.icon, cc.url, cc.name))
        )
    } getOrElse {
      Profile(
        name = u.name,
        icon = "user",
        title = "Bruker",
        avatar = Avatar.emailToAvatar(u.email, u.name)
      )
    }

  def digipostUserToDbUser(u: DigipostUser): DbUser =
    DbUser(
      _id = new ObjectId(),
      name = u.name,
      email = Some(u.emailAddress),
      digipostAddress = Some(u.digipostAddress),
      digipostId = Some(u.id)
    )

  def openIdUserToDbUser(u: OpenIdUser): DbUser =
    DbUser(
      _id = new ObjectId(),
      name = u.name,
      email = Some(u.email),
      openidUrl = Some(u.id)
    )
}
