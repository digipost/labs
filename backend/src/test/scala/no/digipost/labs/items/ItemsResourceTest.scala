package no.digipost.labs.items

import org.scalatest.FunSuite
import org.scalatra.test.scalatest._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import no.digipost.labs.security.Headers
import Headers.X_CSRF_Token
import org.scalatra.test.HttpComponentsClient
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import no.digipost.labs.users.SessionHelper
import SessionHelper._
import java.util.Date
import no.digipost.labs.security.Headers
import no.digipost.labs.errorhandling.Error

class ItemsResourceTest extends ScalatraSuite with FunSuite with ShouldMatchers {
  val itemsRepo = new TestItemsRepository

  private val itemWithBody = DbItem(new ObjectId(), ItemType.news, new Date, author = "Test Testesen", body = "body", source = Some("Original source"))
  itemsRepo.insert(itemWithBody)
  private val itemWithoutSource = DbItem(new ObjectId(), ItemType.news, new Date, author = "Test Testesen", body = "body", source = None)
  itemsRepo.insert(itemWithoutSource)

  addServlet(new ItemsResource(new ItemsService(itemsRepo)), "/*")
  addServlet(sessionServletWithMocks, "/sessions/*")

  val validIdea = IdeaInput("Great idea everyone!", "Something smart and something funny", None)
  val validNews = NewsInput("Good news everyone!", "Today you'll be delivering a crate of subpoenas to Sicily 8, the Mob Planet!")

  test("get items") {
    get("/items") {
      status should equal (200)
      val items = parse(body).extract[Items]
      assert(items.items.size === 2)
    }
  }

  test("should have no-cache and security headers") {
    get("/items") {
      assert(response.headers(Headers.CacheControl) === List("no-cache, no-store, no-transform"))
      assert(response.headers(Headers.StrictTransportSecurity) === List("max-age=31536000"))
      assert(response.headers(Headers.XContentTypeOptions) === List("nosniff"))
      assert(response.headers(Headers.XFrameOptions) === List("deny"))
      assert(response.headers(Headers.XPermittedCrossDomainPolicies) === List("master-only"))
      assert(response.headers(Headers.XXSSProtection) === List("1; mode=block"))
    }
  }

  test("get a single item by id") {
    get(s"/items/${itemWithBody._id}") {
      status should equal (200)
      val item = parse(body).extract[Item]
      assert(item.id === itemWithBody._id.toStringMongod)
      assert(item.source === None)
    }
  }

  test("get a single item by id not accessible for non admin") {
    get(s"/items/${itemWithBody._id}/editable") {
      status should equal (403)
    }
  }

  test("get a single item by id for editing by admin") {
    session {
      SessionHelper.loginUser(this, admin = true)
      get(s"/items/${itemWithBody._id}/editable") {
        status should equal (200)
        val item = parse(body).extract[Item]
        assert(item.id === itemWithBody._id.toStringMongod)
        assert(item.source === Some("Original source"))
      }
    }
  }

  test("items without source should use body when editing") {
    session {
      SessionHelper.loginUser(this, admin = true)
      get(s"/items/${itemWithoutSource._id}/editable") {
        status should equal (200)
        val item = parse(body).extract[Item]
        assert(item.id === itemWithoutSource._id.toStringMongod)
        assert(item.body === "body")
        assert(item.source === Some("body"))
      }
    }
  }

  test("get items of type") {
    get("/items/type/news") {
      status should equal (200)
      val items = parse(body).extract[Items]
      assert(items.items.size === 2)
    }
  }

  test("no access when not logged in") {
    post("/ideas", body = Serialization.write(validIdea), headers = Map("Content-Type" -> "application/json")) {
      assert(status === 403)
    }
  }

