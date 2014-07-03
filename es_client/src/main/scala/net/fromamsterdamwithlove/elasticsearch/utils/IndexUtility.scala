package net.fromamsterdamwithlove.elasticsearch.utils

import scalastic.elasticsearch.Indexer

/**
 * Created by jeroen on 2/3/14.
 */
object IndexUtility {
  def createIndexWithSettings(client: Indexer, indexName: String, settings: String) = {
    client.createIndex(indexName)
    client.waitForYellowStatus()
    client.closeIndex(indexName)
    client.waitForGreenStatus()
    client.updateSettings(settings, indexName)
    client.openIndex(indexName)
    client.waitForYellowStatus()
  }
}
