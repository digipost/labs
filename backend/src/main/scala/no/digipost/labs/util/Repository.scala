package no.digipost.labs.util

import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.mongo.{ObjectIdSerializer, JObjectParser}
import org.json4s.ext.EnumSerializer
import scala.util.Try
import no.digipost.labs.items.Status

trait Repository {
  self: Logging =>

  implicit val jsonFormats = DefaultFormats + new ObjectIdSerializer + new EnumSerializer(Status)

  def idQuery(id: String) = MongoDBObject("_id" -> new ObjectId(id))
  def jsToMongo(value: JValue): DBObject = JObjectParser.parse(value)
  def mongoToJs(obj: Any): JValue = JObjectParser.serialize(obj)

  def toObject[T: Manifest](obj: DBObject): Option[T] = {
    val dbType = Try(mongoToJs(obj).extract[T])
    if (dbType.isFailure) warn(obj, dbType)
    dbType.toOption
  }

}
