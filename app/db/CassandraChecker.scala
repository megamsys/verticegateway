package db

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._

import com.datastax.driver.core.Cluster
import com.websudos.phantom.connectors.{ KeySpace, SessionProvider }
import com.websudos.phantom.dsl._

import app.MConfig

class CassandraChecker {

  def check:ValidationNel[Throwable, String] = {
   val hosts = MConfig.cassandra_host
   val keyspace: KeySpace = KeySpace(MConfig.cassandra_keyspace)

    val cluster = Cluster.builder()
                  .addContactPoints(hosts)
                  .withCredentials(MConfig.cassandra_username, MConfig.cassandra_password)
                  .build

    (Validation.fromTryCatchThrowable[String, Throwable] {
      cluster.connect
      "cool buddy"
    } leftMap { t: Throwable => new RuntimeException("Cassandra: (" + hosts + keyspace + ") not available")}).toValidationNel
  }
}
