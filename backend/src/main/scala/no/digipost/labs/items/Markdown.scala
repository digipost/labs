package no.digipost.labs.items

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions._

object Markdown {

  def markdownToHtml[T](item: T): String = item match {
    case NewsInput(_, body, _, _) => new PegDownProcessor(ALL).markdownToHtml(body)
  }
}
