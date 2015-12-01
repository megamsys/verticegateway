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
package wash

import scalaz._
import Scalaz._
import org.megam.common._
import org.megam.common.amqp._
import org.megam.common.amqp.request._
import org.megam.common.amqp.response._
import app.MConfig
import models.base.RequestResult
import play.api.Logger
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
case class AOneWasher(pq: PQd) extends MessageContext {

  def queueName = (pq.QrE(cloudFarm).getOrElse(("","")))._1

  def exchangeName = (pq.QrE(cloudFarm).getOrElse(("","")))._2

  val msg = Messages(pq.messages.toList)
  
print(msg)
  def wash(): ValidationNel[Throwable, AMQPResponse] = {
    play.api.Logger.debug("%-20s -->[%s]".format("Washing:[" + queueName+"]", msg))
    execute(rmqClient.publish(msg, MConfig.routing_key))
  }
}

case class PQd(reqres: models.base.RequestResult) {

  val DQACTIONS = Array[String](CREATE, DELETE)

  def QrE(cloudFarm: String): Option[Tuple2[String, String]] = {
    if (reqres.cattype.equalsIgnoreCase(CATTYPE_DOCKER)) {
      (cloudFarm + MConfig.dockerup_queue, cloudFarm + MConfig.dockerup_exchange).some
    } else if (DQACTIONS.contains(reqres.action)) {
      (cloudFarm + MConfig.standup_queue, cloudFarm + MConfig.standup_exchange).some
    } else if (reqres.name.trim.length > 0) {
      (cloudFarm + reqres.name + "_queue", cloudFarm + reqres.name + "_exchange").some
    } else  none
  }

  val messages = reqres.toMap

}


object PQd {
    def empty: PQd = new PQd(models.base.RequestResult("","","","","","",""))
}
