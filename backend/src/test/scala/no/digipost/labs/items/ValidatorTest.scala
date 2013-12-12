package no.digipost.labs.items

import org.scalatest.FunSuite
import scala.util.Failure
import scala.util.Success
import no.digipost.labs.errorhandling.WebError

class ValidatorTest extends FunSuite {

  test("valid idea") {
    val idea = IdeaInput("title", "body", None)
    val result = Validator.validate(idea)
    assert(result === Success(idea))
  }

  test("invalid idea") {
    val result = Validator.validate(IdeaInput("", "", None))
    assert(result === Failure(WebError(400, "title cannot be empty, body cannot be empty")))
  }

  test("valid comment") {
    val comment = CommentInput("kommentar")
    val result = Validator.validate(comment)
    assert(result === Success(comment))
  }

  test("invalid comment") {
    val result = Validator.validate(CommentInput(""))
    assert(result === Failure(WebError(400, "body cannot be empty")))
  }

}
