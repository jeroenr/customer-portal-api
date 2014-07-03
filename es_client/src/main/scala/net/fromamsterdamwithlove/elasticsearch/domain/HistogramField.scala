package net.fromamsterdamwithlove.elasticsearch.domain

import collection.Map
import net.fromamsterdamwithlove.collection.utils.MapImplicits._

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/10/13
 * Time: 9:59 AM
 * To change this template use File | Settings | File Templates.
 */
case class HistogramField(keyField: String, valueField: String, interval: String, filters: Iterable[Map[String, AnyRef]])

object HistogramField {
  val DEFAULT_INTERVAL = "month"

  def apply(keyField: String): HistogramField = {
    HistogramField(keyField, keyField, DEFAULT_INTERVAL)
  }
  def apply(keyField: String, valueField: String): HistogramField = {
     HistogramField(keyField, valueField, DEFAULT_INTERVAL)
  }
  def apply(keyField: String, valueField: String, interval: String): HistogramField = {
     HistogramField(keyField, valueField, interval, Seq())
  }
  def apply(map: Map[String, String]): HistogramField = {
    val key = map("key")
    val interval = map.getOrElse("interval", DEFAULT_INTERVAL)
    HistogramField(key, map.getOrElse("value", key), interval, map.getSeqOrEmpty[Map[String,AnyRef]]("filters"))
  }
}
