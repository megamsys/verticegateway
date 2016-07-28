/**
 * @author ram
 *
 */
package app

object MConfig {
  val nsqurl = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.url").get
  val scyllaurl = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.host").get
  val scylla_keyspace = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.keyspace").get
  val scylla_username = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.username").get
  val scylla_password = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.password").get
  val domain = play.api.Play.application(play.api.Play.current).configuration.getString("domain").get
}
