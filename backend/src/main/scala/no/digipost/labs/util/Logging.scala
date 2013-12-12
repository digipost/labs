package no.digipost.labs.util

import org.slf4j.LoggerFactory

trait Logging {
  lazy val log = LoggerFactory.getLogger(getClass)

  def debug(msg: Any*) = if (log.isDebugEnabled) msg.foreach {
    case t: Throwable => log.debug(t.getMessage, t)
    case m => log.debug(m.toString)
  }
  def info(msg: Any*) = if (log.isInfoEnabled) msg.foreach {
    case t: Throwable => log.info(t.getMessage, t)
    case m => log.info(m.toString)
  }
  def warn(msg: Any*) = if (log.isWarnEnabled) msg.foreach {
    case t: Throwable => log.warn(t.getMessage, t)
    case m => log.warn(m.toString)
  }
  def error(msg: Any*) = if (log.isErrorEnabled) msg.foreach {
    case t: Throwable => log.error(t.getMessage, t)
    case m => log.error(m.toString)
  }

}
