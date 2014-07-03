package services

import net.fromamsterdamwithlove.elasticsearch.services._
import utils.ConfigUtil
import net.fromamsterdamwithlove.elasticsearch.utils.IndexUtility
import scala.collection
import net.fromamsterdamwithlove.elasticsearch.domain.PagingOptions

/**
 * Created by jeroen on 1/27/14.
 */
trait ElasticSearchClient {
  val client = IndexServiceFactory.indexService
}

object ElasticSearchFieldMappingService extends ElasticSearchFieldMappingService with ElasticSearchClient

object ElasticSearchQueryService extends ElasticSearchQueryService with ElasticSearchClient

object ElasticSearchReportService extends ElasticSearchReportService with ElasticSearchClient

object ElasticSearchIndexer extends ElasticSearchIndexer with ElasticSearchClient {
  val USE_KEYWORD_ANALYZER_BY_DEFAULT = """{"index":{"analysis":{"analyzer":{"default":{"type":"keyword"}}},"mapper":{"dynamic": true}}}"""

  def ensureIndexWithDefaultSettings(indexName: String) = {
     if(!exists(indexName)) IndexUtility.createIndexWithSettings(client, indexName, settings = USE_KEYWORD_ANALYZER_BY_DEFAULT)
  }
}

