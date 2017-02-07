package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import app.MConfig
import models.base._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import io.megam.common.amqp.response.AMQPResponse
import io.megam.util.Time

/**
 * @author ram
 */
case class EventInput(id: String, accounts_id: String, etype: String, action: String, inputs: Map[String, String]) {
  val email = inputs(Events.EVTEMAIL)
}

case class EventResult(id: String, accounts_id: String, etype: String, action: String, inputs: Map[String, String], created_at: String) {

  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(inputs)

  val json = "{\"id\":\"" + id + "\",\"accounts_id\":\"" + accounts_id + "\",\"type\":\"" + etype + "\",\"action\":\"" + action + "\",\"inputs\":" + models.tosca.KeyValueList.toJson(toKeyList) + "}"

  def topicFunc(x: Unit): Option[String] = "events".some

}

class Events(evi: EventInput) {

  //create request from input
  private def create(): ValidationNel[Throwable, Option[wash.PQd]] = {
    for {
      eres <- EventResult(evi.id, evi.accounts_id, evi.etype, evi.action, evi.inputs, Time.now.toString).some.successNel[Throwable]
    } yield {
      eres match {
        case Some(thatER) => {
          new wash.PQd(thatER.topicFunc, thatER.json).some
        }
        case None => {
          None //shouldn't happen
        }
      }
    }
  }

  // create a request and publish
  def createAndPub(): ValidationNel[Throwable, Option[wash.PQd]] = {
    (create() leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { pq: Option[wash.PQd] =>
      if (!MConfig.mute_emails.contains(evi.email) &&
          !MConfig.mute_events) {
        (new wash.AOneWasher(pq.get).wash).
          flatMap { maybeGS: AMQPResponse =>
            play.api.Logger.debug(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Event","|+| ✔", Console.RESET))
            pq.successNel[Throwable]
          }
          play.api.Logger.debug(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Event","|+| ✗", Console.RESET))
          pq.successNel[Throwable]
      } else {
        play.api.Logger.debug(("%s%s%-20s%s%s").format(Console.YELLOW, Console.BOLD, "Event","|+| ●", Console.RESET))
        wash.PQd.empty.some.successNel[Throwable]
      }
    }
  }
}

object Events {
  //types
  val EVENTUSER = "user"
  //inputs
  val EVTEMAIL = "email"
  val EVTCLICK = "click_url"
  val EVTTOKEN = "token"
  //actions
  val CREATE = "0"
  val DESTROY = "1"
  val STATUS = "2"
  val DEDUCT = "3"
  val ONBOARD = "4"
  val RESET = "5"
  val INVITE = "6"
  val BALANCE = "7"
  val LOGIN = "8"

  def apply(aid: String, etype: String, eaction: String, inputs: Map[String, String]) = new Events(new EventInput("", aid, etype, eaction, inputs))
}
