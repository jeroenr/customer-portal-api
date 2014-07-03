package net.fromamsterdamwithlove.elasticsearch.services

import org.elasticsearch.index.query._
import scala.collection.Map
import net.fromamsterdamwithlove.elasticsearch.domain.{PagingOptions, HistogramField}
import scala.Some
import org.elasticsearch.search.facet.terms.TermsFacetBuilder
import scala.util.Try
import collection.JavaConversions._
import scalastic.elasticsearch.Indexer
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._

object ElasticSearchReportServiceFactory {
  def apply() = new ElasticSearchReportService with EmbeddedElasticSearchClient

  def apply(host: String = "localhost", clusterName: String) = new ElasticSearchReportService {
    val client: Indexer = IndexService.apply(host, clusterName)
  }
}

object ElasticSearchFieldMappingServiceFactory {
  def apply() = new ElasticSearchFieldMappingService with EmbeddedElasticSearchClient

  def apply(host: String = "localhost", clusterName: String) = new ElasticSearchFieldMappingService {
    val client: Indexer = IndexService.apply(host, clusterName)
  }
}

object ElasticSearchQueryServiceFactory {
  def apply() = new ElasticSearchQueryService with EmbeddedElasticSearchClient

  def apply(host: String = "localhost", clusterName: String) = new ElasticSearchQueryService {
    val client: Indexer = IndexService.apply(host, clusterName)
  }
}

trait ElasticSearchFieldMappingService extends ElasticSearchService  {
  val FIELDMAPPING_KEY = "properties"

  def fieldMapping(indexName: String, documentType: String) = {
    val result = for {
      mappings <- Try(client.metadataFor(indexName).mappings)
      mappingMetaData <- Try(mappings.get(documentType).sourceAsMap)
      properties <- Try(mappingMetaData.get(FIELDMAPPING_KEY).asInstanceOf[java.util.LinkedHashMap[String, java.util.LinkedHashMap[String, String]]])
    } yield {
      properties.map {
        property =>
          val (field, fieldProperties) = property
          fieldProperties.toMap.get("type") match {
            case Some(fieldType) => Map("name" -> field, "type" -> fieldType)
            case _ => Map[String,String]()
          }
      }
    }
    result.getOrElse(Seq()).filterNot(x => x.isEmpty)
  }

  def fieldNames(indexName:String, documentType: String) = fieldMapping(indexName, documentType).map { fieldMapping => fieldMapping("name") }

  def createFieldMapping(indexName: String, fieldMappings: Map[String, Map[String, Map[String, Any]]]) = {
    val resps = if(!client.exists(indexName).isExists){
      Seq(client.createIndex(index = indexName, mappings = fieldMappings.map {
        case (fieldType, mapping) => fieldType -> Map(fieldType -> Map(FIELDMAPPING_KEY -> mapping)).toJson
      }))
    } else {
      fieldMappings.map {
        case (fieldType, mapping) =>
          val json: String = Map(fieldType -> Map(FIELDMAPPING_KEY -> mapping)).toJson
          client.putMapping(index = indexName, `type` = fieldType, source = json, ignoreConflicts = Some(true))
      }
    }
    resps.forall {
      case resp: {def isAcknowledged: Boolean} => resp.isAcknowledged
      case _ => false
    }
  }
}

trait ElasticSearchQueryService extends ElasticSearchService with QueryFilterBuilder with FacetFilterBuilder with FacetResultParser with QueryResultParser {
  def all(indices: Iterable[String] = Nil, pagingOptions: PagingOptions = PagingOptions()) = {
    val searchResponse = client.search(indices = indices, size = Some(pagingOptions.size), from = Some(pagingOptions.offset))
    getQueryResults(searchResponse)
  }

  def search(indexName: String, documentType: String, queryParameters: Map[String, AnyRef], facetParameters: Iterable[Map[String, AnyRef]], fields: Iterable[(String,String)], facetSpecificFilters: Map[String, Iterable[Map[String,AnyRef]]] = Map(), pagingOptions: PagingOptions = PagingOptions()): (Long, Seq[Map[String, AnyRef]], Seq[Map[String, Object]]) = {

    val queryParameterFilters = buildQueryFilters(queryParameters)

    val (facetTermFilters, facetBuilders) = buildFacetFilters(facetParameters, indexName, documentType, fields, facetSpecificFilters)

    val allTermsFilters = queryParameterFilters ++ facetTermFilters

    performSearch(indexName, allTermsFilters, facetBuilders, pagingOptions)
  }

  def byId(indexName: String, documentType: String, id: String) = get(indexName: String, documentType: String, id: String)

  def delete(indices: Iterable[String] = Nil, queryParameters: Map[String, AnyRef]) = deleteByQuery(indices, buildQueryFilters(queryParameters))

  private def performSearch(indexName: String, allTermsFilters: Iterable[FilterBuilder], facetBuilders: Iterable[TermsFacetBuilder], pagingOptions: PagingOptions) = {
    val searchResponse = executeQuery(Seq(indexName), allTermsFilters, facetBuilders, size = Some(pagingOptions.size), offset = Some(pagingOptions.offset))
    val facetResults = getFacetResults(searchResponse)
    val (total, searchResults) = getQueryResults(searchResponse)
    (total, searchResults, facetResults)
  }
}
trait ElasticSearchReportService extends ElasticSearchService with QueryFilterBuilder with FacetFilterBuilder with FacetResultParser{

  def getStats(indexName: String, documentType: String, queryParameters: Map[String,AnyRef], facetParameters: Iterable[Map[String, AnyRef]], histogramFields: Iterable[HistogramField], fieldStatFields: Iterable[String]) = {
    val allParams = queryParameters ++ facetParameters.groupBy(_("field").asInstanceOf[String]).map {
      case (key, values) => key -> values.map(_("values").asInstanceOf[Iterable[AnyRef]]).flatten
    }

    val facetBuilders = buildHistogramFilters(histogramFields.map(x => HistogramField.unapply(x).get), allParams) ++ buildFieldStatFilters(fieldStatFields, allParams)

    val searchResponse = executeQuery(Seq(indexName), Seq(), facetBuilders, size = Some(0))
    val facetResults = getFacetResults(searchResponse)
    facetResults.map { facetResult =>
      (facetResult("field"), facetResult("values"))
    }.toMap
  }
}

