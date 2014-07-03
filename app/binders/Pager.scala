package binders

import play.api.mvc.QueryStringBindable
import util.{Try, Success, Failure}
import services.ParamValidator
import play.api.data.validation._
import play.api.i18n.Messages

case class Pager(offset: Int, size: Int)

object Pager {
  val NUM = "num"
  val SIZE = "size"
  val DEFAULT_NUM = 1
  // TODO: is this a safe default?
  val DEFAULT_SIZE = 1000000
  val CONSTRAINTS = Seq(ParamValidator.MIN_0, Constraints.max(50000000))
  val CONSTRAINTS_DESC = Pager.CONSTRAINTS.map(_.args.head).mkString("[",",","]")

  implicit def queryStringBinder(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[Pager] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pager]] = {
      val pagingKeys = Seq(s"$key.$NUM", s"$key.$SIZE")
      val pagingParams = pagingKeys.filter(params.keys.toSeq.contains(_))
      val result = for {
        num <- Try(intBinder.bind(pagingKeys(0), params).get).recover {
          case e => Right(DEFAULT_NUM)
        }
        size <- Try(intBinder.bind(pagingKeys(1), params).get).recover {
          case e => Right(DEFAULT_SIZE)
        }
      } yield {
        (num.right.toOption, size.right.toOption)
      }
      result match {
        case Success((maybeNum, maybeSize)) =>
          ParamValidator(CONSTRAINTS, maybeNum, maybeSize) match {
            case Valid =>
              Some(Right(Pager(maybeNum.get - 1, maybeSize.get)))
            case Invalid(errors) =>
              Some(Left(errors.zip(pagingParams).map {
                case (ValidationError(message, v), param) => Messages(message, param, v)
              }.mkString(", ")))
          }
        case Failure(e) => Some(Left(s"Invalid paging params: ${e.getMessage}"))
      }
    }

    override def unbind(key: String, pager: Pager): String = {
      intBinder.unbind(s"$key.$NUM", pager.offset + 1) + "&" + intBinder.unbind(s"$key.$SIZE", pager.size)
    }
  }
}