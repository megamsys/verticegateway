/*
** Copyright [2013-2016] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package app

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
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
/**
 * @author ram
 *
 */

case object Hellow {

  type THunt = (String, Option[String])

  case class Treasure(infra: Map[String, String],
    hunts: Map[String, THunt]) {

    //crude but for now its ok.
    val stat = (hunts.map { x => (x._1, x._2._2.getOrElse("down")) }).toMap
  //  val loady = scala.collection.immutable.TreeMap((mkps.list.flatten.sortWith(_.cattype < _.cattype).map { x =>
  //    (x.cattype + "." + x.name, x.image + "." + x.plans.size.toString + "," + x.id)
  //  }).toMap.toSeq: _*)

    val json = Json.prettyPrint(Json.toJson(Map("status" -> stat,
      "runtime" -> infra
      )))
    println(json)

  }

  val TOTAL_MEMORY = "total_mem"
  val FREE_MEMORY = "freemem"
  val SPACE = "freespace"
  val CPU_CORES = "cores"

  val NSQ = "nsq"
  val RIAK = "riak"
  val RUNNING = "up"

  val What2Hunts = Array(NSQ)

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

  //ping nsq, by droping a DUM0001 req
  private def nsq = new wash.AOneWasher(new wash.PQd(topic, "test pub")).wash match {
    case Success(succ_uid) => (MConfig.nsqurl, Some(RUNNING))
    case Failure(erruid) => (MConfig.nsqurl, none)
  }

  val sharks = Map(NSQ -> nsq)

  //super confusing, all we are trying to do is find the overal status by filte
  val sharkBite = sharks.values.filter(_._2.isEmpty)

//  private def mkps = models.base.MarketPlaces.listAll match {
//  case Success(succ_mkps) => succ_mkps
//    case Failure(errmkps) => MarketPlaceResults.empty
//  }

  def buccaneer = Treasure(infra, sharks)

  val events = Map[String, String]("events" -> "none")

}
