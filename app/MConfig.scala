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
/**
 * @author ram
 *
 */
package app

object MConfig {
  //val baseurl = play.api.Play.application(play.api.Play.current).configuration.getString("application.baseUrl")
  val marketplaces_yaml = play.api.Play.application(play.api.Play.current).configuration.getString("megam.marketplaces").get
  val riakurl = play.api.Play.application(play.api.Play.current).configuration.getString("riak.url").get
  val nsqurl = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.url").get
  val scyllaurl = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.host").get
  val scylla_keyspace = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.keyspace").get
  val scylla_username = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.username").get
  val scylla_password = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.password").get
}
