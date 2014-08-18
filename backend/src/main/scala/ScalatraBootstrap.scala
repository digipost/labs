import com.mongodb.casbah.Imports._
import no.digipost.labs.items.{ItemsService, ItemsResource, MongoItemsRepository}
import no.digipost.labs.legacy.{LegacyRedirectResource}
import no.digipost.labs.login.SessionsResource
import no.digipost.labs.oauth.LoginWithDigipost
import no.digipost.labs._
import no.digipost.labs.openid.OpenIdConsumer
import no.digipost.labs.users.{MongoUsersRepository, UsersResource}
import no.digipost.labs.util.Logging
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle with Logging {
  override def init(context: ServletContext) {
    val settings = Settings.loadFromSystemEnvironmentProperty()

    val mongoClient = MongoClient(settings.mongoHost, settings.mongoPort)
    val db = mongoClient(settings.mongoDatabase)
    val itemsColl = db("items")
    itemsCollIndexes.map(itemsColl.ensureIndex(_))
    val itemsRepo = new MongoItemsRepository(itemsColl)
    val userColl = db("users")
    val usersRepo = new MongoUsersRepository(userColl)
    val itemsService = new ItemsService(itemsRepo)
    
    context.mount(new ItemsResource(itemsService), "/*")
    context.mount(new SessionsResource(settings, new LoginWithDigipost, new OpenIdConsumer, usersRepo), "/sessions/*")
    context.mount(new UsersResource(usersRepo), "/users/*")
    
    context.mount(new LegacyRedirectResource(settings, itemsService), "/legacy/*")
  }

  lazy val itemsCollIndexes = List(
    MongoDBObject("title" -> "text", "body" -> "text"), MongoDBObject("default_language" -> "norwegian"),
    MongoDBObject("type" -> 1),
    MongoDBObject("index" -> -1, "date" -> -1)
  )
}
