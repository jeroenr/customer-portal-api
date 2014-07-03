package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsObject
import play.api.libs.json.Json

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/29/13
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
class IncidentControllerSpec extends Specification with TestConfig with TestRequest{

  "IncidentController" should {
    "return field mappings" in {
      running(FakeAppWithTestDb) {
        val fieldmapping = get("/api/incidents/fieldmapping")

        status(fieldmapping) must equalTo(OK)
        contentType(fieldmapping) must beSome.which(_ == "application/json")
        contentAsJson(fieldmapping) must equalTo(
          Json.obj(
            "fields" -> Json.arr()
          )
        )
      }
    }

    "return search results" in {
      running(FakeAppWithTestDb) {
        val json: JsObject = Json.obj(
          "query" -> Json.obj(),
          "facets" -> Json.arr(
            Json.obj(
              "field" -> "owner",
              "values" -> Json.arr("ibanxadmin")
            )
          )
        )
        val search = post("/api/incidents/search", json)
        status(search) must equalTo(OK)
        contentType(search) must beSome.which(_ == "application/json")
        contentAsJson(search) must equalTo(
          Json.obj(
            "total" -> 0,
            "hits" -> Json.arr(),
            "facets" -> Json.arr()
          )
        )
      }
    }
  }

}
