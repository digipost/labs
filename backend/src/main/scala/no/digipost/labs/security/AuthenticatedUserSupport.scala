package no.digipost.labs.security

import org.scalatra.{Forbidden, ScalatraServlet}
import no.digipost.labs.login.SessionUser

trait AuthenticatedUserSupport { self: ScalatraServlet =>

  /**
   * Run action only if user is logged in
   */
  def requireAuthentication[T](action: SessionUser => T): T =
    session.getAs[SessionUser](SessionUser.sessionKey) match {
      case None => halt(Forbidden())
      case Some(user) => action(user)
    }

  /**
   * Run action only if user is admin
   */
  def requireAdmin[T](action: SessionUser => T): T =
    session.getAs[SessionUser](SessionUser.sessionKey) match {
      case Some(user) if user.admin => action(user)
      case _ => halt(Forbidden())
    }

  def getAuthenticatedUser = session.getAs[SessionUser](SessionUser.sessionKey)
}
