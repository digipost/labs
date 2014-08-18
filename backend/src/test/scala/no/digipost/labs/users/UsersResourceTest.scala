package no.digipost.labs.users

import no.digipost.labs.users.SessionHelper._
import org.bson.types.ObjectId
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFunSuite

class UsersResourceTest extends ScalatraFunSuite {
  val usersRepo = new TestUsersRepository

  addServlet(new UsersResource(usersRepo), "/users/*")
  addServlet(sessionServletWithMocks, "/sessions/*")

  val users = loadTestData()

  test("should get the an actual user profile") {
    val user = getUser(2)
    user.profile should be ('defined)

    get(s"/users/${user._id.toHexString}/profile") {
      status should equal (200)
      val profile = parse(body).extract[Profile]
      assert(profile.name === user.name)
      assert(profile.about.get === user.profile.get.about)
      assert(profile.contacts.size === user.profile.get.contactCards.size)
    }
  }

  test("should get a light profile for user without an profile element") {
    val user = getUser(1)
    user.profile should not be 'defined

    get(s"/users/${user._id.toHexString}/profile") {
      status should equal (200)
      val profile = parse(body).extract[Profile]
      assert(profile.name === user.name)
      profile.about should not be 'defined
      assert(profile.title === "Bruker")
      assert(profile.contacts.size === 0)
    }
  }

  test("should get 404 when requesting profile for a non-existing user") {
    get(s"/users/${ new ObjectId().toHexString}/profile") {
      status should equal (404)
    }
  }

  test("should get a list of all profiles when logged in as administrator") {
    session {
      SessionHelper.loginUser(this, admin = true)
      get("/users/profiles") {
        status should equal (200)
        val profiles = parse(body).extract[List[Profile]]
        profiles.size should be (2)
      }
    }
  }

  test("should get 403 if requesting the list of profiles when not logged in as administrator") {
    session {
      SessionHelper.loginUser(this)
      get("/users/profiles") {
        status should equal (403)
      }
    }
  }

  private def loadTestData() = {
    Map(
      1 -> usersRepo.insert(DbUser(_id = new ObjectId(), name = "Kjellfinn Tarvesen", email = Some("kjellfinn@example.com"))),
      2 -> usersRepo.insert(DbUser(_id = new ObjectId(), name = "Kladmin Flatlusli", email = Some("kladmin@example.com"), admin = true, profile =
        Some(DbProfile("laptop", "Utvikler", "Scala-ninja", None, "Teh 1337 Scala Ninja c0der", List(
          DbContactCard("Twitter", "https://twitter.com/kladmin", "@kladmin"),
          DbContactCard("Google+", "https://plus.google.com/kladmin", "Kladmin Flatlusli")
        )))
      )),
      3 -> usersRepo.insert(DbUser(_id = new ObjectId(), name = "Nina Nansen Hansen", email = Some("nina.nansen.hansen@example.com"), profile =
        Some(DbProfile("laptop", "Utvikler", "Det meste.", None, "", Nil))
      ))
    )
  }

  private def getUser(i: Int) = users(i) getOrElse(throw new IllegalArgumentException)

}
