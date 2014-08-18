package no.digipost.labs.users

import scala.language.postfixOps

import scala.collection.mutable.ListBuffer
import java.util.Date

class TestUsersRepository extends UsersRepository {
  lazy val users: ListBuffer[DbUser] = ListBuffer()

  override def findById(id: String): Option[DbUser] = users.find(_._id == id)

  override def findByDigipostId(id: String): Option[DbUser] = users.find(_.digipostId.contains(id))

  override def findByOpenId(id: String): Option[DbUser] = users.find(_.openidUrl.contains(id))

  override def insert(user: DbUser): Option[DbUser] = {
    users += user
    Some(user)
  }

  override def update(user: DbUser, id: String): Option[DbUser] = {
    user.digipostAddress match {
      case Some(da) => updateUser(id, _.copy(name = user.name, email = user.email, digipostAddress = user.digipostAddress))
      case None => updateUser(id, _.copy(name = user.name, email = user.email))
    }
  }

  override def updateLastLogin(id: String, date: Date): Unit = {
    updateUser(id, _.copy(lastLogin = Some(date)))
  }

  override def updateProfile(profile: DbProfile, userId: String): Option[DbUser] = {
    updateUser(userId, _.copy(profile = Option(profile)))
  }

  override def getUsersWithProfile: Seq[DbUser] = users.filter(_.profile.isDefined)

  private def updateUser(userId: String, action: DbUser => DbUser) = {
    val oldUser = users.find(_._id == userId)
    val newUser = oldUser.map(action)
    oldUser foreach (users -=)
    newUser foreach (users +=)
    newUser
  }
}
