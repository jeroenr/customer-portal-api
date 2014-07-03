package net.fromamsterdamwithlove.elasticsearch.services

import org.elasticsearch.action.search.SearchResponse
import scala.util.Try
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.search.facet.datehistogram.InternalFullDateHistogramFacet
import collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/13/13
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
trait FacetResultParser {
  def getFacetResults(searchResponse: SearchResponse) = {
    val facetMap = Try(searchResponse.getFacets.facetsAsMap.toMap).getOrElse(Map())
    facetMap.map {
      case (facetName, facet: TermsFacet) => Map(
        "field" -> facetName,
        "values" -> facet.getEntries.map {
          facetEntry =>
            Map(
              "value" -> facetEntry.getTerm.string,
              "count" -> facetEntry.getCount
            )
        }.toSeq)
      case (facetName, facet: InternalFullDateHistogramFacet) => Map(
        "field" -> facetName,
        "values" -> facet.getEntries.map {
          facetEntry =>
            Map(
              "total" -> facetEntry.getTotal,
              "totalcount" -> facetEntry.getTotalCount,
              "max" -> facetEntry.getMax,
              "min" -> facetEntry.getMin,
              "mean" -> facetEntry.getMean,
              "count" -> facetEntry.getCount,
              "time" -> facetEntry.getTime
            )
        }
      )
    }.toSeq.filterNot(x => x("values") == Nil)
  }
}

trait QueryResultParser {
  def getQueryResults(searchResponse: SearchResponse) = {
    val searchHits = searchResponse.getHits
    val searchResults = searchHits.hits.toSeq.map {
      searchHit => searchHit.sourceAsMap.toMap + ("id" -> searchHit.getId)
    }
    (searchHits.totalHits, searchResults)
  }
}
