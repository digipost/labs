package no.digipost.labs.errorhandling

import scala.util.{Try, Failure}
import no.digipost.labs.util.Logging

case class Error(errorMessage: String)

case class WebError(status: Int, result: Error, cause: Option[Throwable] = None) extends Throwable(cause.orNull) {
  override def getMessage = toString

  override def toString = s"${getClass.getName}($status, $result, $cause)"
}

object WebError extends Logging {

  def apply(status: Int, result: String) = new WebError(status, Error(result))

  def fromThrowable[A](status: Int, message: String): PartialFunction[Throwable, Try[A]] = {
    case t => Failure(WebError(status, Error(message), Some(t)))
  }
}
