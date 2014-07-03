package services

import org.specs2.mutable.Specification
import play.api.data.validation.{ValidationError, Invalid, Valid, Constraints}

/**
 * Created by jeroen on 2/13/14.
 */
class ParamValidatorSpec extends Specification {

  "ParamValidator" should {

    "accept no value" in {
      ParamValidator(List(Constraints.max(10))) must equalTo (Valid)
    }

    "accept a valid value" in {
      ParamValidator(List(Constraints.max(10)), Some(9)) must equalTo (Valid)
    }

    "refuse a value out of range" in {
      val result = Invalid(List(ValidationError("error.max", 10)))
      ParamValidator(List(Constraints.max(10)), Some(11)) must equalTo (result)
    }

    "validate multiple values" in {
      val constraints = List(Constraints.max(10), Constraints.min(1))
      val values = Seq(Some(9), Some(0), Some(5))
      val expected = Invalid(List(ValidationError("error.min", 1)))

      ParamValidator(constraints, values:_*) must equalTo (expected)
    }

    "validate multiple string values and multiple validation errors" in {
      val constraints = List(Constraints.maxLength(10), Constraints.minLength(1))
      val values = Seq(Some(""), Some("12345678910"), Some("valid"))
      val expected = Invalid(List(ValidationError("error.minLength", 1), ValidationError("error.maxLength", 10)))

      ParamValidator(constraints, values:_*) must equalTo (expected)
    }
  }
}
