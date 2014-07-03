package net.fromamsterdamwithlove.elasticsearch.services

import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import scalastic.elasticsearch.Indexer
import collection.Map
import net.fromamsterdamwithlove.elasticsearch.utils.IndexUtility
import scala.util.Try

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/13/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
object ElasticSearchIndexerFactory {
  def apply(): ElasticSearchIndexer = new ElasticSearchIndexer {
    val client: Indexer = Indexer.local
  }

  def apply(host: String = "localhost", clusterName: String): ElasticSearchIndexer = new ElasticSearchIndexer {
    val client: Indexer = IndexService.apply(host, clusterName)
  }
}

trait ElasticSearchIndexer extends ElasticSearchService {
  def exists(indices: String*) = client.exists(indices:_*).isExists

  def createIndexWithSettings(indexName: String, settings: String) = if(!exists(indexName)) IndexUtility.createIndexWithSettings(client, indexName, settings)

  def createIndexWithSettings(indexName: String, settings: Map[String, AnyRef]) = if(!exists(indexName)) IndexUtility.createIndexWithSettings(client, indexName, settings.toJson)

  def createOrUpdate(indexName: String, documentType: String, id: String, fields: Map[String, AnyRef]) = {
    val mergedFields = Try(get(indexName, documentType, id)).getOrElse(Map[String,AnyRef]()) ++ fields
    index(indexName, documentType, Seq((id,mergedFields.toJson)))
  }

  def bulkCreate(indexName: String, documentType: String, fields: Map[String, Map[String, AnyRef]]) = {
    val bulkResponse = bulk(indexName, documentType, fields.map {
      case (id, fieldMap) => (id, fieldMap.toJson)
    }.toIterable)
    (bulkResponse.hasFailures, bulkResponse.buildFailureMessage())
  }

  def deleteAll(indices: Iterable[String] = Nil) = client.deleteIndex(indices = indices)

}
