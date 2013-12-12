package no.digipost.labs.security

import org.scalatra.ScalatraServlet
import no.digipost.labs.errorhandling.Error
import no.digipost.labs.login.SessionUser

trait CsrfTokenFilter { self: ScalatraServlet =>

  before(List("POST", "PUT", "DELETE", "PATCH").contains(request.getMethod)) {
    session.getAs[SessionUser](SessionUser.sessionKey) map { user =>
      request.headers.get(Headers.X_CSRF_Token) match {
        case None => halt(status = 403, body = Error("Missing " + Headers.X_CSRF_Token))
        case Some(token) if token != user.csrfToken => halt(status = 403, body = Error("Invalid " + Headers.X_CSRF_Token))
        case _ => ()
      }
    }
  }
}