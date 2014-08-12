package no.digipost.labs.legacy

import org.scalatra.test.scalatest.{ScalatraFunSuite, ScalatraSuite}
import no.digipost.labs.items._
import no.digipost.labs.Settings
import org.bson.types.ObjectId
import no.digipost.labs.items.DbItem
import java.util.Date

class LegacyRedirectResourceTest extends ScalatraFunSuite {
  val itemsRepo = new TestItemsRepository

  val settings = new Settings(Settings.load().config)

  private val itemWithBody = DbItem(new ObjectId(), ItemType.news, new Date, author = "Test Testesen", body = "body", source = Some("Original source"), oldId = Some("n125"))
  val newsDbItem = itemsRepo.insert(itemWithBody)

  private val ideaItemWithBody = DbItem(new ObjectId(), ItemType.idea, new Date, author = "Test Testesen", body = "body", source = Some("Original source"), oldId = Some("i12"))
  val ideaDbItem = itemsRepo.insert(ideaItemWithBody)

  addServlet(new LegacyRedirectResource(settings, new ItemsService(itemsRepo)), "/legacy/*")


  test("should be able to parse old ids from legacy idea id strings") {
    LegacyRedirectResource.parseLegacyIdString("8687;Opplasting-av-flere-dokumenter-samtidig") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("/nor/8687;Opplasting-av-flere-dokumenter-samtidig") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("8687-Opplasting-av-flere-dokumenter-samtidig") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("8687") should equal (Some("8687"))
    LegacyRedirectResource.parseLegacyIdString("51;Elektronisk-budgivning") should equal (Some("51"))
    LegacyRedirectResource.parseLegacyIdString(";Elektronisk-budgivning") should equal (None)
    LegacyRedirectResource.parseLegacyIdString("/nor/125-flyttemeldinger_for_pensjonsavtaler") should equal (Some("125"))
    LegacyRedirectResource.parseLegacyIdString("125-flyttemeldinger_for_pensjonsavtaler") should equal (Some("125"))
    LegacyRedirectResource.parseLegacyIdString("a125-flyttemeldinger_for_pensjonsavtaler") should equal (None)
  }

  test("should return 301 for known old legacy news urls") {
    get("/legacy/pages/125-flyttemeldinger_for_pensjonsavtaler") {
      status should equal (301)
      header("Location") should equal(s"https://localhost:7000/#!/item/${newsDbItem.get._id.toStringMongod}")
    }
  }

  test("should return 301 for known old legacy news urls with nor") {
    get("/legacy/pages/nor/125-flyttemeldinger_for_pensjonsavtaler") {
      status should equal (301)
      header("Location") should equal(s"https://localhost:7000/#!/item/${newsDbItem.get._id.toStringMongod}")
    }
  }

  test("shoukd return 301 for known old legacy idea urls") {
    get("/legacy/ideer/12;Rekommandert-sending-digitalt") {
      status should equal(301)
      header("Location") should equal(s"https://localhost:7000/#!/item/${ideaDbItem.get._id.toStringMongod}")
    }
  }

  test("should return 404 for unknown legacy urls") {
    get("/legacy/pages/1-sak_med_ugyldig_id") {
      status should equal (404)
    }
  }

  test("should return 404 when legacy news url contains old id from idea") {
    get("/legacy/ideer/125-flyttemeldinger_for_pensjonsavtaler") {
      status should equal (404)
    }
  }




}
