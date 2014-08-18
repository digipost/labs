package no.digipost.labs.items

import com.mongodb.casbah.Imports._
import org.json4s._
import scala.util.Try
import org.bson.BSONObject
import java.util.Date
import org.bson.types.ObjectId
import no.digipost.labs.util.{Logging, Repository}
import scala.Some

class MongoItemsRepository(items: MongoCollection) extends ItemsRepository with Repository with Logging {

  val maxResults = 50
  val maxResultsLatestComments = 100

  def findAll(start: Option[Int]): (Seq[DbItem], Int) = {
    val (found, count) = findMany(MongoDBObject(), start)
    (arrangeByIndex(found), count)
  }

  def findById(id: String): Option[DbItem] = items.findOne(idQuery(id)) flatMap toObject[DbItem]

  def findByOldId(oldId: String): Option[DbItem] = items.findOne(MongoDBObject("oldId" -> oldId)) flatMap toObject[DbItem]

  def findBy(key: String, value: String, start: Option[Int]): (Seq[DbItem], Int) = {
    val (found, count) = findMany(MongoDBObject(key -> value), start)
    (arrangeByIndex(found), count)
  }

  private def findMany(query: MongoDBObject, start: Option[Int] = Some(0)): (List[DbItem], Int) = {
    val res = items.find(query ++ MongoDBObject("status" -> MongoDBObject("$ne" -> Status.Closed.toString)))
    .sort(MongoDBObject("index" -> -1, "date" -> -1))
    .skip(start.getOrElse(0))
    val count = res.count
    (res.take(maxResults)
    .flatMap(obj => toObject[DbItem](obj))
    .toList, count)
  }

  private def arrangeByIndex(items: List[DbItem]) = {
    def iterate(i: Int, withIndex: List[DbItem], withoutIndex: List[DbItem]): List[DbItem] = withoutIndex match {
      case Nil => Nil
      case head :: tail =>
        val (equalIndex, notEqualIndex) = withIndex.partition(_.index.get <= i)
        if (equalIndex.isEmpty) {
          head :: iterate(i + 1, notEqualIndex, tail)
        } else {
          val nextIndex = i + equalIndex.size
          equalIndex ::: iterate(nextIndex, notEqualIndex, head :: tail)
        }
    }

    val (withIndex, without) = items.partition(_.index.isDefined)
    iterate(0, withIndex, without)
  }

  def search(query: Option[String]): Seq[DbItem] = {
    val response = items.db.command(MongoDBObject("text" -> "items", "search" -> query.getOrElse(""), "limit" -> maxResults))
    val results = response.get("results").asInstanceOf[BasicDBList]
    results.map(_.asInstanceOf[DBObject].get("obj").asInstanceOf[DBObject]).flatMap(obj => toObject[DbItem](obj)).toList
  }

  def findByType(t: String, start: Option[Int]): (Seq[DbItem], Int) = findBy("type", t, start)

  override def insert(item: DbItem) =  {
    val mongoObj = jsToMongo(Extraction.decompose(item))
    items.insert(mongoObj)
    toObject[DbItem](mongoObj)
  }

  override def update(item: DbItem, id: String) =  {
    val update = $set("title" -> item.title, "body" -> item.body, "source" -> item.source, "url" -> item.url.getOrElse(null), "index" -> item.index.getOrElse(null), "status" -> item.status.map(_.id))
    items.update(idQuery(id), update)
    findById(id)
  }

  override def delete(id: String) = items.remove(idQuery(id))

  override def insertComment(comment: DbComment, parentId: String) =  {
    val mongoComment = jsToMongo(Extraction.decompose(comment))
    val query = idQuery(parentId)
    val update = $addToSet("comments" -> mongoComment)
    items.update(query, update)
    findById(parentId)
  }

  override def findLatestComments(): Seq[(String, DbComment)] = {
    items
      .find(MongoDBObject() , MongoDBObject("comments" -> 1))
      .flatMap(toComments)
      .flatten
      .toList
      .sortBy(_._2.date)(Ordering[Date].reverse)
      .take(maxResultsLatestComments)
  }

  override def deleteComment(parentId: String, commentId: String) = {
    val query = idQuery(parentId)
    val update = $pull("comments" -> idQuery(commentId))
    items.update(query, update)
    findById(parentId)
  }

  override def addVote(itemId: String, userId: String) = {
	  val query = idQuery(itemId)
	  val update = $addToSet("votes" -> new ObjectId(userId))
	  items.update(query, update)
	  findById(itemId)
  }

  def toComments(obj: DBObject): Option[List[(String, DbComment)]] = {
    val parentIdJson = obj.get("_id").asInstanceOf[ObjectId].toHexString
    val commentsJson = obj.get("comments").asInstanceOf[BSONObject]
    if (commentsJson.keySet().size() == 0) {
      None
    } else {
      val commentDbItems = Try(mongoToJs(commentsJson).extract[List[DbComment]])
      if (commentDbItems.isFailure) warn(obj, commentDbItems)
      commentDbItems.map(items => items.map(item => (parentIdJson, item))).toOption
    }
  }

}