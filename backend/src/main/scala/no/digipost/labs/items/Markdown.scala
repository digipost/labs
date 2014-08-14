package no.digipost.labs.items

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions._

object Markdown {

  def markdownToHtml(item: String): String = new PegDownProcessor(ALL).markdownToHtml(item)
}
