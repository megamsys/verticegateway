package app

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import db._
import controllers.Constants._
import io.megam.auth.funnel.FunnelErrors._
import models.base.{ RequestResult }

import io.megam.common.uid.UID
import io.megam.util.Time
import play.api.Logger
import play.api.Play._
import play.api.libs.json.Json
import play.Play;

/**
 * @author ram
 *
 */

case object Hellow {

  type THunt = (String, Option[String])

  case class Treasure(infra: Map[String, String],
    hunts: Map[String, THunt]) {

    val version = scala.io.Source.fromFile(Play.application().getFile("VERSION")).getLines().toList.map {x => (x.split("=").head, x.split("=").last)}.toMap

    val stat = (hunts.map { x => (x._1, x._2._2.getOrElse("down")) }).toMap

    val json = Json.prettyPrint(Json.toJson(Map(
      "about" -> version,
      "status" -> stat,
      "runtime" -> infra
      )))
    println(json)

  }

  val TOTAL_MEMORY = "total_mem"
  val FREE_MEMORY = "freemem"
  val SPACE = "freespace"
  val CPU_CORES = "cores"

  val NSQ       = "nsq"
  val CASSANDRA = "cassandra"

  val RUNNING   = "up"

  import java.lang.management.{ ManagementFactory, OperatingSystemMXBean }
  import java.lang.reflect.{ Method, Modifier }

  //the infra information, returned as tuples.
  val infra = ({
    val runtime = Runtime.getRuntime()
    val mxbean = ManagementFactory.getOperatingSystemMXBean

    import runtime.{ totalMemory, freeMemory, maxMemory, availableProcessors }
    List((TOTAL_MEMORY, (totalMemory / (1024 * 1024) + " MB")), (FREE_MEMORY, (freeMemory / (1024 * 1024)) + " MB"),

      (CPU_CORES, (mxbean.getAvailableProcessors()).toString)) ++
      (java.io.File.listRoots map { root =>
        (SPACE, root.getFreeSpace / (1024 * 1024 * 1024) + " of " + (root.getTotalSpace / (1024 * 1024 * 1024)) + " GB")
      }).toList
  }).toMap



  private  def topic(x: Unit) = "testing".some

  private def nsq = new wash.AOneWasher(new wash.PQd(topic, "test pub")).wash match {
    case Success(succ_uid) => (MConfig.nsqurl, Some(RUNNING))
    case Failure(erruid) => (MConfig.nsqurl, none)
  }

  private def cassandra = new CassandraChecker().check match {
    case Success(succ)  => (MConfig.cassandra_host, Some(RUNNING))
    case Failure(err)   => (MConfig.cassandra_host, none)
  }

  val sharks = Map(NSQ -> nsq, CASSANDRA -> cassandra)

  //super confusing, all we are trying to do is find the overal status by filte
  val sharkBite = sharks.values.filter(_._2.isEmpty)

  def buccaneer = Treasure(infra, sharks)

  val events = Map[String, String]("events" -> "none")

}
