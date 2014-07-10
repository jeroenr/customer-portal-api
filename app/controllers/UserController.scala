package controllers

import play.api.mvc._
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import net.fromamsterdamwithlove.collection.utils.MapImplicits
import MapImplicits._
import utils.ConfigUtil
import utils.JsonActions
import scala.util.Try
import net.eamelink.swaggerkit._
import net.eamelink.swaggerkit.SimpleTypes._
import services._
import org.elasticsearch.indices.IndexMissingException
import scala.collection.Map
import scala.util.Failure
import net.eamelink.swaggerkit.Operation
import net.eamelink.swaggerkit.Api
import scala.util.Success
import play.api.Logger
import net.fromamsterdamwithlove.elasticsearch.domain.PagingOptions
import binders.{Period, Pager}
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 7/22/13
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
object UserController extends Controller with JsonActions {

  lazy val usersApi = Api("/users") describedBy "An users API" withOperations(listOperation, createOperation, deleteAllOperation)
  lazy val usersFieldMappingApi = Api("/users/fieldmapping") describedBy "An user fieldmapping API" withOperations(listFieldMappingOperation, createFieldMappingOperation)
  lazy val usersSearchApi = Api("/users/search") describedBy "An user search API" withOperations (searchOperation)
  lazy val userApi = Api("/users/{userId}") describedBy "An user API" withOperations(showOperation, updateOperation, deleteOperation)

  lazy val listFieldMappingOperation = Operation("listFieldMapping", GET, "List the field mapping for users")
  lazy val createFieldMappingOperation = Operation("createFieldMapping", PUT, "Update an user index with a certain field mapping") takes (
    BodyParam(String) is "/schema/user-fieldmapping.json"
    )
  lazy val searchOperation = Operation("searchusers", POST, "Search for users") takes(
    QueryParam("page.num", String) is Pager.CONSTRAINTS_DESC,
    QueryParam("page.size", String) is Pager.CONSTRAINTS_DESC,
    BodyParam(String) is "/schema/user-search.json"
  )

  lazy val hsiSearchOperation = Operation("showHsiStats", POST, "Show HSI results for users") takes(
    QueryParam("datefield", String) is "The field to treat as datefield (default: userDate)",
    BodyParam(String) is "/schema/user-hsi.json"
  )

  lazy val listOperation = Operation("listuser", GET, "List all users") takes(
    QueryParam("page.num", String) is Pager.CONSTRAINTS_DESC,
    QueryParam("page.size", String) is Pager.CONSTRAINTS_DESC
    )
  lazy val showOperation = Operation("showUser", GET, "Get an user") takes (
    PathParam("userId", String) is "The user id"
    )
  lazy val createOperation = Operation("createOrUpdateUser", POST, "Create an user") takes(
    QueryParam("idfield", String) is "The field to use as ID for the user",
    BodyParam(String)
    )
  lazy val deleteOperation = Operation("deleteUser", DELETE, "Delete user") takes(
    PathParam("userId", String) is "The user id",
    QueryParam("idfield", String) is "The field to use as ID for the user"
    )
  lazy val deleteAllOperation = Operation("deleteAll", DELETE, "Delete all users")
  lazy val bulkCreateOperation = Operation("bulkCreateUser", POST, "Bulk create users") takes(
    QueryParam("idfield", String) is "The field to use as ID for the user",
    BodyParam(String)
    )
  lazy val updateOperation = Operation("createOrUpdateUser", PUT, "Update an user") takes(
    PathParam("userId", String) is "The user id",
    BodyParam(String)
    )

  val DEFAULT_ID_FIELD = "id"
  val INDEX_NAME = ConfigUtil.getStringOrElse("elasticsearch.index.name", "users")
  val DEFAULT_DOCUMENT_TYPE = "user"

  val ONE_HUNDRED_MB_IN_BYTES = 1024 * 1024 * 100

  def search(page: Pager) = JsonPostAction("user-search") {
    implicit parameterMap => {
      SearchRequestParser.parse(parameterMap) match {
        case ValidSearchRequest(queryParameters, filters, facets, _, _, _) => {
          val result = for {
            pagingOptions <- Try(PagingOptions(page.offset, page.size))
            facetFields <- Try(facets.map{ facet =>
              val fieldName = facet("field").asInstanceOf[String]
              (facet.getOrElse("key",fieldName).asInstanceOf[String], fieldName)
            })
            facetSpecificFilters <- Try(facets.map { x =>
              x.getOrElse("key", x("field")).asInstanceOf[String] -> x.getOrElse("filters", Seq()).asInstanceOf[Iterable[Map[String,AnyRef]]]
            }.toMap)
            fields <- Try(if(facetFields.isEmpty) ElasticSearchFieldMappingService.fieldNames(INDEX_NAME, DEFAULT_DOCUMENT_TYPE).map { fieldName => (fieldName, fieldName) } else facetFields)
            (total, searchResults, facetResults) <- Try(ElasticSearchQueryService.search(INDEX_NAME, DEFAULT_DOCUMENT_TYPE, queryParameters, filters, fields, facetSpecificFilters, pagingOptions))
          } yield {
            Map(
              "total" -> total,
              "pages" -> pagingOptions.pages(total),
              "hits" -> searchResults.map {
                searchResult => searchResult + ("uri" -> s"/api/users/${searchResult("id")}")
              },
              "facets" -> facetResults
            ).toJson
          }
          result match {
            case Failure(fail: IndexMissingException) => Ok(
              Map("total" -> 0, "hits" -> Seq(), "facets" -> Seq()).toJson
            )
            case Failure(fail) =>
              Logger.error(s"Couldn't forfill search request", fail)
              InternalServerError(wrapExceptionInJson(fail))
            case Success(jsonResponse) => Ok(jsonResponse)
          }
        }
      }
    }
  }

