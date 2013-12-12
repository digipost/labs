package no.digipost.labs.items

import org.bson.types.ObjectId
import java.util.Date
import no.digipost.labs.login.SessionUser

object Converters {

  def ideaToDbItem(i: IdeaInput, u: SessionUser): DbItem =
    DbItem(_id = new ObjectId(),
      `type` = ItemType.idea,
      date = new Date,
      title = Option(i.title),
      body = i.body,
      author = u.name,
      userId = Some(new ObjectId(u.id)),
      votes = Set(),
      source = None,
      admin = Some(u.admin),
      email = u.emailAddress,
      url = None,
      status = Some(i.status.map(Status.withName).getOrElse(Status.Published)),
      comments = Nil)

  def newsToDbItem(n: NewsInput, u: SessionUser, htmlBody: String): DbItem =
    DbItem(_id = new ObjectId(),
      `type` = ItemType.news,
      date = new Date,
      title = Option(n.title),
      body = htmlBody,
      author = u.name,
      userId = Some(new ObjectId(u.id)),
      votes = Set(),
      source = Some(n.body),
      admin = Some(u.admin),
      email = u.emailAddress,
      url = n.imageUrl,
      status = None,
      index = n.index,
      comments = Nil)

  def tweetToDbItem(t: TweetInput, u: SessionUser): DbItem =
    DbItem(
      _id = new ObjectId(),
      `type` = ItemType.tweet,
      date = new Date,
      title = None,
      body = t.body,
      author = t.author,
      userId = Some(new ObjectId(u.id)),
      votes = Set(),
      source = None,
      admin = Some(u.admin),
      email = u.emailAddress,
      url = Some(t.url),
      status = None,
      comments = Nil)

  def commentToDbComment(c: CommentInput, u: SessionUser, parentId: String): DbComment =
    DbComment(
      _id = new ObjectId(),
      date = new Date,
      body = c.body,
      author = u.name,
      userId = Some(new ObjectId(u.id)),
      admin = u.admin,
      email = u.emailAddress)

  def dbCommentToComment(parentId: String)(c: DbComment): Comment =
    Comment(
      id = c._id.toStringMongod,
      author = Author(c.userId.map(_.toStringMongod), c.author, Avatar.emailToAvatar(c.email, c.author), c.admin),
      body = c.body,
      date = c.date,
      itemId = parentId)

  def dbItemToItem(u: Option[SessionUser])(i: DbItem): Item =
    Item(i._id.toStringMongod,
      i.`type`,
      i.date,
      i.title,
      i.body,
      u.filter(_.admin).map(_ => i.source.getOrElse(i.body)),
      Author(i.userId.map(_.toStringMongod), i.author, Avatar.emailToAvatar(i.email, i.author), i.admin.getOrElse(false)),
      i.votes.size,
      u.exists(u => i.votes.map(_.toStringMongod).contains(u.id)),
      i.url,
      i.status.map(_.toString),
      i.index,
      i.comments.map(Converters.dbCommentToComment(i._id.toStringMongod)))
}
