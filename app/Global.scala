/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 7/24/13
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */

import net.fromamsterdamwithlove.elasticsearch.services.{IndexServiceFactory, IndexService}
import org.squeryl.adapters.{OracleAdapter, MSSQLServer, H2Adapter, MySQLAdapter}
import play.api._
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import filters.{LoggingFilter => LogFilter, GzipFilter}
import utils.ConfigUtil

object Global extends WithFilters(LogFilter, GzipFilter) with GlobalSettings  {

  val SUPPORTED_DB_DRIVERS = Seq("org.h2.Driver","com.mysql.jdbc.Driver","net.sourceforge.jtds.jdbc.Driver", "oracle.jdbc.OracleDriver")

  override def onStart(app: Application) {
    initDbSessionFactory(app)
    initElasticSearchClient()
  }

  private def initElasticSearchClient() {
    val isEmbedded = ConfigUtil.getBooleanOrElse("elasticsearch.embedded", false)
    val hostName = ConfigUtil.getStringOrElse("elasticsearch.host", "localhost")
    val clusterName = ConfigUtil.getStringOrElse("elasticsearch.cluster.name", "elasticsearch")
    IndexServiceFactory.indexService = if (isEmbedded) IndexService() else IndexService(hostName, clusterName)
    if (isEmbedded) {
      Logger.info("Using embedded Elasticsearch instance")
    } else {
      Logger.info(s"Using elasticsearch cluster $clusterName at $hostName")
    }
    try {
      Logger.info(s"Elasticsearch cluster info: ${IndexServiceFactory.indexService.infoForNodes()}")
    } catch {
      case e: Exception =>
        sys.error(s"Couldn't connect to elasticsearch cluster $clusterName at ${ConfigUtil.getStringOrElse("elasticsearch.host", "embedded ES instance")}")
    }
  }

  private def initDbSessionFactory(app: Application) {
  	val driver = app.configuration.getString("db.default.driver")
    SessionFactory.concreteFactory = driver match {
      case Some("org.h2.Driver") => Some(() => getSession(new H2Adapter, app))
      case Some("com.mysql.jdbc.Driver") => Some(() => getSession(new MySQLAdapter, app))
      case Some("oracle.jdbc.OracleDriver") => Some(() => getSession(new OracleAdapter, app))
      case Some("net.sourceforge.jtds.jdbc.Driver") => Some(() => getSession(new MSSQLServer, app))
      case _ => sys.error(s"Database driver must one of ${SUPPORTED_DB_DRIVERS.mkString("(",", ",")")}, found $driver")
    }
  }

  private def getSession(adapter: DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)

}


