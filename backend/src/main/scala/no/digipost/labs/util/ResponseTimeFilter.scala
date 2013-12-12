package no.digipost.labs.util

import javax.servlet._
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

class ResponseTimeFilter extends Filter {

  val log = LoggerFactory.getLogger(getClass)

  def destroy() = {

  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) = {
    val start = System.currentTimeMillis()
    chain.doFilter(request, response)
    val stop = System.currentTimeMillis() - start
    val req = request.asInstanceOf[HttpServletRequest]
    val method = req.getMethod
    val path = req.getPathInfo
    log.debug(s"$method $path took $stop ms")
  }

  def init(filterConfig: FilterConfig) = {

  }
}
