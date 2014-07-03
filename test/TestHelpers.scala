package test

import play.api.test.FakeApplication
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import play.api.libs.Codecs
import play.api.libs.json.JsObject
import play.api.libs.json.Json

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/29/13
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
trait TestConfig {
  val dbConfig = Map(
    "db.default.driver" -> "org.h2.Driver",
    "db.default.url" -> "jdbc:h2:mem:play",
    "db.default.user" -> "sa",
    "db.default.password" -> "",
    "elasticsearch.embedded" -> "true",
    "api.token" -> "fake"
  )

  def FakeAppWithTestDb = FakeApplication(additionalConfiguration = dbConfig)
}

trait TestRequest {
	private val SALT = "?#h|`nBg7qjg [)6pS(8p!RV%$;C@_=b6&b>>?TVj--o)?v[:IEhhkK|nZ2^Z7ag"

	def get(uri: String, apiToken: String = "fake") = route(FakeRequest(GET, uri).withHeaders(headers(apiToken):_*)).get

	def post(uri: String, json: JsObject, apiToken: String = "fake") = route(FakeRequest(POST, uri).withHeaders(headers(apiToken):_*).withJsonBody(json)).get

	def headers(apiToken: String, timestamp: Long = new DateTime().getMillis) = {
		val token = Codecs.sha1(timestamp + SALT + apiToken)
		Seq("Authorization" -> ("Token token=\"" + token + "\", timestamp=\"" +timestamp + "\""))
	}
}
