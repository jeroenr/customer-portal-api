package net.fromamsterdamwithlove.elasticsearch

import scalastic.elasticsearch.Indexer
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.node._, NodeBuilder._
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeExample, Step, Fragments}
import org.slf4j.LoggerFactory
import scala.util.Try
import java.io.File
import org.apache.commons.io.{FileDeleteStrategy,FileUtils}

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/13/13
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
trait UsingIndexer extends BeforeAllAfterAll with BeforeExample with TestElasticSearchClient {
  protected val logger = LoggerFactory.getLogger("net.fromamsterdamwithlove.elasticsearch.UsingIndexer")
  def indexName = getClass.getSimpleName.toLowerCase

  override def beforeAll = {
    client.start
  }

  override def before = {
    Seq(
      Try(client.deleteIndexIfExists(Seq(indexName))),
      Try(client.createIndex(indexName)),
      Try(client.waitTillActive(Seq(indexName)))
    ).foreach { _.recover { case e: Throwable => logger.debug("Ignoring these ES exceptions") } }

    client.count().getCount should be equalTo 0
  }

  override def afterAll = { 
    client.stop
    FileDeleteStrategy.FORCE.deleteQuietly(TestElasticSearchClient.tempEsHome)
  }

}

object TestElasticSearchClient {
  protected val logger = LoggerFactory.getLogger("net.fromamsterdamwithlove.elasticsearch.TestElasticSearchClient")
  val tempEsHome = new File(FileUtils.getTempDirectory, s"elasticsearch${System.nanoTime}")
  FileUtils.forceMkdir(tempEsHome)
  logger.info("Data dir: " + tempEsHome.toString)
  val c = Indexer.at(
    nodeBuilder.local(true).settings(settingsBuilder
      .put("path.home", tempEsHome.getAbsolutePath)
      .put("http.enabled", false)
    ).build
  )
}

trait TestElasticSearchClient {
  val client = TestElasticSearchClient.c
}

trait BeforeAllAfterAll extends Specification {
  // see http://bit.ly/11I9kFM (specs2 User Guide)
  override def map(fragments: => Fragments) =
    Step(beforeAll) ^ fragments ^ Step(afterAll)

  protected def beforeAll()

  protected def afterAll()
}

