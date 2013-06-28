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

import org.megam.common._
import org.megam.common.amqp._
import com.typesafe.config._
import play.api.Logger
import play.api.Play._

/**
 * @author rajthilak
 *
 */
object MessageObjects {

  trait MessageContext {


    Logger.debug("MessageObjects: Setting up RabbitMQClient")

    /*
     * create the RabbitMQ Client using url, exchange name and queue name
     */
    val client = new RabbitMQClient(MConfig.amqpuri, MConfig.exchange_name, MConfig.queue_name)

    /*
     * these was execute Publish or subscribe the messages 
     * 
     */
    protected def execute[T](t: AMQPRequest, expectedCode: AMQPResponseCode = AMQPResponseCode.Ok) = {
      Logger.debug("Executing AMQPRequest")
      val r = t.executeUnsafe
    }
   
  }

  /*
   * these case class extends TestContext trait 
   * and then to publish the messages
   *  
   */
  case class Publish(messages: String) extends MessageContext {
    val pubMsg = Messages("id" -> messages)
    Logger.debug("Publishing message " + pubMsg)
    def succeeds = execute(client.publish(pubMsg, MConfig.routing_key))
  }
}