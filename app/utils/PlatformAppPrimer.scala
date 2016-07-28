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
import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
import io.megam.util.Time
import models.base._
import play.api.Logger
import models.base._
import models.team._

/**
 * @author ram
 *
 */

object PlatformAppPrimer {
val name = new Name(MEGAM_FIRST_NAME, MEGAM_LAST_NAME)
val phone = new Phone(MEGAM_PHONE, MEGAM_PHONE_VERIFIED)
val states = new States("demo", MEGAM_ACTIVE, MEGAM_BLOCKED, "")
val password = new Password(SAMPLE_PASSWORD,MEGAM_PASSWORD_RESET_KEY, MEGAM_PASSWORD_RESET_SENT_AT)
val approval = new Approval(MEGAM_APPROVED, MEGAM_APPROVED_BY_ID, MEGAM_APPROVED_AT)
val dates = new Dates(MEGAM_LAST_POSTED_AT, MEGAM_LAST_EMAILED_AT, MEGAM_PREVIOUS_VISIT_AT, MEGAM_FIRST_SEEN_AT, Time.now.toString)
val suspend = new Suspend(MEGAM_SUSPENDED, MEGAM_SUSPENDED_AT, MEGAM_SUSPENDED_TILL)
val testPassword = new Password(TEST_PASSWORD,MEGAM_PASSWORD_RESET_KEY, MEGAM_PASSWORD_RESET_SENT_AT)
val testName = new Name(MEGAM_TEST_FIRST_NAME, MEGAM_LAST_NAME)
val testStates = new States("test", MEGAM_ACTIVE, MEGAM_BLOCKED, "")
   def takeatourAcct = models.base.Accounts.create(

    AccountInput(name, phone, DEMO_EMAIL, DEMO_APIKEY, password, states, approval ,suspend, MEGAM_REGISTRATION_IP_ADDRESS, dates).json)

    def taketestAcct = models.base.Accounts.create(
      AccountInput(testName, phone, TEST_EMAIL, TEST_APIKEY, testPassword, testStates, approval, suspend, MEGAM_REGISTRATION_IP_ADDRESS, dates).json)

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
        format("").stripMargin, "Megam::Account"))
    FunnelResponses(chainedComps)
  }


  def clone_organizations = { clonefor_email: String =>
    models.team.Organizations.create(clonefor_email,
      OrganizationsInput(DEFAULT_ORG_NAME).json)
  }

}
