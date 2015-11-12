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
package controllers.stack

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import org.megam.util.Time
import controllers.stack._
import models.RequestResult
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.tosca._
import models.riak._
import models.tosca.Organizations
import models.MarketPlaceResults
import org.megam.common.uid.UID
import controllers.Constants.DEMO_EMAIL

import play.api.Logger
import play.api.Play._
/**
 * @author ram
 *
 */

case object Hellow {

  type THunt = (String, Option[String])

  case class Treasure(infra: Map[String, String],
    hunts: Map[String, THunt],  mkps: MarketPlaceResults, cansail: List[(String, Boolean, String, Option[String])])

  val TOTAL_MEMORY = "TOTAL_MEMORY"
  val FREE_MEMORY = "FREE_MEMORY"
  val SPACE = "SPACE"
  val CPU_LOAD = "CPU_LOAD"
  val CPU_CORES = "CPU_CORES"

  val MEMCACHE = "Memcache"
  val SNOWFLAKE = "Snowflake"
  val RABBITMQ = "RabbitMQ"
  val RIAK = "Riak"
  val RUNNING = "Running"

  val What2Hunts = Array(RIAK, SNOWFLAKE, RABBITMQ, MEMCACHE)

  play.api.Logger.debug("%-20s -->[%s]".format("Hellow: ", MConfig.baseurl))

  import java.lang.management.{ ManagementFactory, OperatingSystemMXBean }
  import java.lang.reflect.{ Method, Modifier }

  //the infra information, returned as tuples.
  val infra = ({
    val runtime = Runtime.getRuntime()
    val mxbean = ManagementFactory.getOperatingSystemMXBean

    import runtime.{ totalMemory, freeMemory, maxMemory, availableProcessors }

    List((TOTAL_MEMORY, (totalMemory / (1024 * 1024) + " MB")), (FREE_MEMORY, (freeMemory / (1024 * 1024)) + ""),
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
  private val amqp = new AOneWasher(new PQd(RequestResult("","","","","test","",""))).wash match {
    case Success(succ_uid) => (MConfig.amqpurl, Some(RUNNING))
    case Failure(erruid) => (MConfig.amqpurl, none)
  }

  //we don't memcache today
  private val memcache = ("None", none)

  val sharks = Map(RIAK -> gwr, SNOWFLAKE -> uid, RABBITMQ -> amqp, MEMCACHE -> memcache)

  //super confusing, all we are trying to do is find the overal status by filtering the none
  val sharkBite = sharks.filterNot(_._1.equals(MEMCACHE)).values.filter(_._2.isEmpty)


  private val mkps = models.MarketPlaces.listAll match {
    case Success(succ_mkps) => succ_mkps
    case Failure(errmkps) => MarketPlaceResults.empty
  }

  private val orgs = (models.tosca.Organizations.findByEmail(DEMO_EMAIL) match {
    case Success(succ_orgs) => succ_orgs
    case Failure(errmkps) => OrganizationsResults.empty
  }).list.flatten map {og => ("ORG", false, og.id +" " + og.name, og.created_at.some)}


  //pings each of our servers and return a Treasure :) Yeah for sure.
    val buccaneer = Treasure(infra,sharks, mkps, orgs)

  val events = Map[String, String]("events" -> "none")

}
