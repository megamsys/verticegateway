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

/**
 * @author ram
 *
 */
/**
 * play.api.Play.current to get the current configuration
 * getString() method return the current configuration file value
 */
object MConfig {
  val baseurl = play.api.Play.application(play.api.Play.current).configuration.getString("application.baseUrl")
  val riakurl = play.api.Play.application(play.api.Play.current).configuration.getString("riak.url").get
  val amqpurl = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.url").get
  val routing_key = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.routing.key").get
  val snowflakeHost = play.api.Play.application(play.api.Play.current).configuration.getString("sf.host").get
  val snowflakePort: Int = play.api.Play.application(play.api.Play.current).configuration.getInt("sf.port").get
  val snowflakeurl = snowflakeHost + ":" + snowflakePort
  val cloudstandup_queue = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.cloudstandup.queue").get
  val cloudstandup_exchange = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.cloudstandup.exchange").get
  val cloudper_node_queue_prefix = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.node.queue.prefix").get
  val cloudper_node_exchage_prefix = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.node.exchange.prefix").get
  val event_queue = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.event.queue").get
  val event_exchange = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.event.exchange").get
}
