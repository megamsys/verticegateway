/* 
** Copyright [2012] [Megam Systems]
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
import play.api.Play._

/**
 * @author rajthilak
 *
 */
object MessageObjects {

  trait TestContext {

    val app = play.api.Play.current
    val url = play.api.Play.application(app).configuration.getString("amqp.url")
    val uris = show(play.api.Play.application(app).configuration.getString("amqp.url"))
    val exchange_name = show(play.api.Play.application(app).configuration.getString("amqp.global.exchange"))
    val queue_name = show(play.api.Play.application(app).configuration.getString("amqp.global.conf.queue"))
    val routingKey = "megam_key"

    println("Setting up RabbitMQClient")

    val client = new RabbitMQClient(uris, exchange_name, queue_name)

    protected def execute[T](t: AMQPRequest, expectedCode: AMQPResponseCode = AMQPResponseCode.Ok) = {
      println("Executing AMQPRequest")
      val r = t.executeUnsafe

    }

    def show(x: Option[String]) = x match {
      case Some(s) => s
      case None    => "?"
    }

  }

  case class Publish(messages: String) extends TestContext {
    println("Run PUB")
    val message1 = Messages("id" -> messages)
    println("------------>" + message1)
    def succeeds = execute(client.publish(message1, routingKey))
  }
}