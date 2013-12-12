package no.digipost.labs.errorhandling

import org.scalatra._
import scala.util.Try
import scala.util.Failure
import org.scalatra.ActionResult
import scala.util.Success
import no.digipost.labs.util.Logging

trait ResponseHandler { self: ScalatraServlet with Logging =>

  def toOkResponse[T](res: Try[T]): ActionResult = toResponse(res)(Ok(_))

  def toNoContentReponse(res: Try[Unit]): ActionResult = toResponse(res)(_ => NoContent())

  def toResponse[T](res: Try[T])(action: T => ActionResult): ActionResult = {
    res match {
      case Success(item) =>
        action(item)
      case Failure(w @ WebError(code, result, _)) =>
        if (code > 500) error(w) else debug(w)
        ActionResult(ResponseStatus(code), result, Map())
      case Failure(t) =>
        error(t)
        InternalServerError(Error("Unknown error"))
    }
  }
}
