package utils

import play.api.mvc.{SimpleResult, Request, Result}

/**
 * Created by jeroen on 2/18/14.
 */
object ResultImplicits {
  val JSON_CONTENT_TYPE: String = "application/json"

  implicit class MyResult(result: Result){
    def asJsonWithAccessControlHeaders(implicit request: Request[Any]): Result = {
      result.as(JSON_CONTENT_TYPE).withHeaders(
        "Access-Control-Allow-Origin" -> request.headers.get("Origin").getOrElse("*"),
        "Access-Control-Allow-Credentials" -> "true"
      )
    }
  }

  implicit class MySimpleResult(result: SimpleResult){
    def asJsonWithAccessControlHeaders(implicit request: Request[Any]): SimpleResult = {
      result.as(JSON_CONTENT_TYPE).withHeaders(
        "Access-Control-Allow-Origin" -> request.headers.get("Origin").getOrElse("*"),
        "Access-Control-Allow-Credentials" -> "true"
      )
    }
  }

}
