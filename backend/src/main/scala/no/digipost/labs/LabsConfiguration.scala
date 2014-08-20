package no.digipost.labs

import javax.servlet.DispatcherType

import com.mongodb.DBCollection
import com.mongodb.casbah.Imports._
import no.digipost.labs.util.ResponseTimeFilter
import org.eclipse.jetty.nosql.mongodb.{MongoSessionManager, MongoSessionIdManager}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.webapp.{WebAppContext, AbstractConfiguration}
import org.scalatra.servlet.ScalatraListener
import scala.concurrent.duration._

class LabsConfiguration() extends AbstractConfiguration {

  override def configure(context: WebAppContext) {
    context.setResourceBase(".")

    //Setup persistent sessions using mongodb
    val settings = Settings.loadFromSystemEnvironmentProperty()
    val mongoClient = MongoClient(settings.mongoHost, settings.mongoPort)
    val db = mongoClient(settings.mongoDatabase)
    val sessions = db.getCollection("sessions")

    val server = context.getServer
    val idMgr = createMongoIdManager(server, sessions)
    server.setSessionIdManager(idMgr)
    context.setSessionHandler(new SessionHandler(createMongoSessionManager(idMgr)))

    context.setContextPath("/")
    context.addFilter(classOf[ResponseTimeFilter], "/*", java.util.EnumSet.of(DispatcherType.REQUEST))
    context.addEventListener(new ScalatraListener)
  }

  private def createMongoIdManager(server: Server, sessions: DBCollection): MongoSessionIdManager = {
    val idMgr = new MongoSessionIdManager(server, sessions)
    idMgr.setWorkerName("labs")
    idMgr.setScavengePeriod(1.hour.toMillis)
    idMgr.setPurgeInvalidAge(1.day.toMillis)
    idMgr.setPurgeValidAge(0)
    idMgr
  }

  private def createMongoSessionManager(idMgr: MongoSessionIdManager): MongoSessionManager = {
    val sessionCookieMaxAge = 365.days
    val maxInactiveTime = 182.days
    val mongoMgr = new MongoSessionManager()
    mongoMgr.setSessionIdManager(idMgr)
    mongoMgr.setHttpOnly(true)
    mongoMgr.setSecureRequestOnly(true)
    mongoMgr.getSessionCookieConfig.setHttpOnly(true)
    mongoMgr.getSessionCookieConfig.setSecure(true)
    mongoMgr.getSessionCookieConfig.setMaxAge(sessionCookieMaxAge.toSeconds.toInt)
    mongoMgr.setMaxInactiveInterval(maxInactiveTime.toSeconds.toInt)
    mongoMgr
  }

}
