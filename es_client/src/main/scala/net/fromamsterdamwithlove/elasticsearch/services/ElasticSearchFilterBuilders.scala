package net.fromamsterdamwithlove.elasticsearch.services

import collection.{Map,Seq}
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.index.query._
import org.apache.commons.lang3.StringUtils

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/9/13
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
trait FacetFilterBuilder {
  val EXCLUDING_CODE_VALUES = "(?!code_).*"

  def buildHistogramFilters(fieldNames: Iterable[(String, String, String, Iterable[Map[String,AnyRef]])], parameters: Map[String, AnyRef] = Map()) = {
    val allFacetTermFilters = queryParametersToTermFilterMap(parameters)
    fieldNames.map {
      case (keyField, valueField, interval, _) =>
        val facetTermFiltersExcludingCurrentField = allFacetTermFilters - keyField
        dateHistogramFacet(keyField).keyField(keyField).valueField(valueField).interval(interval).facetFilter(andFilter(facetTermFiltersExcludingCurrentField.values.toSeq: _*))
    }
  }

  def buildFieldStatFilters(fieldNames: Iterable[String], parameters: Map[String, AnyRef] = Map()) = {
    val allFacetTermFilters = queryParametersToTermFilterMap(parameters)
    buildMultiSelectFacetFilters(fieldNames.map{ fieldName => (fieldName,fieldName)},allFacetTermFilters)
  }

  def buildFacetFilters(facetParameters: Iterable[Map[String, AnyRef]], indexName: String, documentType: String, fields: Iterable[(String,String)], facetSpecificFilters: Map[String, Iterable[Map[String,AnyRef]]]) = {
    val allFacetTermFilters = facetParameters.map(toFilter(_)).toMap
    val facetSpecificTermFilters = facetSpecificFilters.mapValues { filters =>
      filters.map(toFilter(_)._2)
    }
    val facetBuilders = buildMultiSelectFacetFilters(fields,allFacetTermFilters, facetSpecificTermFilters)
    (allFacetTermFilters.values, facetBuilders)
  }


  def toFilter(filter: Map[String, AnyRef]) = {
    val fieldName = filter("field").asInstanceOf[String]
    val fieldValues = filter("values").asInstanceOf[Iterable[AnyRef]]
    filter.get("range").asInstanceOf[Option[Boolean]] match {
      case Some(true) =>
        val low :: high :: _ = Seq(null, null).patch(0, fieldValues.toSeq, fieldValues.size)
        (fieldName, rangeFilter(fieldName).gte(low).lte(high))
      case _ =>
        (fieldName, termsFilter(fieldName, fieldValues.toSeq: _*))
    }

  }

  private def queryParametersToTermFilterMap(parameters: Map[String, AnyRef] = Map()) = {
    parameters.map {
      case (fieldName, fieldValues:Iterable[AnyRef]) => fieldName -> termsFilter(fieldName, fieldValues.toSeq: _*)
      case (fieldName, fieldValue) => fieldName -> termsFilter(fieldName, fieldValue)
    }
  }

  private def buildMultiSelectFacetFilters(fields: Iterable[(String, String)], allFacetTermFilters: Map[String, FilterBuilder], facetSpecificFilters: Map[String, Iterable[FilterBuilder]] = Map()) = {
    fields.map {
      case (facetKey, fieldName) =>
        val facetTermFiltersExcludingCurrentField = allFacetTermFilters - fieldName
        val allFacetFilterBuilders = facetTermFiltersExcludingCurrentField.values.toSeq ++ facetSpecificFilters.getOrElse(facetKey, Seq())
        termsFacet(facetKey).field(fieldName).facetFilter(andFilter(allFacetFilterBuilders: _*)).regex(EXCLUDING_CODE_VALUES).allTerms(true)
    }
  }
}

trait QueryFilterBuilder {

  def buildQueryFilters(queryParameters: Map[String, AnyRef]): Iterable[FilterBuilder] = {
    queryParameters.map {
      case (fieldName, stringValue: String) => {
        val tokenizedValue = tokenizeString(stringValue)
        orFilter(termsFilter(fieldName, tokenizedValue), termsFilter(fieldName, tokenizedValue.map {v => s"code_${v}" }))
      }
      case (fieldName, range:Seq[AnyRef]) => {
        val low :: high :: _ = Seq(null, null).patch(0, range, range.size)
        rangeFilter(fieldName).gte(low).lte(high)
      }
      case (fieldName, value) => termsFilter(fieldName, value)
    }
  }

  private def tokenizeString(stringValue: String) = {
    StringUtils.split(StringUtils.lowerCase(stringValue), " -")
  }
}
