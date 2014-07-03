package controllers

import play.api.mvc._

object Application extends Controller {

  val MAX_AGE = "86400" // 60 * 60 * 24

  def options(url: String) = Action {
    implicit request =>
      val origin = request.headers.get("Origin").getOrElse("*")
      NoContent.withHeaders(
        "Access-Control-Allow-Origin" -> origin,
        "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept, Authorization, User-Agent",
        "Access-Control-Allow-Credentials" -> "true",
        "Access-Control-Max-Age" -> MAX_AGE
      )
  }

}