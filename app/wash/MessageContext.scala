package wash

import scalaz._
import Scalaz._
import scala.concurrent._
import scala.concurrent.duration.Duration
import io.megam.common._
import io.megam.common.amqp._
import io.megam.common.amqp.request._
import io.megam.common.amqp.response._
import io.megam.common.concurrent._

/**
 * @author rajthilak
 *
 */

trait MessageContext {


  def topic: String

  def nsqClient = {
    new NSQClient(app.MConfig.nsqurl, topic)
  }

  protected def execute(ampq_request: AMQPRequest, duration: Duration = io.megam.common.concurrent.duration) = {
    import io.megam.common.concurrent.SequentialExecutionContext
    val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] = ampq_request.apply
    responseFuture.block(duration)
  }
}
