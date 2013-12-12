package no.digipost.labs.links

class LinkBuilder(templates: Map[String, String]) {

  val apiBase = "/api"

  def apply[T](relation: String, fragments: List[String] = Nil, query: Map[String, T] = Map()): Option[(String, String)] =
    templates.get(relation).map { base =>
        val path = if (fragments.isEmpty) "" else "/" + fragments.mkString("/")
        val queryString = if (query.isEmpty) "" else "?" + query.map(q => q._1 + "=" + q._2).mkString("&")
        (relation, apiBase + base + path + queryString)
    }
}

object LinkBuilder {
  def apply(templates: (String, String)*) = new LinkBuilder(templates.toMap)
}
