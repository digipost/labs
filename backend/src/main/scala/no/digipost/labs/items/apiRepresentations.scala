package no.digipost.labs.items

import java.util.Date

/*
 * INPUT REPRESENTATIONS
 */

case class IdeaInput(title: String, body: String, status: Option[String])

case class NewsInput(title: String, body: String, index: Option[Int] = None, imageUrl: Option[String] = None)

case class TweetInput(url: String, author: String, body: String)

case class CommentInput(body: String)



/*
 * OUTPUT REPRESENTATIONS
 */

case class Item(id: String,
                `type`: String,
                date: Date,
                title: Option[String] = None,
                body: String,
                source: Option[String] = None,
                author: Author,
                votes: Int = 0,
                voted: Boolean = false,
                url: Option[String],
                status: Option[String] = None,
                index: Option[Int] = None,
                comments: List[Comment])

case class Items(items: Seq[Item], links: Map[String, String] = Map())

case class Comment(id: String, author: Author, body: String, date: Date, itemId: String)

case class Author(userId: Option[String], name: String, avatar: String, admin: Boolean)
