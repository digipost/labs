package no.digipost.labs.items

object Status extends Enumeration {
  val Published = Value(0, "published")
  val Started = Value(1, "started")
  val Finished = Value(2, "finished")
  val Closed = Value(3, "closed")
}