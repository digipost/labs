package no.digipost.labs
import org.scalatra.servlet.ScalatraListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.{Configuration, WebAppContext}
import org.eclipse.jetty.nosql.mongodb.{MongoSessionIdManager, MongoSessionManager}
import org.eclipse.jetty.server.session.SessionHandler
import com.mongodb.casbah.Imports._
import scala.concurrent.duration._
import javax.servlet.DispatcherType
import com.mongodb.DBCollection
import no.digipost.labs.util.ResponseTimeFilter
import java.net.InetSocketAddress
import java.net.InetAddress

object LabsMain {
  def main(args: Array[String]) {
    val port = sys.props.get("port").map(_.toInt).getOrElse(7002)

    val server = new Server(InetSocketAddress.createUnresolved("127.0.0.1", port))

    val context = new WebAppContext()

    //Setup persistent sessions using mongodb
    val settings = Settings.loadFromSystemEnvironmentProperty()
    val mongoClient = MongoClient(settings.mongoHost, settings.mongoPort)
    val db = mongoClient(settings.mongoDatabase)
    val sessions = db.getCollection("sessions")

    val idMgr = createMongoIdManager(server, sessions)
    server.setSessionIdManager(idMgr)
    context.setSessionHandler(new SessionHandler(createMongoSessionManager(idMgr)))

    context.setConfigurations(Array[Configuration]())
    context.setContextPath("/")
    context.addFilter(classOf[ResponseTimeFilter], "/*", java.util.EnumSet.of(DispatcherType.REQUEST))
    context.addEventListener(new ScalatraListener)

    server.setHandler(context)

    server.start()
    server.join()
  }


  def createMongoIdManager(server: Server, sessions: DBCollection): MongoSessionIdManager = {
    val idMgr = new MongoSessionIdManager(server, sessions)
    idMgr.setWorkerName("labs")
    idMgr.setScavengeDelay(1.day.toMillis)
    idMgr.setScavengePeriod(1.hour.toMillis)
    idMgr.setPurgeInvalidAge(1.day.toMillis)
    idMgr.setPurgeValidAge(0)
    idMgr
  }

  private def createMongoSessionManager(idMgr: MongoSessionIdManager): MongoSessionManager = {
    val sessionCookieMaxAge = 365.days
    val maxInavtiveTime = 182.days
    val mongoMgr = new MongoSessionManager()
    mongoMgr.setSessionIdManager(idMgr)
    mongoMgr.setHttpOnly(true)
    mongoMgr.setSecureRequestOnly(true)
    mongoMgr.getSessionCookieConfig.setHttpOnly(true)
    mongoMgr.getSessionCookieConfig.setSecure(true)
    mongoMgr.getSessionCookieConfig.setMaxAge(sessionCookieMaxAge.toSeconds.toInt)
    mongoMgr.setMaxInactiveInterval(maxInavtiveTime.toSeconds.toInt)
    mongoMgr
  }
}
