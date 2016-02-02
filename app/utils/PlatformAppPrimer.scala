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
import models.base._
import play.api.Logger
import models.base._
import models.team._

/**
 * @author ram
 *
 */

object PlatformAppPrimer {

  def takeatourAcct = models.base.Accounts.create(
    AccountInput(MEGAM_FIRST_NAME, MEGAM_LAST_NAME, MEGAM_PHONE, DEMO_EMAIL, DEMO_APIKEY, SAMPLE_PASSWORD, "demo", MEGAM_PASSWORD_RESET_KEY, MEGAM_PASSWORD_RESET_SENT_AT).json)

  def taketestAcct = models.base.Accounts.create(
    AccountInput(MEGAM_TEST_FIRST_NAME, MEGAM_LAST_NAME, MEGAM_PHONE, TEST_EMAIL, TEST_APIKEY, TEST_PASSWORD, "test", MEGAM_PASSWORD_RESET_KEY, MEGAM_PASSWORD_RESET_SENT_AT).json)

  def acc_prep: ValidationNel[Throwable, FunnelResponses] = for {
    dumact <- takeatourAcct
    testacct <- taketestAcct
    dumorg <- clone_organizations(DEMO_EMAIL)
    testorg <- clone_organizations(TEST_EMAIL)
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Account/Org created successfully(%s).
            |
            |Your email registered successully.""".
        format(dumact.get.email).stripMargin, "Megam::Account"))
    FunnelResponses(chainedComps)
  }

  //populate the marketplace catalogue
  def marketplace_catalogs = models.base.MarketPlaces.createMany(MarketPlaceInput.toMap)

  def mkp_prep: ValidationNel[Throwable, FunnelResponses] = for {
    mkp <- marketplace_catalogs
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Marketpace catalogs created successfully. Cache gets loaded upon first fetch.
            |
            |%nLoaded results are ----->%n[%s]""".format(mkp.list.size + " catalogs primed.").stripMargin, "Megam::MarketPlaces"))
    FunnelResponses(chainedComps)
  }

  def clone_organizations = { clonefor_email: String =>
    models.team.Organizations.create(clonefor_email,
      OrganizationsInput(DEFAULT_ORG_NAME).json)
  }

}
