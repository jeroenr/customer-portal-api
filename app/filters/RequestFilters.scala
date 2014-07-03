package filters

import play.api.mvc._
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.filters.gzip.{GzipFilter => PlayGzipFilter}

object GzipFilter extends PlayGzipFilter

object LoggingFilter extends Filter {
  def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader) = {
    val start = System.currentTimeMillis

    def logTime(result: SimpleResult) = {
      val time = System.currentTimeMillis - start
      Logger.info(s"${rh.method} ${rh.uri} took ${time}ms and returned ${result.header.status}")
      result.withHeaders("Request-Time" -> time.toString)
    }

    next(rh) match {
      case plain: SimpleResult => Future.successful(logTime(plain))
      case async: Future[SimpleResult] => async.map(logTime)
    }
  }
}
