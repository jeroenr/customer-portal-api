package binders

import play.api.mvc.QueryStringBindable
import scala.util.{Failure, Success, Try}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by jeroen on 2/18/14.
 */
case class Period(from: DateTime, to: DateTime)

object Period {
  val FROM = "from"
  val TO = "to"
  val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
  val DTF = DateTimeFormat.forPattern(DATE_FORMAT).withZoneUTC

  implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[Period] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Period]] = {
      val result = for {
        from <- Try(stringBinder.bind(s"$key.$FROM", params).get).recover {
          case e => Right(DTF.print(new DateTime().minusYears(1)))
        }
        to <- Try(stringBinder.bind(s"$key.$TO", params).get).recover {
          case e => Right(DTF.print(new DateTime))
        }
      } yield {
        (DTF.parseDateTime(from.right.get), DTF.parseDateTime(to.right.get))
      }
      result match {
        case Success((fromDate, toDate)) =>
          Some(Right(Period(fromDate, toDate)))
        case Failure(e) => Some(Left(s"Invalid date params: ${e.getMessage}"))
      }
    }

    override def unbind(key: String, pager: Period): String = {
      stringBinder.unbind(s"$key.$FROM", DTF.print(pager.from)) + "&" + stringBinder.unbind(s"$key.$TO", DTF.print(pager.to))
    }
  }
}
