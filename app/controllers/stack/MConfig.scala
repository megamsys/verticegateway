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
  val marketplaces_yaml = play.api.Play.application(play.api.Play.current).configuration.getString("megam.marketplaces").get
  val riakurl = play.api.Play.application(play.api.Play.current).configuration.getString("riak.url").get
  val amqpurl = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.url").get
  val snowflakeHost = play.api.Play.application(play.api.Play.current).configuration.getString("sf.host").get
  val snowflakePort: Int = play.api.Play.application(play.api.Play.current).configuration.getInt("sf.port").get
  val snowflakeurl = snowflakeHost + ":" + snowflakePort

  val amqp_prefix = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.prefix").get
  val routing_key = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.routing.key").get
  val standup_queue = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.vmup.queue").get
  val standup_exchange = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.vmup.exchange").get
  val dockerup_queue = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.dockerup.queue").get
  val dockerup_exchange = play.api.Play.application(play.api.Play.current).configuration.getString("amqp.global.dockerup.exchange").get
  }
