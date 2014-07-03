package net.fromamsterdamwithlove.collection.utils

import collection.Map

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 10/29/13
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
object MapImplicits {
  implicit class NestedMap[T](map: Map[T,Any]) {
    def getMapOrEmpty[K,V](key: T): Map[K,V] = map.getOrElse(key, Map()).asInstanceOf[Map[K, V]]
    def getSeqOrEmpty[D](key: T)(implicit m : Manifest[D]): Seq[D] = map.getOrElse(key, Nil).asInstanceOf[Seq[D]]
    def getOrDefault[D](key: T, default: D)(implicit m : Manifest[D]): D = map.getOrElse(key, default).asInstanceOf[D]
    def getAsType[D](key: T)(implicit m : Manifest[D]) = map(key).asInstanceOf[D]
  }
}
