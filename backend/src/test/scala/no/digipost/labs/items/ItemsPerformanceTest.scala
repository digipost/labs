package no.digipost.labs.items

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global

class ItemsPerformanceTest extends FlatSpec with ShouldMatchers {

  "All items" should "load fast" ignore  {
    val startTime = System.currentTimeMillis()
    for (_ <- 1 to 500) yield Http(url("https://localhost:7000/api/items") OK as.String).apply()
    val duration = System.currentTimeMillis() - startTime
    println(duration + " ms")
  }
}
