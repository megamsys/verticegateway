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

import com.typesafe.config.ConfigFactory


/**
 * @author ram
 *
 */
object Config {
  
  val AMQP_HOST = ConfigFactory.load().getString("amqp.host")
  val AMQP_PORT = ConfigFactory.load().getString("amqp.port")
  val AMQP_USER = ConfigFactory.load().getString("amqp.user")
  val AMQP_PASS = ConfigFactory.load().getString("amqp.pass")
  val AMQP_GLOBAL_QUEUE = ConfigFactory.load().getString("megam.global.queue")
  val AMQP_GLOBAL_EXCHANGE = ConfigFactory.load().getString("megam.global.exchange")


}