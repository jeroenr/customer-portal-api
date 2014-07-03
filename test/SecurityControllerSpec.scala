package test

import org.specs2.mutable._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import controllers.SecurityController
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/29/13
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
class SecurityControllerSpec extends Specification with TestConfig with TestRequest{

  "SecurityController" should {
    "return 401 when header and cookie are missing" in {
      running(FakeAppWithTestDb) {
        val unauthRequest = route(FakeRequest(GET, "/api/incidents")).get

        status(unauthRequest) must equalTo(UNAUTHORIZED)
      }
    }

    "not return 401 when cookie is set" in {
      running(FakeAppWithTestDb) {
        val unauthRequest = route(FakeRequest(GET, "/api/incidents").withSession(SecurityController.AUTH_USER_KEY -> "testuser")).get

        status(unauthRequest) must not equalTo(UNAUTHORIZED)
      }
    }

    "return 401 on replay attack" in {
      running(FakeAppWithTestDb) {
        val twoHoursAgo = new DateTime().minusHours(2).getMillis
        val unauthRequest = route(FakeRequest(GET, "/api/incidents").withHeaders(headers(apiToken = dbConfig("api.token"), timestamp = twoHoursAgo):_*)).get

        status(unauthRequest) must equalTo(UNAUTHORIZED)
      }
    }

    "return 401 on wrong token" in {
      running(FakeAppWithTestDb) {
        val unauthRequest = route(FakeRequest(GET, "/api/incidents").withHeaders(headers(apiToken = "WRONG"):_*)).get

        status(unauthRequest) must equalTo(UNAUTHORIZED)
      }
    }
  }

}
