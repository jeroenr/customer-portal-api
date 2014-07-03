package net.fromamsterdamwithlove.elasticsearch.domain

/**
 * Created by jeroen on 2/11/14.
 */
object PagingOptions {
  val LARGE_SIZE = 100000000

  def apply(num: Option[Int] = None, size: Option[Int] = None): PagingOptions = PagingOptions(num.getOrElse(1) - 1, size.getOrElse(10))
  def limitPageSizeOrFetchAll(num: Option[Int], size: Option[Int] = None) = PagingOptions(num, Some(size.getOrElse(LARGE_SIZE)))
}

case class PagingOptions(offset: Int, size: Int) {
  def pages(total: Long) = if (size == 0) 1L else (total + size - 1) / size
}
