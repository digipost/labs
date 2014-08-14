package no.digipost.labs.items

import java.util.Date

import no.digipost.labs.errorhandling.WebError
import no.digipost.labs.items.Converters._
import no.digipost.labs.links.{LinkBuilder, Relations}
import no.digipost.labs.login.SessionUser

import scala.util.{Failure, Success, Try}


class ItemsService(itemsRepo: ItemsRepository) {

  def search(query: Option[String]): Try[Items] = Try(Items(itemsRepo.search(query).map(dbItemToItem(None))))

  def findAll(user: Option[SessionUser], start: Option[Int], linkBuilder: LinkBuilder): Try[Items] =
    Try(findManyWithPaging(user, start, itemsRepo.findAll, linkBuilder))

  def findById(id: String, user: Option[SessionUser]): Try[Item] = {
    val item = Try(itemsRepo.findById(id).map(dbItemToItem(user))).flatMap(checkResult(404, "Not found"))
    item.map(i => i.copy(comments = i.comments.sortBy(_.date)(Ordering[Date].reverse)))
  }

  def findByOldId(oldId: String, user: Option[SessionUser]): Try[Item] =
    Try(itemsRepo.findByOldId(oldId).map(dbItemToItem(user))).flatMap(checkResult(404, "Not found"))

  def findByType(`type`: String, user: Option[SessionUser], start: Option[Int], linkBuilder: LinkBuilder): Try[Items] =
    Try(findManyWithPaging(user, start, itemsRepo.findByType(`type`, _: Option[Int]), linkBuilder))

  def createNews(news: NewsInput, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated @ NewsInput(_, body, _, _) <- Validator.validate(news)
      htmlBody = Markdown.markdownToHtml(body)
      stored <- Try(itemsRepo.insert(newsToDbItem(validated, user, htmlBody)))
      resultItem <- checkResult("Unable to create News")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def updateNews(news: NewsInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated @ NewsInput(_, body, _, _) <- Validator.validate(news)
      htmlBody = Markdown.markdownToHtml(body)
      stored <- Try(itemsRepo.update(newsToDbItem(validated, user, htmlBody), id))
      resultItem <- checkResult("Unable to update News")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def deleteItem(id: String, user: SessionUser): Try[Unit] =
    for {
      _ <- checkAdmin(user)
      result <- Try(itemsRepo.delete(id))
    } yield result

  def createIdea(idea: IdeaInput, user: SessionUser): Try[Item] = {
    for {
    //Only admin users are allowed to set status for new ideas
      _ <- if (idea.status.isDefined) checkAdmin(user) else Success(Unit)
      validated <- Validator.validate(idea)
      stored <- Try(itemsRepo.insert(ideaToDbItem(validated, user)))
      resultItem <- checkResult("Unable to create Idea")(stored)
    } yield dbItemToItem(Some(user))(resultItem)
  }

  def updateIdea(idea: IdeaInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(idea)
      stored <- Try(itemsRepo.update(ideaToDbItem(validated, user), id))
      resultItem <- checkResult("Unable to update Idea")(stored)
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
      resultItem <- checkResult("Unable to create Comment")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def deleteComment(itemId: String, commentId: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      deleted <- Try(itemsRepo.deleteComment(itemId, commentId))
      resultItem <- checkResult("Unable to delete Comment")(deleted)
    } yield dbItemToItem(Some(user))(resultItem)

  def createTweet(tweet: TweetInput, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(tweet)
      stored <- Try(itemsRepo.insert(tweetToDbItem(validated, user)))
      resultItem <- checkResult("Unable to create Tweet")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def updateTweet(tweet: TweetInput, id: String, user: SessionUser): Try[Item] =
    for {
      _ <- checkAdmin(user)
      validated <- Validator.validate(tweet)
      stored <- Try(itemsRepo.update(tweetToDbItem(validated, user), id))
      resultItem <- checkResult("Unable to update Tweet")(stored)
    } yield dbItemToItem(Some(user))(resultItem)

  def addVote(user: SessionUser, itemId: String): Try[Item] =
    for {
      stored <- Try(itemsRepo.addVote(itemId, user.id))
      resultItem <- checkResult("Unable to add vote")(stored)
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

  private def checkResult[T](message: String): Option[T] => Try[T] = checkResult[T](500, message)

  private def checkResult[T](status: Int, message: String)(result: Option[T]): Try[T] = result match {
    case Some(i) => Success(i)
    case None => Failure(WebError(status, message))
  }

  private def checkAdmin(user: SessionUser): Try[Unit] = if (user.admin) Success(Unit) else Failure(WebError(403, "Forbidden"))
}
