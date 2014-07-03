package net.fromamsterdamwithlove.elasticsearch

import net.fromamsterdamwithlove.elasticsearch.services.{ElasticSearchFieldMappingService, ElasticSearchQueryService}
import org.specs2.mutable.Specification

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/29/13
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
class QueryServiceTest extends Specification with UsingIndexer {

  "ElasticSearchQueryService" should {
    "return all results" in {
       val queryService = new ElasticSearchQueryService with TestElasticSearchClient
       val fieldMappingService = new ElasticSearchFieldMappingService with TestElasticSearchClient
      val documentType = "incident"
      val (total, results, facets) = queryService.search(indexName, documentType, Map[String,AnyRef](), Seq(), fieldMappingService.fieldNames(indexName, documentType).map { fieldName => (fieldName, fieldName) })
       total must equalTo(0)
       results must equalTo(Seq())
       facets must equalTo(Seq())
    }

  }

}
