/* 
** Copyright [2013-2014] [Megam Systems]
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

/*
 * Used it to perform rate limiting
 * TO-DO: Yet to me implemented. 
 * 
 */
trait MessageContext {

  play.api.Logger.debug("%-20s -->[%s]".format("MessageContext:", "Entry"))

  //by default everybody belongs to megam cloud farm
  //this will be a list, as any body can belong to multiple cloud farms.
  def cloudFarm: String = MConfig.cloudper_node_queue_prefix
  def queueName: String
  def exchangeName: String

  def rmqClient = {
    play.api.Logger.debug("%-20s -->[%s]".format("MessageContext:", "Setting up RMQ" + MConfig.amqpuri))
    new RabbitMQClient(MConfig.amqpuri, exchangeName, queueName)
  }

  protected def execute(ampq_request: AMQPRequest, duration: Duration = org.megam.common.concurrent.duration) = {
    import org.megam.common.concurrent.SequentialExecutionContext
    val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] = ampq_request.apply
    responseFuture.block(duration)
  }
}