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
package controllers

import akka.actor.Terminated
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import play.api.libs.json._

/**
 * @author ram
 *
 */

/**
 * Actors that are used as registration for subscriptions. This will handle
 * Register message. It is sent when the maa bridge starts up
 * starts a subscription (STOMP SUBSCRIBE). The `sender` of the
 * `RegisterSubscriber` message is the subscriber `ActorRef`
 * that expect published messages from the topic.
 *
 * When NBG starts up and registers a zk node named
 * /machines
 * 	/instances
 * 		/megam_conf
 *
 */
case object Plus

case class User(id: BigDecimal, name: String, favThings: List[String])

/**
 * Generic topic, i.e. can be used as destination for
 * STOMP SUBSCRIBE. It will send all received messages to registered
 * subscribers. You may use any other actor as a subscription
 * destination as long as it provides similar functionality, i.e.
 * handles `RegisterSubscriber` message and watch the subscriber for
 * termination.
 */
class SubscriberRegistry extends Actor {

  val jsonObject = Json.obj(
  "users" -> Json.arr(
    Json.obj(
      "name" -> "Bob",
      "age" -> 31,
      "email" -> "bob@gmail.com"
    ),
    Json.obj(
      "name" -> "Kiki",
      "age" -> 25,
      "email" -> JsNull
    )
  )
)

  var subscribers = Set.empty[ActorRef]

  def receive = {
    case Plus ⇒
      context.watch(sender)
      subscribers += sender
    case Terminated(subscriber) ⇒
      subscribers -= subscriber
    case msg ⇒
      subscribers foreach { _ ! msg }
  }

  implicit object UserFormat extends Format[User] {

    def writes(o: User): JsValue = JsObject(
      List("id" -> JsNumber(o.id),
        "name" -> JsString(o.name),
        "favThings" -> JsArray(o.favThings.map(JsString(_)))))

    def reads(json: JsValue): JsResult[User] = JsSuccess(User(
      (json \ "id").as[Long],
      (json \ "name").as[String],
      (json \ "favThings").as[List[String]]))

  }
}

