package app.db

import java.net.InetAddress

import com.datastax.driver.core.Cluster
import com.websudos.phantom.connectors.{KeySpace, SessionProvider}
import com.websudos.phantom.dsl.Session

import scala.collection.JavaConversions._
import app.MConfig

/**
 * Created by Thiago Pereira on 6/9/15.
 *
 * Cassandra Connector extends the [[SessionProvider]] from phantom-dsl,
 * establishing a connection to a secure cluster with username and password
 */
trait CassandraConnector extends SessionProvider {

  implicit val space: KeySpace = Connector.keyspace

  val cluster = Connector.cluster

  override implicit lazy val session: Session = Connector.session
}

object Connector {

  val hosts = MConfig.scyllaurl
  //val inets = hosts.map(InetAddress.getByName)
 
  val keyspace: KeySpace = KeySpace(MConfig.scylla_keyspace)

  val cluster =
    Cluster.builder()
      .addContactPoints("103.56.92.24")
      .withCredentials(MConfig.scylla_username, MConfig.scylla_password)
      .build()
   println(cluster)
  val session: Session = cluster.connect(keyspace.name)
}