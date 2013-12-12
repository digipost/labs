package no.digipost.labs

import com.typesafe.config.{ConfigParseOptions, Config, ConfigFactory}
import java.io.File
import no.digipost.labs.util.Logging

class Settings(val config: Config) {
  val environment = config.getString("environment")

  val mongoDatabase = config.getString("mongo.database")
  val mongoHost = config.getString("mongo.host")
  val mongoPort = config.getInt("mongo.port")

  val oauthClientId = config.getString("oauth.clientId")
  val oauthSecret = config.getString("oauth.secret")
  val oauthRedirectUrl = config.getString("oauth.redirectUrl")
  val oauthAccessTokenUrl = "https://www.digipost.no/post/api/oauth/accesstoken"
  val openIdRedirectUrl = config.getString("openid.redirectUrl")
  val openIdRealm = config.getString("openid.realm")
}

object Settings extends Logging {
  final val Prod = "prod"
  final val Test = "test"

  protected def loadDefaultConfig(): Config = ConfigFactory.load().getConfig("labs")

  protected def loadSecretConfig(): Config = {
    val secretLocation = sys.props.get("secrets").getOrElse("secret.conf")
    val parseOpts = ConfigParseOptions.defaults().setAllowMissing(false)
    ConfigFactory.parseFile(new File(secretLocation), parseOpts).getConfig("labs")
  }

  def load(): Settings = new Settings(loadSecretConfig().withFallback(loadDefaultConfig()))

  def load(env: Option[String]): Settings = env.fold(load())(env => load(env))

  def load(env: String): Settings = {
    val default = loadDefaultConfig()
    new Settings(loadSecretConfig().withFallback(default.getConfig(env)).withFallback(default))
  }

  def loadFromSystemEnvironmentProperty() = {
    val env = sys.props.get("env").flatMap(e => if (e.isEmpty) None else Some(e))
    info("Loading config for environment " + env.getOrElse("dev"))
    val settings = try {
      env map Settings.load getOrElse Settings.load()
    } catch {
      case e: Exception =>
        error(e)
        sys.exit(1)
    }
    info("'environment' defined in settings is: " + settings.environment)
    settings
  }

}
