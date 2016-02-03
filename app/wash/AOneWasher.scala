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
package wash

import scalaz._
import Scalaz._
import app.MConfig
import io.megam.common._
import io.megam.common.amqp._
import io.megam.common.amqp.request._
import io.megam.common.amqp.response._
import models.base.RequestResult
import play.api.Logger
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
case class AOneWasher(pq: models.Messageble) extends MessageContext {

  def topic = (pq.topic().getOrElse(""))

  val msg = pq.messages

  def wash(): ValidationNel[Throwable, AMQPResponse] = {
    play.api.Logger.debug("%-20s -->[%s]".format("Washing:[" + topic + "]", msg))
    execute(nsqClient.publish(msg))
  }
}

case class PQd(f: Unit => Option[String], msg: String) extends models.Messageble {

  override def topic(x: Unit): Option[String] = f(x)

  override def messages = msg

}

object PQd {
  def topic(x: Unit) = "testing".some
  def empty: PQd = new PQd(topic, "")
}
