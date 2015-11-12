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
import org.megam.common._
import org.megam.common.amqp._
import org.megam.common.amqp.request._
import org.megam.common.amqp.response._
import models.RequestResult
import play.api.Logger

/**
 * @author rajthilak
 *
 */
case class AOneWasher(pq: PQd) extends MessageContext {

  def queueName = pq.QrE(cloudFarm)._1

  def exchangeName = pq.QrE(cloudFarm)._2

  val msg = Messages(pq.messages.toList)

  play.api.Logger.debug("%-20s -->[%s]".format("Publish:" + queueName, msg))

  def wash(): ValidationNel[Throwable, AMQPResponse] = execute(rmqClient.publish(msg, MConfig.routing_key))
}

case class PQd(reqres: RequestResult) {

  val DELETE = "DESTROY"
  val CREATE = "CREATE"
  val CATTYPE_DOCKER = "MICROSERVICES"
  val DQACTIONS = Array[String](CREATE, DELETE)

  def QrE(cloudFarm: String): Tuple2[String,String] = {
    (if (reqres.cattype == CATTYPE_DOCKER) {
      (cloudFarm + MConfig.dockerup_queue, cloudFarm + MConfig.dockerup_exchange)
    } else if (DQACTIONS.contains(reqres.action)) {
      (cloudFarm + MConfig.standup_queue, cloudFarm + MConfig.standup_exchange)
    } else if (reqres.name.trim.length > 0) {
      (cloudFarm + reqres.name + "_queue", cloudFarm + reqres.name + "_exchange")
    })
  }

  val messages = reqres.toMap

}
