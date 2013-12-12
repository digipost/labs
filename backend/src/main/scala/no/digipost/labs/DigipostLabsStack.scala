package no.digipost.labs

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.json4s.JsonAST.JValue
import scala.util.Try
import no.digipost.labs.security.{SecurityHeaders, CsrfTokenFilter}
import no.digipost.labs.errorhandling.{WebError, Error}

trait DigipostLabsStack extends ScalatraServlet with GZipSupport with JacksonJsonSupport with CsrfTokenFilter with SecurityHeaders {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  notFound {
    NotFound(Error("Not found"))
  }

  def extractFromJson[T: Manifest](json: JValue): Try[T] =
    Try(json.extract[T]).recoverWith(WebError.fromThrowable[T](400, "Error parsing json"))
}
