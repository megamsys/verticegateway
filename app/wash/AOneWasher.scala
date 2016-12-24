package wash

import scalaz._
import Scalaz._
import app.MConfig
import io.megam.common._
import io.megam.common.amqp._
import io.megam.common.amqp.request._
import io.megam.common.amqp.response._
import models.base.RequestResult
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
case class AOneWasher(pq: models.Messageble) extends MessageContext {

  def topic = (pq.topic(()).getOrElse(""))

  val msg = pq.messages

  def wash(): ValidationNel[Throwable, AMQPResponse] = {
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
