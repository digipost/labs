package no.digipost.labs.security

import org.scalatra.ScalatraServlet

trait SecurityHeaders { self: ScalatraServlet =>

  after() {
    response.headers += Headers.CacheControl -> "no-cache, no-store, no-transform"
    response.headers += Headers.XFrameOptions -> "deny"
    response.headers += Headers.XContentTypeOptions -> "nosniff"
    response.headers += Headers.XPermittedCrossDomainPolicies -> "master-only"
    response.headers += Headers.StrictTransportSecurity -> "max-age=31536000"
    response.headers += Headers.XXSSProtection -> "1; mode=block"
  }
}
