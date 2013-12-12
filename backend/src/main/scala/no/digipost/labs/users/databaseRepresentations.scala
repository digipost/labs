package no.digipost.labs.users

import org.bson.types.ObjectId
import java.util.Date
import org.joda.time.DateTime

case class DbUser(_id: ObjectId,
                  username: Option[String] = None,
                  name: String,
                  email: Option[String],
                  digipostAddress: Option[String] = None,
                  digipostId: Option[String] = None,
                  openidUrl: Option[String] = None,
                  facebookUid: Option[String] = None,
                  created: Date = DateTime.now.toDate,
                  oldId: Option[String] = None,
                  profile: Option[DbProfile] = None,
                  admin: Boolean = false,
                  lastLogin: Option[Date] = None)

case class DbProfile(icon: String, title: String, expertise: String, avatar: Option[String], about: String, contactCards: List[DbContactCard])

case class DbContactCard(icon: String, url: String, name: String)
