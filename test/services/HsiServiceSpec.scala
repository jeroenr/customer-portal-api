package services

import org.specs2.mutable.Specification
import org.joda.time._
import test.TestConfig
import play.api.test.Helpers._

/**
 * Created by jeroen on 5/1/14.
 */
class HsiServiceSpec extends Specification with TestConfig{
  private val TODAY = new DateTime().withTimeAtStartOfDay

  "HsiService" should {

    "have 0 for frequency rate when no working hours in that month" in {
      running(FakeAppWithTestDb) {
        HsiService.frequencyRateForMonth(1000000, Map())(Map(), TODAY, 1) must equalTo(0.0)
      }
    }

    "calculate correct frequency rate in a month" in {
      running(FakeAppWithTestDb) {
        val workingHours = Map(TODAY -> 900000L)
        val fakeData = Map(TODAY -> 2d)
        HsiService.frequencyRateForMonth(1000000, workingHours)(fakeData, TODAY, 1) must equalTo(0.18518444444444443)
      }
    }
  }
}
