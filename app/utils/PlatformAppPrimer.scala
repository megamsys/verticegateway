package utils

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }
import play.api.http.Status._
import controllers.stack._
import controllers.Constants._
import io.megam.auth.funnel.FunnelErrors._
import io.megam.util.Time
import models.base._
import play.api.Logger
import models.base._
import models.team._

/**
 * @author rajthilak
 *
 */

object PlatformAppPrimer {
  val key = app.MConfig.master_key

  def masterkey = models.base.MasterKeys.create(

    MasterKeysInput(key).json)

  def masterkeys_prep: ValidationNel[Throwable, FunnelResponses] = for {
    mk ← masterkey
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, "Master key in the config file registered successfully(%s).".
        format("").stripMargin, "Megam::MasterKey"))
    FunnelResponses(chainedComps)
  }

}
