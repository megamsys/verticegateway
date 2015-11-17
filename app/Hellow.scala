/*
** Copyright [2013-2015] [Megam Systems]
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
import controllers.funnel.FunnelErrors._
import models.team.{Organizations, OrganizationsResults}
import models.base.{RequestResult, MarketPlaceResults}

import org.megam.common.uid.UID
import org.megam.util.Time
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
    hunts: Map[String, THunt],
    mkps: MarketPlaceResults) {

   //crude but for now its ok.
    val stat = (hunts.map { x => (x._1, x._2._2.getOrElse("fried")) }).toMap
    val load = (mkps.list.flatten.sortWith(_.cattype < _.cattype).map { x =>
        (x.cattype +"."+x.name, x.id  + "|"  + x.image + "." + x.plans.size.toString)
      }).toMap
    val json =  Json.prettyPrint(Json.toJson(Map("status" -> stat,
        "runtime" -> infra,
        "loaded" ->  load)))
   println(json)

  }

  val TOTAL_MEMORY = "total_mem"
  val FREE_MEMORY = "freemem"
  val SPACE = "freespace"
  val CPU_CORES = "cores"

  val SNOWFLAKE = "snowflake"
  val RABBITMQ = "rabbitmq"
  val RIAK = "riak"
  val RUNNING = "up"

  val What2Hunts = Array(RIAK, SNOWFLAKE, RABBITMQ)

  import java.lang.management.{ ManagementFactory, OperatingSystemMXBean }
  import java.lang.reflect.{ Method, Modifier }

  //the infra information, returned as tuples.
  val infra = ({
    val runtime = Runtime.getRuntime()
    val mxbean = ManagementFactory.getOperatingSystemMXBean

    import runtime.{ totalMemory, freeMemory, maxMemory, availableProcessors }
    List((TOTAL_MEMORY, (totalMemory / (1024 * 1024) + " MB")), (FREE_MEMORY, (freeMemory / (1024 * 1024)) + " MB"),

    (CPU_CORES, (mxbean.getAvailableProcessors()).toString))   ++
      (java.io.File.listRoots map { root =>
        (SPACE, root.getFreeSpace / (1024 * 1024 * 1024) + " of " + (root.getTotalSpace / (1024 * 1024 * 1024)) + " GB")
      }).toList
  }).toMap

  //pings riak
  private val gwr = GWRiak("accounts").ping match {
    case Success(succ_gwr) => (MConfig.riakurl, Some(RUNNING))
    case Failure(errgwr) => (MConfig.riakurl, none)
  }

  //ping snowflake
  private val uid = UID(MConfig.snowflakeHost, MConfig.snowflakePort, "act").get match {
    case Success(succ_uid) => (MConfig.snowflakeurl, Some(RUNNING))
    case Failure(erruid) => (MConfig.snowflakeurl, none)
  }

  //ping rabbitmq, by droping a DUM0001 req
  private val amqp = new wash.AOneWasher(new wash.PQd(RequestResult("r001","001","torpedo","megam.test","start","test","nop"))).wash match {
    case Success(succ_uid) => (MConfig.amqpurl, Some(RUNNING))
    case Failure(erruid) => (MConfig.amqpurl, none)
  }

  val sharks = Map(RIAK -> gwr, SNOWFLAKE -> uid, RABBITMQ -> amqp)

  //super confusing, all we are trying to do is find the overal status by filte
 val sharkBite = sharks.values.filter(_._2.isEmpty)

  private val mkps = models.base.MarketPlaces.listAll match {
    case Success(succ_mkps) => succ_mkps
    case Failure(errmkps) => MarketPlaceResults.empty
  }

  val buccaneer = Treasure(infra, sharks, mkps)

  val events = Map[String, String]("events" -> "none")

}
