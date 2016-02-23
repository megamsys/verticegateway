package db

import java.net.InetAddress

import com.datastax.driver.core.Cluster
import com.websudos.phantom.connectors.{ KeySpace, SessionProvider }
import com.websudos.phantom.dsl.Session
import com.websudos.phantom.dsl._

import scala.collection.JavaConversions._
import app.MConfig

trait ScyllaConnector extends SessionProvider with RootConnector {

  implicit val space: KeySpace = Connector.keyspace
  val cluster = Connector.cluster
  override implicit lazy val session: Session = Connector.session
}

object Connector {

  val hosts = MConfig.scyllaurl
  val keyspace: KeySpace = KeySpace(MConfig.scylla_keyspace)

  val cluster =
    Cluster.builder()
      .addContactPoints("103.56.92.24")
      .withCredentials(MConfig.scylla_username, MConfig.scylla_password)
      .build()
  println(cluster)
  val session: Session = cluster.connect(keyspace.name)
}

object scyllaConnection extends ScyllaConnector {}
