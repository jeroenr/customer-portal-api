package services

import play.api.data.validation.{Constraint, Invalid, Valid, Constraints}

/**
 * Created by jeroen on 2/7/14.
 */
object ParamValidator {
  val MIN_0 = Constraints.min(0)

  def apply[T](constraints: Iterable[Constraint[T]], optionalParam: Option[T]*) =
    optionalParam.flatMap { _.map { param =>
        constraints flatMap {
          _(param) match {
            case i:Invalid => Some(i)
            case _ => None
          }
        }
      }
    }.flatten match {
      case Nil => Valid
      case invalids => invalids.reduceLeft {
        (a,b) => a ++ b
      }
    }
}

