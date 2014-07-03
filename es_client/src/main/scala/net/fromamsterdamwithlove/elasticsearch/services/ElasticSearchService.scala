package net.fromamsterdamwithlove.elasticsearch.services

import scalastic.elasticsearch.Indexer
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.search.facet.FacetBuilder
import org.elasticsearch.action.search.SearchResponse
import collection.JavaConversions._
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse

trait ElasticSearchService {
  val client: Indexer

  protected[services] def executeQuery(indices: Iterable[String], allFilters: Iterable[FilterBuilder], facetBuilders: Iterable[FacetBuilder], size:Option[Int] = Some(10), offset:Option[Int] = Some(0)): SearchResponse = {
    client.search(indices = indices, query = matchAllQuery, filter = Some(andFilter(allFilters.toSeq: _*)), facets = facetBuilders, size = size, from = offset)
  }

  protected[services] def index(indexName: String, documentType: String, documents: Iterable[(String, String)]) = {
    documents.map {
      case (id, source) => client.index(index = indexName, `type` = documentType, id = id, source = source, create = Some(false), refresh = Some(true))
    }
  }

  protected[services] def bulk(indexName: String, documentType: String, documents: Iterable[(String, String)]) = {
    val indexRequests = documents.map {
      case (id, source) => client.index_prepare(index = indexName, `type` = documentType, id = id, source = source).request()
    }
    client.bulk(indexRequests)
  }

  protected[services] def get(indexName: String, documentType: String, id:String) = {
    client.get(index = indexName, `type` = documentType, id = id).getSource.toMap
  }

  protected[services] def deleteByQuery(indices: Iterable[String], allFilters: Iterable[FilterBuilder]): DeleteByQueryResponse = {
    client.deleteByQuery(indices = indices, query = filteredQuery(matchAllQuery, andFilter(allFilters.toSeq: _*)))
  }
}

trait EmbeddedElasticSearchClient {
  lazy val client = IndexService()
}

object IndexServiceFactory {
  var indexService: Indexer = _
}

object IndexService {
  def apply() = Indexer.local
  def apply(host:String = "localhost", clusterName: String) = Indexer.transport(Map("cluster.name" -> clusterName), host = host)
}
