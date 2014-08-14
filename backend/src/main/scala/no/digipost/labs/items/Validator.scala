package no.digipost.labs.items

import no.digipost.labs.errorhandling.WebError

import scala.util.{Failure, Success, Try}

object Validator {

  def validate[T <: Input](item: T): Try[T] = item match {
    case NewsInput(title, body, _, _) => validate(item, notEmpty(title, "title"), notEmpty(body, "body"))
    case TweetInput(url, author, body) => validate(item, notEmpty(url, "title"), notEmpty(author, "author"), noMarkup(author, "author"), notEmpty(body, "body"))
    case IdeaInput(title, body, status) => validate(item, notEmpty(title, "title"), notEmpty(body, "body"))
    case CommentInput(body) => validate(item, notEmpty(body, "body"))
  }

  private def notEmpty(str: String, fieldName: String) = check(str)(!_.isEmpty, s"$fieldName cannot be empty")
  private def noMarkup(str: String, fieldName: String) = check(str)(str => List("<", ">", "&").forall(c => !str.contains(c)), s"$fieldName cannot contain markup")

  private def check[T](item: T)(p: T => Boolean, errorMessage: String): Option[String] = if(p(item)) None else Some(errorMessage)

  private def validate[T](item: T, results: Option[String] *): Try[T] = {
    val errors = results.toList.flatten
    if (errors.isEmpty) Success(item) else Failure(WebError(400, errors.mkString(", ")))
  }
}