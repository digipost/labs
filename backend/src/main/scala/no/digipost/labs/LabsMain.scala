package no.digipost.labs
import java.net.InetSocketAddress

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object LabsMain {
  def main(args: Array[String]) {
    val port = sys.props.get("port").map(_.toInt).getOrElse(7002)

    val server = new Server(InetSocketAddress.createUnresolved("127.0.0.1", port))

    val context = new WebAppContext()
    context.setConfigurations(Array(new LabsConfiguration))

    server.setHandler(context)
    server.start()
    server.join()
  }
}
