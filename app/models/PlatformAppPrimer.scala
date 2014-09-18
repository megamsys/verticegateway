/* 
** Copyright [2013-2014] [Megam Systems]
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
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import controllers.funnel.{ FunnelResponse, FunnelResponses }
import play.api.http.Status._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import play.api.Logger
import models.tosca._



/**
 * @author ram
 *
 */

object PlatformAppPrimer {

  //on board a sandbox account during start of the play server.
  def sandboxAcct = models.Accounts.create(
    AccountInput(MEGAM_ADMIN_EMAIL, MEGAM_ADMIN_APIKEY, "normal").json)

  def sandboxDummyAcct = models.Accounts.create(
    AccountInput(DEMO_EMAIL, DEMO_APIKEY, "demo").json)

  //populate the predefinitions of the platform supported by megam.
  def predefs = models.Predefs.create

  //populate the marketplace addons
  def marketplace_addons = models.MarketPlaces.createMany(MarketPlaceInput.toMap)

  def sandbox_ec2_default = PredefCloudInput("clouddefault",
    new PredefCloudSpec("ec2", "megam", "ami-a0074df2", "t1.micro", ""),
    new PredefCloudAccess("megam_ec2", "cloudkeys/" + MEGAM_ADMIN_EMAIL + "/default/megam_ec2.pem", "ubuntu", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/" + MEGAM_ADMIN_EMAIL + "/default", "", "", "ap-southeast-1")).json

  def sandbox_google_default = PredefCloudInput("clouddefault",
    new PredefCloudSpec("google", "", "debian-7-wheezy-v20131120", "f1-micro", ""),
    new PredefCloudAccess("", "cloudkeys/" + MEGAM_ADMIN_EMAIL + "/id_rsa.pub", "ubuntu", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/" + MEGAM_ADMIN_EMAIL + "/gdefault", "", "europe-west1-a", "")).json

  def clone_predefcloud = { ccemail: String => models.PredefClouds.create(ccemail, sandbox_google_default) }

  //define the cloud tools used to manage the cloud platform. 
  def cloudtools = models.CloudTools.create

  def acc_prep: ValidationNel[Throwable, FunnelResponses] = for {
    sada <- sandboxAcct
    dummy <- sandboxDummyAcct
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Account created successfully(%s, %s).
            |
            |Your email registered successully.""".
        format(sada.get.email, dummy.get.email).stripMargin, "Megam::Account"))
    FunnelResponses(chainedComps)
  }

  def prep: ValidationNel[Throwable, FunnelResponses] = for {
    lpd <- predefs
    ccd <- clone_predefcloud(MEGAM_ADMIN_EMAIL)
    cdsd <- clone_predefcloud(DEMO_EMAIL)
    cts <- cloudtools
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Predefs created successfully. Cache gets loaded upon first fetch.
               	|
								|%nLoaded values are ----->%n[%s]""".format(lpd.toString).stripMargin, "Megam::Predef"),
      FunnelResponse(CREATED, """Predefs cloud created successfully(%s,%s).
								|
								|You can use the the 'predefs cloud name':{%s}.""".format(MEGAM_ADMIN_EMAIL, DEMO_EMAIL, cdsd.getOrElse("none")), "Megam::PredefCloud"),
      FunnelResponse(CREATED, """Cloud tools created successfully. Cache gets loaded upon first fetch.
								|
								|%nLoaded values are ----->%n[%s]""".format(cts.toString).stripMargin, "Megam::CloudTools"))
      FunnelResponses(chainedComps)
      }

  //populate the default cloud tool settings  
  def cloudtoolsetting_default = CloudToolSettingInput("chef", "default_chef", "https://github.com/indykish/chef-repo.git", "https://s3-ap-southeast-1.amazonaws.com/cloudrecipes/megam@mypaas.io/default_chef/chef-repo.zip", "cloudrecipes/megam@mypaas.io/default_chef/chef-repo/.chef/knife.rb").json

  def clone_cloudtoolsettings = { ccemail: String => models.CloudToolSettings.create(ccemail, cloudtoolsetting_default) }

  def cts_prep: ValidationNel[Throwable, FunnelResponses] = for {
    cts <- clone_cloudtoolsettings(MEGAM_ADMIN_EMAIL)
    ctds <- clone_cloudtoolsettings(DEMO_EMAIL)
    pub <- CloudToolPublish("https://s3-ap-southeast-1.amazonaws.com/cloudrecipes/" + MEGAM_ADMIN_EMAIL + "/default_chef/chef-repo.zip", "https://github.com/indykish/chef-repo.git").dop
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """CloudToolSettings created successfully(%s,%s).
            |
            |You can use the the 'cloud tool setting name':{%s}.""".format(MEGAM_ADMIN_EMAIL, DEMO_EMAIL, cts.getOrElse("none")), "Megam::CloudToolSetting"),
      FunnelResponse(CREATED, """CloudToolSettings inilization published successfully.
            |
            |You can use the the 'CloudToolSetting.""", "Megam::CloudToolSetting"))
    FunnelResponses(chainedComps)
  }

  def mkp_prep: ValidationNel[Throwable, FunnelResponses] = for {
    mkp <- marketplace_addons
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Market Place addons created successfully. Cache gets loaded upon first fetch. 
            |
            |%nLoaded results are ----->%n[%s]""".format(mkp.list.size + " addons primed.").stripMargin, "Megam::MarketPlaces"))
    FunnelResponses(chainedComps)
  }

  def organizations_default = models.tosca.Organizations.create( MEGAM_ADMIN_EMAIL,
      OrganizationsInput(DEFAULT_ORG_NAME).json)
      
      
  def org_prep: ValidationNel[Throwable, FunnelResponses] = for {
    org <- organizations_default    
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Organization created successfully(%s, %s).
            |
            |Your email registered successully.""".
        format(org.get.name).stripMargin, "Megam::Organizations"))
    FunnelResponses(chainedComps)
  }

      
  def domains_default = models.tosca.Domains.create(MEGAM_ADMIN_EMAIL,
      DomainsInput( DEFAULT_DOMAIN_NAME).json)
  
  def dmn_prep: ValidationNel[Throwable, FunnelResponses] = for {
    dmn <- domains_default    
  } yield {
    val chainedComps = List[FunnelResponse](
      FunnelResponse(CREATED, """Domains created successfully(%s, %s).
            |
            |Your email registered successully.""".
        format(dmn.get.name).stripMargin, "Megam::Domains"))
    FunnelResponses(chainedComps)
  }
}