  def fieldMapping = JsonGetAction {
    implicit request =>
      val mappings = ElasticSearchFieldMappingService.fieldMapping(INDEX_NAME, DEFAULT_DOCUMENT_TYPE)
      Ok(Map(
        "fields" -> mappings
      ).toJson)
  }

  def createFieldMappings = JsonPostAction("user-fieldmapping") {
    implicit parameterMap =>
      ElasticSearchIndexer.ensureIndexWithDefaultSettings(INDEX_NAME)
      val createdMappings = Try(ElasticSearchFieldMappingService.createFieldMapping(INDEX_NAME, Map(DEFAULT_DOCUMENT_TYPE -> parameterMap("fields").asInstanceOf[Map[String, Map[String, Any]]])))
      createdMappings match {
        case Failure(f: NoSuchElementException) => BadRequest(Map("exception" -> s"Json does not contain 'fields' field").toJson)
        case Failure(f) =>
          Logger.error(s"Failed to create field mapping", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success(true) => NoContent
        case Success(_) => Accepted
      }
  }

  def delete(id: String, idField: Option[String]) = JsonDeleteAction {
    implicit request =>
      val isDeleted = Try(ElasticSearchQueryService.delete(Seq(INDEX_NAME), Map(idField.getOrElse(DEFAULT_ID_FIELD) -> id)))
      isDeleted match {
        case Failure(f: IndexMissingException) => Ok(Map("message" -> f.getMessage).toJson)
        case Failure(f) =>
          Logger.error(s"Failed to delete user [$id]", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success(_) => Accepted
      }
  }

  def deleteAll = JsonDeleteAction {
    implicit request =>
      val areDeleted = Try(ElasticSearchIndexer.deleteAll(Seq(INDEX_NAME)))
      areDeleted match {
        case Failure(f: IndexMissingException) => Ok(Map("message" -> f.getMessage).toJson)
        case Failure(f) =>
          Logger.error(s"Failed to delete all users", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success(deleteResp) if (deleteResp.isAcknowledged) => NoContent
        case Success(_) => Accepted
      }
  }

  def list(page: Pager) = JsonGetAction {
    implicit request => {
      val pagingOptions = PagingOptions(page.offset, page.size)
      val areFetched = Try(ElasticSearchQueryService.all(Seq(INDEX_NAME), pagingOptions))
      areFetched match {
        case Failure(f: IndexMissingException) => Ok(Map(
          "total" -> 0,
          "pages" -> 0,
          "hits" -> Seq()
        ).toJson)
        case Failure(f) =>
          Logger.error(s"Failed to list users", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success((total, searchResults)) => {
          Ok(Map(
            "total" -> total,
            "pages" -> pagingOptions.pages(total),
            "hits" -> searchResults
          ).toJson)
        }
      }
    }
  }

  def create(idField: Option[String]) = JsonPostAction {
    implicit parameterMap => {
      val idFieldToUse = idField.getOrElse(DEFAULT_ID_FIELD)
      ElasticSearchIndexer.ensureIndexWithDefaultSettings(INDEX_NAME)
      val isCreated = Try(ElasticSearchIndexer.createOrUpdate(INDEX_NAME, DEFAULT_DOCUMENT_TYPE, parameterMap.getAsType[String](idFieldToUse), parameterMap))
      isCreated match {
        case Failure(f: NoSuchElementException) => BadRequest(Map("exception" -> s"Json does not contain id field '$idFieldToUse'").toJson)
        case Failure(f: ClassCastException) => BadRequest(Map("exception" -> s"Id field '$idFieldToUse' must have a string value").toJson)
        case Failure(f) =>
          Logger.error("Failed to create user", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success(_) => Created
      }
    }
  }

  def update(id: String, idField: Option[String]) = JsonPostAction {
    implicit parameterMap => {
      ElasticSearchIndexer.ensureIndexWithDefaultSettings(INDEX_NAME)
      val isCreated = Try(ElasticSearchIndexer.createOrUpdate(INDEX_NAME, DEFAULT_DOCUMENT_TYPE, id, parameterMap + (idField.getOrElse(DEFAULT_ID_FIELD) -> id)))
      isCreated match {
        case Failure(f) =>
          Logger.error(s"Failed to update user [$id]", f)
          InternalServerError(wrapExceptionInJson(f))
        case Success(_) => Ok
      }
    }
  }

  def byId(id: String) = JsonGetAction {
    implicit request =>
      val isFetched = Try(ElasticSearchQueryService.byId(INDEX_NAME, DEFAULT_DOCUMENT_TYPE, id))
      isFetched match {
        case Failure(f: NullPointerException) => NotFound
        case Failure(f) =>
          Logger.error(s"Couldn't retrieve user [$id]", f)
          NotFound(wrapExceptionInJson(f))
        case Success(map) => Ok(map.toJson)
      }
  }

}
