package net.fromamsterdamwithlove.json.utils

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.core.report.ProcessingReport
import collection.JavaConversions._

/**
 * Created by jeroen on 4/2/14.
 */
case class JsonValidationResult(valid: Boolean, errors: Iterable[JsonNode])

object JsonValidator {
   val schemaFactory = JsonSchemaFactory.byDefault()

   def validateJson(schemaAsString: String, jsonString: String) = {
     val schemaAsJsonNode = JsonUtil.fromJson[JsonNode](schemaAsString)
     val jsonSchema = schemaFactory.getJsonSchema(schemaAsJsonNode)
     val json = JsonUtil.fromJson[JsonNode](jsonString)
     val report = jsonSchema.validate(json)
     JsonValidationResult(report.isSuccess, report.map(_.asJson))
   }

   def validateJsonFromRemoteSchema(schemaUrl: String, jsonString: String) = {
    val jsonSchema = schemaFactory.getJsonSchema(schemaUrl)
    val json = JsonUtil.fromJson[JsonNode](jsonString)
    val report = jsonSchema.validate(json)
    JsonValidationResult(report.isSuccess, report.map(_.asJson))
  }
}
