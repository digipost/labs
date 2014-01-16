package no.digipost.labs.items

trait ItemsRepository {
  def search(query: Option[String]): Seq[DbItem]

  def findAll(start: Option[Int]): (Seq[DbItem], Int)

  def findById(id: String): Option[DbItem]

  def findByOldId(oldId: String): Option[DbItem]

  def findByType(t: String, start: Option[Int]): (Seq[DbItem], Int)

  def insert(item: DbItem): Option[DbItem]

  def update(item: DbItem, id: String): Option[DbItem]

  def delete(id: String): Unit

  def findLatestComments(): Seq[(String, DbComment)]

  def deleteComment(parentId: String, commentId: String): Option[DbItem]

  def insertComment(comment: DbComment, parentId: String): Option[DbItem]
	
  def addVote(itemId: String, userId: String): Option[DbItem]
}