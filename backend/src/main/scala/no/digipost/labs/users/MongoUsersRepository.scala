package no.digipost.labs.users

import com.mongodb.casbah.Imports._
import org.json4s.Extraction
import no.digipost.labs.util.{Logging, Repository}
import scala.Some
import java.util.Date

class MongoUsersRepository(users: MongoCollection) extends UsersRepository with Repository with Logging {

  def findById(id: String): Option[DbUser] = users.findOne(idQuery(id)) flatMap toObject[DbUser]

  override def findByDigipostId(id: String): Option[DbUser] = findOneBy("digipostId", id)

  override def findByOpenId(id: String): Option[DbUser] = findOneBy("openidUrl", id)

  private def findOneBy(key: String, value: String): Option[DbUser] = users.findOne(MongoDBObject(key -> value)) flatMap toObject[DbUser]

  override def insert(user: DbUser) =  {
    val mongoObj = jsToMongo(Extraction.decompose(user))
    users.insert(mongoObj)
    toObject[DbUser](mongoObj)
  }

  override def update(user: DbUser, id: String): Option[DbUser] = {
    val update =  user.digipostAddress match {
      case Some(da) => $set("name" -> user.name, "email" -> user.email, "digipostAddress" -> user.digipostAddress)
      case None => $set("name" -> user.name, "email" -> user.email)
    }
    users.update(idQuery(id), update)
    findById(id)
  }

  override def updateLastLogin(id: String, date: Date): Unit = users.update(idQuery(id), $set("lastLogin" -> date))

  override def getUsersWithProfile: Seq[DbUser] = {
    users.find("profile" $exists true)
      .sort(MongoDBObject("created" -> -1))
      .flatMap(obj => toObject[DbUser](obj))
      .toList
  }

  override def updateProfile(profile: DbProfile, userId: String) =  {
    val mongoProfile = jsToMongo(Extraction.decompose(profile))
    val update = $set("profile" -> mongoProfile)
    users.update(idQuery(userId), update)
    findById(userId)
  }
}