/* 
** Copyright [2012-2013] [Megam Systems]
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
import scala.concurrent._
import scala.concurrent.duration.Duration
import org.megam.common._
import org.megam.common.amqp._
import org.megam.common.amqp.request._
import org.megam.common.amqp.response._
import org.megam.common.concurrent._
import com.typesafe.config._
import play.api.Logger
import play.api.Play._

/**
 * @author rajthilak
 *
 */

object CloudPerNodePublish {

  def apply = new CloudPerNodePublish(new String(), new String())

}

case class CloudPerNodePublish(name: String, messages: String) extends MessageContext {
  val cnpURL = MConfig.amqpuri
  val cnpQueueName = MConfig.cloudper_node_queue_prefix + "_" + name + "_queue"
  val cnpExchangeName = MConfig.cloudper_node_exchage_prefix + "_" + name + "_exchange"
  //create the RabbitMQ Client using url, exchange name and queue name
  val cnp_client = new RabbitMQClient(cnpURL, cnpExchangeName, cnpQueueName)
  val cnp_pubMsg = Messages("id" -> messages)
  play.api.Logger.debug("%-20s -->[%s]".format("Publish", cnp_pubMsg))
  def dop(): ValidationNel[Throwable, AMQPResponse] = execute(cnp_client.publish(cnp_pubMsg, MConfig.routing_key))
}
  





