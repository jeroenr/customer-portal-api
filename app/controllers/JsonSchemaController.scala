package controllers

import play.api.mvc.Controller
import utils.JsonActions
import models.JsonSchemas

/**
 * Created by jeroen on 4/3/14.
 */
object JsonSchemaController extends Controller with JsonActions with JsonSchemas {
  def schema(schemaName: String) = UnauthorizedJsonGetAction {
    implicit request =>
      schemaMap.get(schemaName) match {
        case Some(jsonSchema) =>
          Ok(jsonSchema)
        case _ => NotFound
      }

  }
}
