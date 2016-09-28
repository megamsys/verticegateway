/**
 * @author ram
 *
 */
package app

object MConfig {
  val nsqurl = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.url").get
  val mute_events = play.api.Play.application(play.api.Play.current).configuration.getBoolean("nsq.events.muted").get
  val mute_emails = play.api.Play.application(play.api.Play.current).configuration.getStringList("nsq.events.muted_emails").get

  val scyllaurl = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.host").get
  val scylla_keyspace = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.keyspace").get
  val scylla_username = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.username").get
  val scylla_password = play.api.Play.application(play.api.Play.current).configuration.getString("scylla.password").get

  val org = play.api.Play.application(play.api.Play.current).configuration.getString("org").get
  val domain = play.api.Play.application(play.api.Play.current).configuration.getString("domain").get
  val master_key = play.api.Play.application(play.api.Play.current).configuration.getString("master.key").get
}
