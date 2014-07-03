package utils

import play.api.mvc._, BodyParsers._
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import controllers.SecurityController._
import play.api.libs.json.JsValue
import utils.ResultImplicits._
import net.fromamsterdamwithlove.json.utils.JsonValidator
import net.fromamsterdamwithlove.json.utils.JsonValidationResult
import play.api.mvc.Results._
import models.JsonSchemas

object JsonActions extends JsonActions

trait JsonActions extends JsonSchemas {


  val UNSUPPORTED_JSON_ERROR = Map(
    "error" -> "Unsupported JSON structure"
  )

  def JsonPostAction(jsonSchemaId: String, maxLength: Int)(f: Map[String,AnyRef] => Result) =  Authorized {
    Action(parse.json(maxLength = maxLength)) { implicit request =>
      val jsonString = request.body.toString
      JsonValidator.validateJson(schemaMap(jsonSchemaId), jsonString) match {
        case JsonValidationResult(true, _) => f(jsonString.toMapOf[AnyRef]).asJsonWithAccessControlHeaders
        case JsonValidationResult(false, errors) => BadRequest(Map(
          "exception" -> "The request body violates the schema definition",
          "schema_uri" -> s"${request.host}/schema/${jsonSchemaId}.json",
          "errors" -> errors
        ).toJson).asJsonWithAccessControlHeaders
      }
    }
  }

  def JsonPostAction(maxLength: Int)(f: Map[String,AnyRef] => Result) =  Authorized {
    Action(parse.json(maxLength = maxLength)) { implicit request =>
      val jsonString = request.body.toString
      f(jsonString.toMapOf[AnyRef]).asJsonWithAccessControlHeaders
    }
  }

  def JsonPostAction(jsonSchemaId: String)(f: Map[String,AnyRef] => Result):Action[JsValue] = JsonPostAction(jsonSchemaId, parse.DEFAULT_MAX_TEXT_LENGTH)(f)

  def JsonPostAction(f: Map[String,AnyRef] => Result):Action[JsValue] = JsonPostAction(parse.DEFAULT_MAX_TEXT_LENGTH)(f)

  def JsonGetAction(f: Request[AnyContent] => Result) = Authorized {
    Action { implicit request =>
      f(request).asJsonWithAccessControlHeaders
    }
  }

  def UnauthorizedJsonGetAction(f: Request[AnyContent] => Result) = Action {
    implicit request =>
      f(request).asJsonWithAccessControlHeaders
  }

  def JsonDeleteAction(f: Request[Option[Any]] => Result) = Authorized {
    Action(parse.empty) { implicit request =>
      f(request).asJsonWithAccessControlHeaders
    }
  }

  def wrapExceptionInJson(t: Throwable) = Map("exception" -> t.getMessage).toJson



}
