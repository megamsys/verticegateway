/**
 * @author ram
 *
 */
package app

object MConfig {

  val nsqurl = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.url").get
  val mute_events = play.api.Play.application(play.api.Play.current).configuration.getBoolean("nsq.events.muted").getOrElse(false)
  val mute_emails = play.api.Play.application(play.api.Play.current).configuration.getStringList("nsq.events.muted_emails").get

  val cassandra_host       = play.api.Play.application(play.api.Play.current).configuration.getString("cassandra.host").get
  val cassandra_keyspace   = play.api.Play.application(play.api.Play.current).configuration.getString("cassandra.keyspace").get
  val cassandra_username   = play.api.Play.application(play.api.Play.current).configuration.getString("cassandra.username").get
  val cassandra_password   = play.api.Play.application(play.api.Play.current).configuration.getString("cassandra.password").get
  val cassandra_use_ssl    = play.api.Play.application(play.api.Play.current).configuration.getString("cassandra.password").get

  val org = play.api.Play.application(play.api.Play.current).configuration.getString("org").get
  val domain = play.api.Play.application(play.api.Play.current).configuration.getString("domain").get
  val master_key = play.api.Play.application(play.api.Play.current).configuration.getString("master.key").get
}
