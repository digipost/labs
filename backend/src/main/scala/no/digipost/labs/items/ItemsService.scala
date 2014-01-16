package no.digipost.labs.items

import scala.util.Try
import scala.util.Failure
import scala.Some
import scala.util.Success
import no.digipost.labs.errorhandling.WebError
import no.digipost.labs.links.{Relations, LinkBuilder}
import Converters._
import no.digipost.labs.login.SessionUser


class ItemsService(itemsRepo: ItemsRepository) {

  def search(query: Option[String]): Try[Items] = Try(Items(itemsRepo.search(query).map(dbItemToItem(None))))

  def findAll(user: Option[SessionUser], start: Option[Int], linkBuilder: LinkBuilder): Try[Items] =
    Try(findManyWithPaging(user, start, itemsRepo.findAll, linkBuilder))

  def findById(id: String, user: Option[SessionUser]): Try[Item] =
    Try(itemsRepo.findById(id).map(dbItemToItem(user))).flatMap(optionToWebError(404, "Not found"))

  def findByOldId(oldId: String, user: Option[SessionUser]): Try[Item] =
    Try(itemsRepo.findByOldId(oldId).map(dbItemToItem(user))).flatMap(optionToWebError(404, "Not found"))

  def findByType(`type`: String, user: Option[SessionUser], start: Option[Int], linkBuilder: LinkBuilder): Try[Items] =
    Try(findManyWithPaging(user, start, itemsRepo.findByType(`type`, _: Option[Int]), linkBuilder))

  def createNews(news: NewsInput, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(news)
      htmlBody = Markdown.markdownToHtml(validated)
      stored <- Try(itemsRepo.insert(newsToDbItem(validated, user, htmlBody)))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def updateNews(news: NewsInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(news)
      htmlBody = Markdown.markdownToHtml(validated)
      stored <- Try(itemsRepo.update(newsToDbItem(validated, user, htmlBody), id))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def deleteItem(id: String, user: SessionUser): Try[Unit] =
    for {
      _ <- checkAdmin(user)
      result <- Try(itemsRepo.delete(id))
    } yield result

  def createIdea(idea: IdeaInput, user: SessionUser): Try[Item] = {
    for {
    //Only admin users are allowed to set status for new ideas
      _ <- if (idea.status.isDefined) checkAdmin(user) else Success()
      validated <- Validator.validate(idea)
      stored <- Try(itemsRepo.insert(ideaToDbItem(validated, user)))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)
  }

  def updateIdea(idea: IdeaInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(idea)
      stored <- Try(itemsRepo.update(ideaToDbItem(validated, user), id))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def getLatestComments(user: SessionUser): Try[Seq[Comment]] = {
    for {
      _ <- checkAdmin(user)
      comments <- Try(itemsRepo.findLatestComments())
    } yield comments.map {
      case (parentId, comment) => dbCommentToComment(parentId)(comment)
    }
  }

  def createNewComment(comment: CommentInput, user: SessionUser, parentId: String): Try[Item] =
    for {
      validated <- Validator.validate(comment)
      stored <- Try(itemsRepo.insertComment(commentToDbComment(validated, user, parentId), parentId))
      resultItem <- optionToWebError(500, "Unable to store Comment")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def deleteComment(itemId: String, commentId: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      deleted <- Try(itemsRepo.deleteComment(itemId, commentId))
      resultItem <- optionToWebError(500, "Unable to delete Comment")(deleted)
    } yield dbItemToItem(Some(user))(resultItem)

  def createTweet(tweet: TweetInput, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(tweet)
      stored <- Try(itemsRepo.insert(tweetToDbItem(validated, user)))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def updateTweet(tweet: TweetInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(tweet)
      stored <- Try(itemsRepo.update(tweetToDbItem(validated, user), id))
      resultItem <- optionToWebError(500, "Unable to store Item")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def addVote(user: SessionUser, itemId: String): Try[Item] =
    for {
      stored <- Try(itemsRepo.addVote(itemId, user.id))
      resultItem <- optionToWebError(500, "Unable to add vote")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  private def findManyWithPaging(user: Option[SessionUser], start: Option[Int], findItemsInDb: Option[Int] => (Seq[DbItem], Int), linkBuilder: LinkBuilder) = {
    val (found, total) = findItemsInDb(start)
    val links: Map[String, String] = if (total - start.getOrElse(0) > found.size) {
      linkBuilder(Relations.MoreItems, query = Map("start" -> (start.getOrElse(0) + found.size))).toMap
    } else {
      Map()
    }
    Items(found.map(dbItemToItem(user)), links)
  }

  private def optionToWebError[T](status: Int, message: String)(opt: Option[T]): Try[T] = opt match {
    case Some(i) => Success(i)
    case None => Failure(WebError(status, message))
  }

  private def checkAdmin(user: SessionUser): Try[Unit] = if (user.admin) Success() else Failure(WebError(403, "Forbidden"))
}
