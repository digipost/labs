package no.digipost.labs.legacy

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.Some

class LegacyRedirectResourceTest extends ScalatraSuite with FunSuite with ShouldMatchers {

  test("should be able to parse old ids from legacy idea id strings") {
    LegacyRedirectResource.parseLegacyIdString("8687;Opplasting-av-flere-dokumenter-samtidig") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("8687-Opplasting-av-flere-dokumenter-samtidig") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("8687") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("51;Elektronisk-budgivning") should equal (Some("51"))
    LegacyRedirectResource.parseLegacyIdString(";Elektronisk-budgivning") should equal (None)
  }


}