  test("should create new item when logged in") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)
      createNewIdea(this, validIdea, csrfToken)
    }
  }

  test("should not create idea when not admin and status is set") {
    session {
      val idea = IdeaInput("Great idea everyone!", "Something smart and something funny", Some(Status.Closed.toString))
      post("/ideas", Serialization.write(idea).getBytes, Map("Content-Type" -> "application/json", X_CSRF_Token -> loginUserAndGetCsrfToken(this))) {
        assert(status === 403)
      }
    }
  }

  test("should create idea when admin and status is set") {
    session {
      val idea = IdeaInput("Great idea everyone!", "Something smart and something funny", Some(Status.Closed.toString))
      val newIdea = createNewIdea(this, idea, loginUserAndGetCsrfToken(this, admin = true))
      assert(newIdea.status.get === Status.Closed.toString)
    }
  }

  test("should not create news when not admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = false)
      val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
      val news = NewsInput("news", "body")
      post("/news", Serialization.write(news).getBytes, headers) {
        status should be(403)
      }
    }
  }


  test("should update news when admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)
      val item = createNews(this, NewsInput("Breaking news", "Long story short"), csrfToken)
      val changed = NewsInput("changed title", "changed body")
      post(s"/news/${item.id}", Serialization.write(changed), headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)) {
        status should be(200)
        val updated = parse(body).extract[Item]
        updated.id should equal(item.id)
        updated.title should equal(Some("changed title"))
        updated.source should equal(Some("changed body"))
        updated.body should equal("<p>changed body</p>")
      }
    }
  }
  
  test("should create tweet when admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)
      val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
      val tweet = TweetInput("https://twitter.com", "@froden", "Digipost er best, ingen protest!")
      post("/tweets", Serialization.write(tweet).getBytes, headers) {
        status must be(201)
        val item = parse(body).extract[Item]
        item.url must be(Some("https://twitter.com"))
        item.author.name must be("@froden")
      }
    }
  }

  test("should not be able to delete when not admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = false)
      val item = createNewIdea(this, validIdea, csrfToken)
      delete(s"/items/${item.id}", headers = Map(X_CSRF_Token -> csrfToken)) {
        status should be(403)
      }
    }
  }

  test("should delete news when admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)
      val item = createNewIdea(this, validIdea, csrfToken)
      delete(s"/items/${item.id}", headers = Map(X_CSRF_Token -> csrfToken)) {
        status should be(204)
      }
      get(s"/items/${item.id}") {
        status should be(404)
      }
    }
  }

  test("should not create item when missing csrf-token") {
    session {
      loginUser(this)
      post("/items", body = Serialization.write(validIdea), headers = Map("Content-Type" -> "application/json")) {
        assert(status === 403)
        assert(response.body.contains("Missing " + X_CSRF_Token))
      }
    }
  }

  test("should not create item when invalid csrf-token") {
    session {
      loginUser(this)
      post("/ideas", body = Serialization.write(validIdea), headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> "invalid")) {
        assert(status === 403)
        assert(response.body.contains("Invalid " + X_CSRF_Token))
      }
    }
  }

  test("should comment on item") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)
      val item = createNewIdea(this, validIdea, csrfToken)
      createNewComment(this, item.id, "Hei på deg", csrfToken, 201)
      assert(getItem(this, item.id).comments.size === 1)
    }
  }

  test("no empty comments") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)
      val item = createNewIdea(this, validIdea, csrfToken)
      createNewComment(this, item.id, "", csrfToken, 400)
    }
  }

  test("should list latest comments for admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)

      val idea = createNewIdea(this, validIdea, csrfToken)
      val news = createNews(this, validNews, csrfToken)
      createNews(this, NewsInput("This just in", "News without comments"), csrfToken)

      createNewComment(this, idea.id, "Kommentar1", csrfToken, 201)
      createNewComment(this, news.id, "Kommentar2", csrfToken, 201)
      createNewComment(this, news.id, "Kommentar3", csrfToken, 201)
      createNewComment(this, idea.id, "Kommentar4", csrfToken, 201)
      createNewComment(this, news.id, "Kommentar5", csrfToken, 201)
      createNewComment(this, news.id, "Kommentar6", csrfToken, 201)

      get("/comments", headers = Map(X_CSRF_Token -> csrfToken)) {
        assert(status === 200)
        val comments = parse(body).camelizeKeys.extract[List[Comment]]
        comments.size should be >= 6
        comments.head.body should include ("Kommentar6")
        comments.head.itemId should be(news.id)
      }
    }
  }

  test("should not list comments when user or not logged in") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = false)
      get("/comments", headers = Map(X_CSRF_Token -> csrfToken)) {
        assert(status === 403)
      }
    }

    get("/comments") {
      assert(status === 403)
    }
  }

  test("should delete a comment when admin") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)
      val item = createNewIdea(this, validIdea, csrfToken)
      createNewComment(this, item.id, "En kommentar", csrfToken, 201)
      createNewComment(this, item.id, "En kommentar til", csrfToken, 201)
      createNewComment(this, item.id, "Enda en kommentar", csrfToken, 201)

      val itemWithComments = getItem(this, item.id)
      assert(itemWithComments.comments.size === 3)

      val commentToDelete = itemWithComments.comments.head
      delete(s"/items/${item.id}/comments/${commentToDelete.id}", headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)) {
        assert(status === 200)
        val item = parse(body).extract[Item]
        assert(item.comments.size === 2)
      }
      assert(getItem(this, item.id).comments.size === 2)
    }
  }
  
  test("should not delete a comment when not admin") {
    val item = session {
      val csrfToken = loginUserAndGetCsrfToken(this, admin = true)
      val item = createNewIdea(this, validIdea, csrfToken)
      createNewComment(this, item.id, "En kommentar", csrfToken, 201)
      createNewComment(this, item.id, "En kommentar til", csrfToken, 201)
      createNewComment(this, item.id, "Enda en kommentar", csrfToken, 201)
      item
    }

    val itemWithComments = getItem(this, item.id)
    assert(itemWithComments.comments.size === 3)

    val commentToDelete = itemWithComments.comments.head
    delete(s"/items/${item.id}/comments/${commentToDelete.id}", headers = Map("Content-Type" -> "application/json")) {
      assert(status === 403)
    }
    assert(getItem(this, item.id).comments.size === 3)
  }

  test("should vote on item") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)
      val item = createNewIdea(this, validIdea, csrfToken)
      assert(item.votes === 0)

      val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
      val resultItem = post(s"/items/${item.id}/votes", params = Nil, headers = headers) {
        assert(status === 200)
        parse(body).extract[Item]
      }
      assert(resultItem.votes === 1)
      assert(resultItem.voted)
    }
  }

  test("should not be able to vote when not logged in") {
    val itemId = session {
      val csrfToken = loginUserAndGetCsrfToken(this)
      val item = createNewIdea(this, validIdea, csrfToken)
      item.id
    }

    post(s"/items/$itemId/votes", params = Nil, headers = Map("Content-Type" -> "application/json")) {
      assert(status === 403)
    }

  }

  test("Invalid routes should give 404 not found") {
    get("/invalid") {
      assertNotFound()
    }
    get("invalid") {
      assertNotFound()
    }
    get("/") {
      assertNotFound()
    }

    get("") {
      assertNotFound()
    }

    def assertNotFound() = {
      assert(status === 404)
      assert(parse(body).camelizeKeys.extract[Error] === Error("Not found"))
    }
  }

  def getItem(client: HttpComponentsClient, itemId: String): Item = {
    client.get(s"/items/$itemId") {
      assert(client.status === 200)
      parse(client.body).extract[Item]
    }
  }

  def createNewIdea(client: HttpComponentsClient, idea: IdeaInput, csrfToken: String): Item = {
    val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
    val created = client.post("/ideas", Serialization.write(idea).getBytes, headers) {
      assert(client.status === 201)
      parse(client.body).extract[Item]
    }
    getItem(client, created.id)
  }

  def createNews(client: HttpComponentsClient, news: NewsInput, csrfToken: String): Item = {
    val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
    val created = client.post("/ideas", Serialization.write(news).getBytes, headers) {
      assert(client.status === 201)
      parse(client.body).extract[Item]
    }
    getItem(client, created.id)
  }

  def createNewComment(client: HttpComponentsClient, itemId: String, body: String, csrfToken: String, expectedStatus: Int) {
    val item = getItem(client, itemId)
    val headers = Map("Content-Type" -> "application/json", X_CSRF_Token -> csrfToken)
    val comment = CommentInput(body = body)
    client.post(s"/items/$itemId/comments", Serialization.write(comment).getBytes, headers) {
      assert(client.status === expectedStatus)
      expectedStatus match {
        case 201 => {
          val updatedItem = parse(client.body).extract[Item]
          assert(updatedItem.comments.size === (item.comments.size + 1))
        }
        case _ => parse(client.body).camelizeKeys.extract[Error]
      }
    }
  }
}