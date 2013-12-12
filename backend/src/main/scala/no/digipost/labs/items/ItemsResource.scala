package no.digipost.labs.items

import scala.util.Try
import org.scalatra.{Ok, Created}
import no.digipost.labs.DigipostLabsStack
import org.scalatra.ActionResult
import no.digipost.labs.security.{AuthenticatedUserSupport, AcceptJsonOnlyFilter}
import no.digipost.labs.errorhandling.ResponseHandler
import no.digipost.labs.util.Logging
import no.digipost.labs.links.{LinkBuilder, Relations}

class ItemsResource(itemsService: ItemsService) extends DigipostLabsStack with AuthenticatedUserSupport with Logging with ResponseHandler with AcceptJsonOnlyFilter {

  get("/search") {
    toOkResponse(itemsService.search(params.get("query")))
  }

  get("/items") {
    toOkResponse(
      itemsService.findAll(
        getAuthenticatedUser,
        params.getAs[Int]("start"),
        LinkBuilder(Relations.MoreItems -> "/items")))
  }

  get("/items/:id") {
    toOkResponse(itemsService.findById(params("id"), getAuthenticatedUser))
  }

  get("/items/:id/editable") {
    requireAdmin { adminUser =>
      toOkResponse(itemsService.findById(params("id"), Some(adminUser)))
    }
  }

  get("/comments") {
    requireAdmin { adminUser =>
      toOkResponse(itemsService.getLatestComments(adminUser))
    }
  }

  get("/items/type/:type") {
    val typeParam = params("type")
    toOkResponse(
      itemsService.findByType(
        typeParam,
        getAuthenticatedUser,
        params.getAs[Int]("start"),
        LinkBuilder(Relations.MoreItems -> ("/items/type/" + typeParam))))
  }

  post("/news") {
    requireAdmin { adminUser =>
      create[NewsInput](itemsService.createNews(_: NewsInput, adminUser))
    }
  }

  post("/news/:id") {
    requireAdmin { adminUser =>
      update[NewsInput](itemsService.updateNews(_: NewsInput, params("id"), adminUser))
    }
  }

  post("/ideas") {
    requireAuthentication { user =>
      create[IdeaInput](itemsService.createIdea(_: IdeaInput, user))
    }
  }

  post("/ideas/:id") {
    requireAdmin { adminUser =>
      update[IdeaInput](itemsService.updateIdea(_: IdeaInput, params("id"), adminUser))
    }
  }

  post("/tweets") {
    requireAdmin { adminUser =>
      create[TweetInput](itemsService.createTweet(_: TweetInput, adminUser))
    }
  }

  post("/tweets/:id") {
    requireAdmin { adminUser =>
      update[TweetInput](itemsService.updateTweet(_: TweetInput, params("id"), adminUser))
    }
  }

  post("/items/:id/comments") {
    requireAuthentication { user =>
      create[CommentInput](itemsService.createNewComment(_: CommentInput, user, params("id")))
    }
  }

  delete("/items/:itemId/comments/:commentId") {
    requireAdmin { adminUser =>
      toOkResponse(itemsService.deleteComment(params("itemId"), params("commentId"), adminUser))
    }
  }

  post("/items/:id/votes") {
    requireAuthentication { user =>
      toOkResponse(itemsService.addVote(user, params("id")))
    }
  }

  delete("/items/:id") {
    requireAdmin { adminUser =>
      toNoContentReponse(itemsService.deleteItem(params("id"), adminUser))
    }
  }

  def create[T: Manifest](action: T => Try[Item]): ActionResult = crud(action, Created(_))

  def update[T: Manifest](action: T => Try[Item]): ActionResult = crud(action, Ok(_))

  def crud[T: Manifest](action: T => Try[Item], onSuccess: Item => ActionResult): ActionResult = {
    val res = for {
      representation <- extractFromJson[T](parsedBody)
      stored <- action(representation)
    } yield stored
    toResponse(res)(onSuccess)
  }
}
