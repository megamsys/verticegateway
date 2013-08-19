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
package models

import scalaz._
import scalaz.Validation._
import Scalaz._

import controllers.funnel.{ FunnelResponse, FunnelResponses }
import play.api.http.Status._

import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import play.api.Logger;

/**
 * @author ram
 *
 */

object PlatformAppPrimer {

  //on board a sandbox account during start of the play server.
  lazy val sandboxAcct = models.Accounts.create(
    AccountInput("sandy@megamsandbox.com", "IamAtlas{74}NobodyCanSeeME#07", "normal").json)

  //populate the predefinitions of the platform supported by megam.
  lazy val predefs = models.Predefs.create

  val iaas_default = PredefCloudInput("iaas_default",
    new PredefCloudSpec("ec2", "megam", "ami-d783cd85", "m1.small"),
    new PredefCloudAccess("megam_ec2", "megam_ec2.pem", "ubuntu")).json

  lazy val clone_predefcloud = { ccemail: String => models.PredefClouds.create(ccemail, iaas_default) }

  //define the cloud tools used to manage the cloud platform. 
  lazy val cloudtools = models.CloudTools.create

  val prep: ValidationNel[Throwable, FunnelResponses] = (for {
    sada <- sandboxAcct
    lpd <- predefs
    ccd <- clone_predefcloud(SANDBOX_EMAIL)
    cts <- cloudtools
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Account created successfully.
            |
            |Your email '%s' and api_key '%s' registered successully.""".
        format(sada.get.email, sada.get.api_key).stripMargin, "Megam::Account"),
      FunnelResponse(CREATED, """Predefs created successfully. Cache gets loaded upon first fetch. 
            |
            |%nLoaded values are ----->%n[%s]""".format(lpd.toString).stripMargin, "Megam::Predef"),
      FunnelResponse(CREATED, """Predefs cloud created successfully.
            |
            |You can use the the 'predefs cloud name':{%s}.""".format(ccd.getOrElse("none")), "Megam::PredefCloud"),

      FunnelResponse(CREATED, """Cloud tools created successfully. Cache gets loaded upon first fetch. 
            |
            |%nLoaded values are ----->%n[%s]""".format(cts.toString).stripMargin, "Megam::CloudTools"))
    FunnelResponses(chainedComps)
  })

}