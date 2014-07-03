package services

import collection.Map
import net.fromamsterdamwithlove.collection.utils.MapImplicits
import MapImplicits._
import net.fromamsterdamwithlove.elasticsearch.domain.HistogramField

/**
 * Created by jeroen on 4/7/14.
 */
case class ValidSearchRequest(queryParameters: Map[String, AnyRef], filters: Iterable[Map[String,AnyRef]], facets: Iterable[Map[String,AnyRef]], statParameters: Iterable[String], histogramParameters: Iterable[HistogramField], frParameters: Iterable[HistogramField])

object ValidSearchRequest {
  def apply(parameters:(Map[String, AnyRef], Iterable[Map[String,AnyRef]], Iterable[Map[String,AnyRef]] , Iterable[String], Iterable[Map[String,String]], Iterable[Map[String,String]])): ValidSearchRequest = {
    val (queryParameters, filters, facets, statParameters, histogramParameters, frParameters) = parameters
    ValidSearchRequest(queryParameters, filters, facets, statParameters, histogramParameters.map(p => HistogramField(p)), frParameters.map(p => HistogramField(p)))
  }
}

object SearchRequestParser {
  def parse(requestParameters: Map[String, AnyRef]) = {
    val queryParameters = requestParameters.getMapOrEmpty[String, AnyRef]("query")
    val filters = requestParameters.getSeqOrEmpty[Map[String, AnyRef]]("filters")
    val facets = requestParameters.getSeqOrEmpty[Map[String, AnyRef]]("facets")
    val statParameters = requestParameters.getSeqOrEmpty[String]("stats")
    val histogramParameters = requestParameters.getSeqOrEmpty[Map[String, String]]("histograms")
    val frParameters = requestParameters.getSeqOrEmpty[Map[String, String]]("fr")
    ValidSearchRequest((queryParameters, filters, facets, statParameters, histogramParameters, frParameters))
  }

}
