package no.digipost.labs.users

import org.scalatest.FunSuite
import org.scalatra.test.scalatest._
import org.json4s._
import no.digipost.labs.security.Headers
import Headers._
import org.json4s.jackson.JsonMethods._
import SessionHelper._

class SessionsResourceTest extends ScalatraFunSuite {
  addServlet(sessionServletWithMocks, "/sessions/*")

  test("successful login with Digipost") {
    session {
      loginUser(this)
    }
  }

  test("logout") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)

      delete("/sessions", headers = Map(X_CSRF_Token -> csrfToken)) {
        assert(status === 200)
      }
      get("/sessions/user", headers = Map(X_CSRF_Token -> csrfToken)) {
        assert(status === 403)
      }
    }
  }

  test("should fetch info about logged in user") {
    session {
      val csrfToken = loginUserAndGetCsrfToken(this)

      get("/sessions/user", headers = Map(X_CSRF_Token -> csrfToken)) {
        assert(status === 200)
        parse(body).extract[UserInfo]
      }
    }
  }

  test("should not get user info when not logged in") {
    get("/sessions/user") {
      assert(status === 403)
    }
  }
}
