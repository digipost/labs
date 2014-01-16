package no.digipost.labs.items

import scala.collection.mutable.ListBuffer
import java.util.Date
import org.bson.types.ObjectId

class TestItemsRepository extends ItemsRepository {
  lazy val items: ListBuffer[DbItem] = ListBuffer()

  def search(query: Option[String]) = List()

  def findAll(start: Option[Int] = Some(0)): (Seq[DbItem], Int) = {
    val found = items.drop(start.getOrElse(0))
    (found, found.size)
  }

  def findById(id: String): Option[DbItem] = {
    items.find(_._id == id)
  }

  def findByOldId(oldId: String): Option[DbItem] = items.find(_.oldId.exists(_ == oldId))

  def findByType(t: String, start: Option[Int] = Some(0)): (Seq[DbItem], Int) = {
    val found = items.filter(_.`type`== t).drop(start.getOrElse(0))
    (found, found.size)
  }

  override def insert(item: DbItem) = {
    items += item
    Some(item)
  }

  override def update(item: DbItem, id: String): Option[DbItem] =
    updateItem(id, _.copy(title = item.title, body = item.body, source = item.source))

  override def delete(id: String) = items.find(_._id == id).foreach(i => items -= i)

  override def insertComment(comment: DbComment, parentId: String): Option[DbItem] = {
    updateItem(parentId, i => i.copy(comments = comment :: i.comments))
  }

  def findLatestComments(): Seq[(String, DbComment)] = items.flatMap(item => item.comments.map(comment => (item._id.toStringMongod, comment))).sortBy(_._2.date)(Ordering[Date].reverse)

  override def deleteComment(parentId: String, commentId: String): Option[DbItem] = {
    updateItem(parentId, i => i.copy(comments = i.comments.filter(_._id.toStringMongod != commentId)))
  }

  override def addVote(itemId: String, userId: String) = {
    updateItem(itemId, i => i.copy(votes = i.votes + new ObjectId(userId)))
  }

  private def updateItem(itemId: String, action: DbItem => DbItem) = {
    val oldItem = items.find(_._id == itemId)
    val newItem = oldItem.map(action)
    oldItem foreach (items -=)
    newItem foreach (items +=)
    newItem
  }

}