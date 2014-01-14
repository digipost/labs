package no.digipost.labs.items

import org.bson.types.ObjectId
import java.util.Date

case class DbComment(_id: ObjectId,
                     date: Date,
                     body: String,
                     author: String,
                     userId: Option[ObjectId],
                     admin: Boolean,
                     email: Option[String],
                     oldId: Option[String] = None,
                     oldParentId: Option[String] = None)

case class DbItem(_id: ObjectId,
                  `type`: String,
                  date: Date,
                  title: Option[String] = None,
                  body: String,
                  author: String,
                  userId: Option[ObjectId] = None,
                  votes: Set[ObjectId] = Set(),
                  source: Option[String] = None,
                  admin: Option[Boolean] = None,
                  email: Option[String] = None,
                  url: Option[String] = None,
                  status: Option[Status.Value] = None,
                  index: Option[Int] = None,
                  comments: List[DbComment] = List(),
                  oldId: Option[String] = None)
