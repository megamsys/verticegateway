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
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import org.megam.util.Time
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.cache._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class MarketPlacePlan(price: String, description: String, plantype: String, version: String, source: String, os: String) {
  val json = "{\"price\":\"" + price +
    "\",\"description\":\"" + description + "\",\"plantype\":\"" + plantype +
    "\",\"version\":\"" + version + "\",\"source\":\"" + source + "\",\"os\":\"" + os + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    toJSON(this)(MarketPlacePlanWriter)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlacePlan {
  def empty: MarketPlacePlan = new MarketPlacePlan(new String(), new String(), new String(), new String, new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlacePlan] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    fromJSON(jValue)(MarketPlacePlanReader)
  }

  def fromJson(json: String): Result[MarketPlacePlan] = (Validation.fromTryCatch {
    play.api.Logger.debug(("%-20s -->[%s]").format("---json------------------->", json))
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

//lets make sure that we have atleast 2 features.
case class MarketPlaceFeatures(feature1: String, feature2: String, feature3: String = null, feature4: String = null) {
  val json = "\"feature1\":\"" + feature1 + "\",\"feature2\":\"" + feature2 + "\",\"feature3\":\"" + feature3 + "\",\"feature4\":\"" + feature4 + "\""
}

case class MarketPlaceAppDetails(logo: String, category: String, description: String) {
  val json = "\"logo\":\"" + logo + "\",\"category\":\"" + category + "\",\"description\":\"" + description + "\""
}

case class MarketPlaceAppLinks(free_support: String, paid_support: String, home_link: String, info_link: String, content_link: String, wiki_link: String, source_link: String) {
  val json = "\"free_support\":\"" + free_support + "\",\"paid_support\":\"" + paid_support + "\",\"home_link\":\"" + home_link + "\",\"info_link\":\"" + info_link + "\",\"content_link\":\"" + content_link + "\",\"wiki_link\":\"" + wiki_link + "\",\"source_link\":\"" + source_link + "\""
}

case class MarketPlaceInput(name: String, appdetails: MarketPlaceAppDetails, features: MarketPlaceFeatures, plans: models.MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String) {
  val json = "{\"name\":\"" + name + "\",\"appdetails\":{" + appdetails.json + "},\"features\":{" + features.json + "},\"plans\":" + MarketPlacePlans.toJson(plans, true) + ",\"applinks\":{" + applinks.json + "},\"attach\":\"" + attach + "\",\"predefnode\":\"" + predefnode + "\",\"approved\":\"" + approved + "\"}"
}


//init the default market place addons
object MarketPlaceInput {

  val toMap = Map[String, MarketPlaceInput](
    "10-Alfresco" -> MarketPlaceInput("10-Alfresco", 
        new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/alfresco.png", 
            "ECM", 
            "Alfresco Community Edition allows organizations to manage any type of content from simple office documents to scanned images, photographs, engineering drawings and large video files. It is commonly used as a: Document management system, Content platform, CMIS-compliant repository"), 
        new MarketPlaceFeatures("Many companies have documents stored all over the place – on desktop computers, laptops, network drives, email, USB sticks and various consumer file sharing sites. And with contracts stored by customer, invoices by month, case files by case number, and consulting reports by year, it is difficult to get a 360° view of a customer’s information, which makes effective collaboration almost impossible.",
            "Alfresco enables you to manage your business critical documents like contracts, proposals, agreements, marketing and sales materials, as well as technical renderings and manuals","Add-Ons — Ability to download and install additional product extensions (see http://addons.alfresco.com/)", "Alfresco saves valuable time otherwise wasted searching for information and recreating misplaced documents, and eliminates mistakes and costs associated with using the wrong version."), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "Alfresco Community Edition is the open source alternative for Enterprise Content Management. ", "Free","4.2", "Work in progress.", "Ubuntu 13.04 +"))), 
            new MarketPlaceAppLinks("http://forums.alfresco.com/", "http://www.alfresco.com/services/subscription/technical-support", "http://www.alfresco.com/", "http://www.alfresco.com/products/community", "", "http://www.alfresco.com/resources/documentation", "https://wiki.alfresco.com/wiki/Source_Code"), "false", "predefnode", "false"), 
    "11-Diaspora" -> MarketPlaceInput("11-Diaspora", 
        new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/diaspora.png", "Social",""), 
        new MarketPlaceFeatures("The online social world where you are in control", "Decentralization: Instead of everyone’s data being contained on huge central servers owned by a large organization, local servers (“pods”) can be set up anywhere in the world", "You can be whoever you want to be in diaspora*. Unlike some networks, you don’t have to use your real identity. You can interact with whomever you choose in whatever way you want.", ""), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "Diaspora foundation", "Free","","Work in progress.", "Ubuntu 13.04 +"))), 
        new MarketPlaceAppLinks("", "", "", "", "", "", ""), "false", "predefnode", "false"),
   "12-DokuWiki" -> MarketPlaceInput("12-DokuWiki", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/dokuwiki.png", "Wiki",""),  
       new MarketPlaceFeatures("", "", "", ""), 
       MarketPlacePlans(List(new MarketPlacePlan("0", "DokuWiki is a popular choice when choosing a Wiki software", "Free","","Work in progress", "Ubuntu 13.04 +"))), 
       new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "1-DRBD" -> MarketPlaceInput("1-DRBD", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/drbd.png", "DR", "The Distributed Replicated Block Device (DRBD) is a distributed replicated storage system for the Linux platform."),  
        new MarketPlaceFeatures("Backup your drive to any cloud, simply provide a directory (/var/lib/data) and at click.", "Enhances your app/server to be highly available, load balanced on multi cloud.", "Long-distance replication with DRBD Proxy (paid product).", "Launch new apps/services which are highly available."), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "Disaster recovery for your app/service", "Free","8.4.4","", "Ubuntu 13.04 +"))),
        new MarketPlaceAppLinks("http://lists.linbit.com/pipermail/drbd-user/", "http://www.linbit.com/en/products-and-services/drbd-support/pricing/pricing-euro", "http://www.drbd.org/", "http://www.drbd.org/home/what-is-drbd/", "http://www.drbd.org/docs/about/", "http://www.drbd.org/users-guide-9.0/", "http://git.drbd.org/"), "true", "predefnode", "true"),
    "13-DreamFactory" -> MarketPlaceInput("13-DreamFactory", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/dreamfactory.png", "Mobile", ""), 
        new MarketPlaceFeatures("", "", "", ""), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "Disaster recovery for your app/service", "Free","8.4.3","", "Ubuntu 13.04 +"))), 
        new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "14-Drupal" -> MarketPlaceInput("14-Drupal", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/drupal.png", "CMS", ""), 
        new MarketPlaceFeatures("", "", "", ""),
        MarketPlacePlans(List(new MarketPlacePlan("0", "Disaster recovery for your app/service", "Free","8.4.3","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "15-Elgg" -> MarketPlaceInput("15-Elgg", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/elgg.png", "Social", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "16-Firepad" -> MarketPlaceInput("16-Firepad", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/firepad.png", "DevelopmentPlatform", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "2-Ghost" -> MarketPlaceInput("2-Ghost", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/ghost.png", "Blog", "Ghost is an Open Source application which allows you to write and publish your own blog, giving you the tools to make it easy and even fun to do."), new MarketPlaceFeatures("Ghost has an incredibly simple concept for writing. You write in Markdown on the left, and you see an immediate preview of exactly what your post will look like in rendered HTML on the right", "The Ghost Open Marketplace contains a large directory of themes, apps, and resources which have been created for Ghost.", "Drag and drop the widgets to create your own custom dashboard, with the most important information first.",""), MarketPlacePlans(List(new MarketPlacePlan("0", "Just a blogging platform", "Free","0.4.1","https://github.com/indykish/ghost.git", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("https://ghost.org/forum/", "mailto:support@ghost.org", "https://ghost.org/", "https://ghost.org/features/", "https://ghost.org/about/", "https://github.com/TryGhost/Ghost/wiki", "https://github.com/tryghost/Ghost"), "false", "predefnode", "true"),
    "17-Gitlab" -> MarketPlaceInput("17-Gitlab", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/gitlab.png", "DevelopmentPlatform", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "18-Hadoop" -> MarketPlaceInput("18-Hadoop", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/hadoop.png", "BusinessIntelligence", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "3-Jenkins" -> MarketPlaceInput("3-Jenkins", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/jenkins.png", "DevelopmentPlatform", "Jenkins CI is the leading open-source continuous integration server. Built with Java, it provides atleast 800+ plugins to support building and testing virtually any project."), 
        new MarketPlaceFeatures("Jenkins can be configured entirely from its friendly web GUI with extensive on-the-fly error checks and inline help. There's no need to tweak XML manually anymore, although if you'd like to do so, you can do that.", "Jenkins gives you clean readable URLs for most of its pages, including some permalinks like 'latest build' / 'latest successful build'.", "JUnit test reports can be tabulated, summarized, and displayed with history information.", "Jenkins can keep track of which build produced which jars, and which build is using which version of jars, and so on."),
        MarketPlacePlans(List(new MarketPlacePlan("0", "An extendable open source continuous integration server.", "Free","1.558","https://s3-ap-southeast-1.amazonaws.com/megampub/0.3/war/jenkins.war", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("https://groups.google.com/forum/#!forum/jenkinsci-users", "https://wiki.jenkins-ci.org/display/JENKINS/Commercial+Support", "http://jenkins-ci.org/", "http://jenkins-ci.org/node", "http://jenkins-ci.org/node", "https://wiki.jenkins-ci.org/display/JENKINS/Home", "https://github.com/jenkinsci/jenkins"), "false", "predefnode", "true"),
    "19-Joomla" -> MarketPlaceInput("19-Joomla", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/joomla.png", "CMS", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "20-Liferay" -> MarketPlaceInput("20-Liferay", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/liferay.png", "Collaboration", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "21-Magneto" -> MarketPlaceInput("21-Magneto", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/magneto.png", "e-Commerce",""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "22-MediaWiki" -> MarketPlaceInput("22-MediaWiki", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/mediawiki.png", "Wiki", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "23-OpenAM" -> MarketPlaceInput("23-OpenAM", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openam.png", "Identity", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "24-OpenAtrium" -> MarketPlaceInput("24-OpenAtrium", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openatrium.png", "ProjectManagement", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "25-OpenDJ" -> MarketPlaceInput("25-OpenDJ", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/opendj.png", "DirectoryServices", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "26-OpenERP" -> MarketPlaceInput("26-OpenERP", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openerp.png", "ERP", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "27-OpenLDAP" -> MarketPlaceInput("27-OpenLDAP", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openldap.png", "DirectoryServices", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "28-OTRS" -> MarketPlaceInput("28-OTRS", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/otrs.png", "HelpDesk", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "29-OwnCloud" -> MarketPlaceInput("29-OwnCloud", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/owncloud.png", "Collaboration", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "30-Redmine" -> MarketPlaceInput("30-Redmine", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/redmine.png", "ProjectManagement", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "31-ReviewBoard" -> MarketPlaceInput("31-ReviewBoard", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/reviewboard.png", "DevelopmentPlatform", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "4-Riak" -> MarketPlaceInput("4-Riak", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/riak.png", "DB", "Riak is a <span class='label label-info'>distributed database</span> designed to deliver maximum data availability by <span class='label label-info'>distributing data across multiple servers</span>."), new MarketPlaceFeatures("Riak replicates and retrieves data intelligently so it is available for read and write operations, even in failure conditions. You can lose access to many nodes due to network partition or hardware failure without losing data. Add new machines to your Riak cluster easily without incurring a larger operational burden – the same ops tasks apply to small clusters as large clusters.", "Riak automatically distributes data around the cluster and yields a near-linear performance increase as you add capacity. Use Riak’s distributed, full-text search engine with a robust query language. Tag objects stored in Riak with additional values and query by exact match or range.", "Non-key-based querying for large datasets.Data is distributed across nodes using consistent hashing. Consistent hashing ensures data is evenly distributed around the cluster and new nodes can be added automatically, with minimal reshuffling. Riak uses Folsom, an Erlang-based system that collects and reports real-time metrics, to provide stats via HTTP request.", ""), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "The World’s Most Scalable Distributed Database is Now Just a Few Clicks Away.", "Free","1.4.8","https://s3-ap-southeast-1.amazonaws.com/megampub/marketplace/riak/riak_1.4.8-1_amd64.deb", "Ubuntu 13.04 +"), new MarketPlacePlan("0", "The World’s Most Scalable Distributed Database is Now Just a Few Clicks Away.", "Free","2.0.beta1","https://s3-ap-southeast-1.amazonaws.com/megampub/marketplace/riak/riak_2.0.0beta1-1_amd64.deb", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("http://docs.basho.com/riak/latest/community/", "http://basho.com/contact/", "http://basho.com/riak/", "http://basho.com/assets/RelationaltoRiak.pdf", "http://basho.com/", "http://docs.basho.com/riak/", "https://github.com/basho/riak"), "false", "predefnode", "true"),
    "5-SCMManager" -> MarketPlaceInput("5-SCMManager", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/scmmanager.png", "DevelopmentPlatform", "A NEW DIMENSION OF SOURCE CODE MANAGEMENT:</br>Enjoy the fascinating possibilities of secure and innovative source code management by using the open source software SCM-Manager.</br> SCM-Manager allows you to manage your source code repositories via an intuitive web interface. It unifies the management of Git, Subversion and Mercurial repositories and enables distributed development teams to administer user rights without having to deal with complex configuration files."), 
        new MarketPlaceFeatures("SCM-Manager is completely configureable from its Web-Interface. No Apache and no database installation is required. Central user, group and permission management.", "Out of the box support for Git, Mercurial and Subversion. Full RESTFul Web Service API (JSON and XML). Rich User Interface.", "Trigger builds directly to cloud using scm-megam plugin", ""), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "Easiest way to share and manage your Git repositories over http.", "Free","1.36","https://s3-ap-southeast-1.amazonaws.com/megampub/0.3/war/scmmanager.war", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("https://groups.google.com/forum/#!forum/scmmanager", "https://www.scm-manager.com/support/", "https://www.scm-manager.com/", "http://www.scm-manager.org/", "http://www.scm-manager.org/", "https://bitbucket.org/sdorra/scm-manager/wiki/Home", "https://bitbucket.org/sdorra/scm-manager/src"), "false", "predefnode", "true"),
    "32-SugarCRM" -> MarketPlaceInput("32-SugarCRM", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/sugarcrm.png", "CRM", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "33-ThinkUp" -> MarketPlaceInput("33-ThinkUp", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/thinkup.png", "Social", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "34-Trac" -> MarketPlaceInput("34-Trac", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/trac.png", "BugTracking", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "35-TWiki" -> MarketPlaceInput("35-TWiki", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/twiki.png", "Wiki", ""),  new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "6-WordPress" -> MarketPlaceInput("6-WordPress", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/wordpress.png", "CMS", "WordPress is web software you can use to create a beautiful website or blog. WordPress is both free and priceless at the same time. The core software is built by hundreds of community volunteers, and when you’re ready for more there are thousands of plugins and themes available to transform your site into almost anything you can imagine."),  new MarketPlaceFeatures("Start a blog or build a full-fledged website. The only limit is your imagination. Build a great site and spend nothing, zilch, nada. And if you want to make a great site even better, a great selection of premium upgrades. Share your work with the world.", "Publicize lets you connect your WordPress site to the most popular social networks — Facebook, Twitter, Tumblr, LinkedIn, and more. Learn more about your readers, where they’re from, and how they found you. Maps and graphs that beautifully present your stats. Don’t be confined to the desk. Publish near and far with mobile apps for iPhone, iPad, Android, and BlackBerry.", "Your site is hosted on cloud across multiple data centers. That way, it’s super fast and always available. Wordpress dashboard is available in over 50 languages and counting.", "Whether you’re searching the forums, reading the support pages, you can always find a helping hand. Keep track of all your favorite blogs and discover new ones with the Reader."), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "WordPress - Launch your Blog in Cloud.", "Free", "3.8.1","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("http://wordpress.org/support/", "#", "http://wordpress.org/", "http://codex.wordpress.org/Main_Page", "http://wordpress.com/", "http://codex.wordpress.org/Getting_Started_with_WordPress", "https://github.com/WordPress/WordPress"), "false", "predefnode", "true"),
    "36-XWiki" -> MarketPlaceInput("36-XWiki", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/xwiki.png", "Wiki", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "Free","","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false"),
    "10-Zarafa" -> MarketPlaceInput("10-Zarafa", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/zarafa.png", "Email", "Zarafa is a collaboration platform that offers integration with existing mail clients and mobile devices. It also offers its own web-based client. The introduction of WebApp is a milestone for Zarafa because the browser-based collaboration client goes beyond Outlook. This next-generation client includes multi-user weekly calendaring and advanced delegation via the browser."), new MarketPlaceFeatures("Zarafa is the only open-source collaboration platform that has a fully MAPI based architecture, making it scalable and compatible with Outlook", "You can integrate ZCP with your existing Linux mail server infrastructure, which means that Zarafa doesn’t provide its own configured SMTP server, web server or database server. In this respect, ZCP differs from other solutions with their own specially developed, compiled and configured components. Their MTA, database and SSL suffer from higher security and TCO costs, as they cannot benefit from the supported OS.", "Zarafa is compatible with BlackBerry handhelds and with ActiveSync enabled handhelds using Z-Push.", "Zarafa has integrated best-in-breed open-source components such as MTAs (such as Postfix), SQL databases and Apache web servers. These leading open-source products also form the foundations of the world’s leading web platforms."), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "The Best Open Source Email & Collaboration Software", "Free","7.1.7","https://s3-ap-southeast-1.amazonaws.com/megampub/marketplace/zarafa/zcp-7.1.9-44333-debian-7.0-x86_64.tar.gz", "Debian Wheezy 7.1"))), new MarketPlaceAppLinks("https://forums.zarafa.com/", "http://www.zarafa.com/content/professional-support", "http://www.zarafa.com/", "http://www.postgresql.org/community/", "http://www.zarafa.com/doc/en", "http://www.zarafa.com/doc/en", "http://download.zarafa.com/community/final/"), "false", "predefnode", "true"),
    "7-PostgreSQL" -> MarketPlaceInput("7-PostgreSQL", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/postgres.png", "DB", "PostgreSQL is a powerful, open source object-relational database system. It has more than 15 years of active development and a proven architecture that has earned it a strong reputation for reliability, data integrity, and correctness. It runs on all major operating systems, including Linux, UNIX (AIX, BSD, HP-UX, SGI IRIX, Mac OS X, Solaris, Tru64), and Windows. It is fully ACID compliant, has full support for foreign keys, joins, views, triggers, and stored procedures (in multiple languages). It includes most SQL:2008 data types, including INTEGER, NUMERIC, BOOLEAN, CHAR, VARCHAR, DATE, INTERVAL, and TIMESTAMP. It also supports storage of binary large objects, including pictures, sounds, or video. "), new MarketPlaceFeatures("Our software has been designed and created to have much lower maintenance and tuning requirements than the leading proprietary databases, yet still retain all of the features, stability, and performance.In addition to this, our training programs are generally regarded as being far more cost effective, manageable, and practical in the real world than that of the leading proprietary database vendors.", "We use a multiple row data storage strategy called MVCC to make PostgreSQL extremely responsive in high volume environments.  The leading proprietary database vendor uses this technology as well, for the same reasons.", "There are many high-quality GUI Tools available for PostgreSQL from both open source developers and commercial providers.", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "Open source object-relational database system.", "Free","9.1","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("http://www.postgresql.org/list/", "http://www.postgresql.org/support/professional_support/", "http://www.postgresql.org/", "http://www.postgresql.org/community/", "http://www.postgresql.org/docs/manuals/", "http://www.postgresql.org/docs/", "http://www.postgresql.org/ftp/source/"), "false", "predefnode", "true"), 
    "8-Redis" -> MarketPlaceInput("8-Redis", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/redis.png", "DB", "Redis is an open source, BSD licensed, advanced key-value store. It is often referred to as a data structure server since keys can contain strings, hashes, lists, sets and sorted sets."), new MarketPlaceFeatures("Redis is an open source, BSD licensed, advanced key-value store. It is often referred to as a data structure server since keys can contain strings, hashes, Lists, sets and sorted sets.You can run atomic operations on these types, like appending to a string; incrementing the value in a hash; pushing to a List; computing set intersection, union and difference; or getting the member with highest ranking in a sorted set.", "In order to achieve its outstanding performance, Redis works with an in-memory dataset. Depending on your use case, you can persist it either by dumping the dataset to disk every once in a while, or by appending each command to a log. Redis also supports trivial-to-setup master-slave replication, with very fast non-blocking first synchronization, auto-reconnection on net split and so forth.", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "Advanced key value store.", "Free","2.8.6","", "Ubuntu 13.04 +"))), new MarketPlaceAppLinks("https://groups.google.com/forum/#!forum/redis-db", "http://www.gopivotal.com/big-data/redis", "http://redis.io/", "http://redis.io/commands", "http://docs.cloudno.de/redis", "http://redis.io/documentation", "https://github.com/antirez/redis"), "false", "predefnode", "true"), 
    "9-Op5" -> MarketPlaceInput("9-Op5", new MarketPlaceAppDetails("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/op5.png", "Monitor","Take Control of your IT op5 helps you as an IT professional to get in control of IT stuff. From the basement server to the hybrid cloud, our open and flexible server monitoring solution delivers a unified view of your entire IT."),  new MarketPlaceFeatures("op5 Monitor allows you to easily monitor all kinds of servers and provide alerting, reporting and visualization. op5 Monitor can monitor physical, virtual, cloud and hybrid types of server environments as well as all layers needed to ensure high quality of services.", "op5 Monitor was specifically designed to be a flexible and scalable solution able to handle very large volumes of monitored devices and checks, as well as being optimized to achieve very low per-device overheads. op5 Monitor can handle distributed monitoring, load sharing and scale to several thousands of op5 Monitor Peers with automatic failover from pollers.", "op5 Monitor presents detailed information about the performance of thousands of devices and interfaces in your network. From a single user interface, you can drill down into any element on your network to see exactly what’s happening.", "op5 Monitor includes BSM functionality that enables managing, monitoring and measuring information technology as it relates to an organization’s business processes."), 
        MarketPlacePlans(List(new MarketPlacePlan("0", "op5 Monitor for your Enterprise", "Free","6.2.1","https://s3-ap-southeast-1.amazonaws.com/megampub/0.3/op5/op5-monitor-6.2.1.tar.gz", "CentOS 6.3"), 
        new MarketPlacePlan("0", "op5 Monitor for your Enterprise", "Free","6.3 Beta","https://s3-ap-southeast-1.amazonaws.com/megampub/0.3/op5/op5-monitor-6.3.0-beta1-public_beta-20140311.tar.gz", "CentOS 6.3"))), 
        new MarketPlaceAppLinks("https://kb.op5.com/", "https://www.op5.com/support/", "https://www.op5.com/", "https://www.op5.com/explore-op5-monitor/", "", "https://www.op5.com/support/documentation/", "http://git.op5.org/git/"), "false", "predefnode", "true")) 
    
  val toStream = toMap.keySet.toStream

}

case class MarketPlaceResult(id: String, name: String, appdetails: MarketPlaceAppDetails, features: MarketPlaceFeatures, plans: MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object MarketPlaces {

  implicit val formats = DefaultFormats
  private def riak: GSRiak = GSRiak(MConfig.riakurl, "marketplaces")
  implicit def MarketPlacesSemigroup: Semigroup[MarketPlaceResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  //implicit def MarketPlacePlansSemigroup: Semigroup[MarketPlacePlans] = Semigroup.instance((f3, f4) => f3.append(f4))

  val metadataKey = "MarketPlace"
  val metadataVal = "MarketPlaces Creation"
  val bindex = BinIndex.named("marketplace")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val marketPlaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch {
      parse(input).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- marketPlaceInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(mkp.name)
      val json = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.appdetails, mkp.features, mkp.plans, mkp.applinks, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  private def mkGunnySack_init(input: MarketPlaceInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.MarketPlaces mkGunnySack_init: entry--------------------:\n" + input.json)
    val marketplaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch {
      parse(input.json).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure
    play.api.Logger.debug("models.MarketPlaces mkGunnySack: entry--------------------:\n" + marketplaceInput)

    for {
      mkp <- marketplaceInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.marketplaces mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(mkp.name)
      val mkpJson = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.appdetails, mkp.features, mkp.plans, mkp.applinks, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, mkpJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name marketplace name will point to the "marketplaces" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[MarketPlaceResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlace.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[MarketPlaceResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def createMany(marketPlaceInput: Map[String, MarketPlaceInput]): ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug("models.MarketPlaces create: entry")
    play.api.Logger.debug(("%-20s -->[%s]").format("value", marketPlaceInput))
    (marketPlaceInput.toMap.some map {
      _.map { p =>
        play.api.Logger.debug(("%-20s -->[%s]").format("value", p))
        (mkGunnySack_init(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => MarketPlaceResults(parse(thatGS.value).extract[MarketPlaceResult]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlaces.created success", "Scaliak returned => None. Thats OK."))
                  MarketPlaceResults(MarketPlaceResult(new String(), p._2.name, p._2.appdetails, p._2.features, p._2.plans, p._2.applinks, p._2.attach, p._2.predefnode, p._2.approved, new String())).successNel[Throwable]
                }
              }
            }
        }
      }
    } map {
      _.fold((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _) //fold or foldRight ? 
    }).head //return the folded element in the head.  

  }

  def findByName(marketPlacesNameList: Option[Stream[String]]): ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("marketPlaceList", marketPlacesNameList))
    (marketPlacesNameList map {
      _.map { marketplacesName =>
        InMemory[ValidationNel[Throwable, MarketPlaceResults]]({
          cname: String =>
            {
              play.api.Logger.debug("models.MarketPlaceName findByName: marketplaces:" + marketplacesName)
              (riak.fetch(marketplacesName) leftMap { t: NonEmptyList[Throwable] =>
                new ServiceUnavailableError(marketplacesName, (t.list.map(m => m.getMessage)).mkString("\n"))
              }).toValidationNel.flatMap { xso: Option[GunnySack] =>
                xso match {
                  case Some(xs) => {
                    (Validation.fromTryCatch {
                      parse(xs.value).extract[MarketPlaceResult]
                    } leftMap { t: Throwable =>
                      new ResourceItemNotFound(marketplacesName, t.getMessage)
                    }).toValidationNel.flatMap { j: MarketPlaceResult =>
                      Validation.success[Throwable, MarketPlaceResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
                    }
                  }
                  case None => Validation.failure[Throwable, MarketPlaceResults](new ResourceItemNotFound(marketplacesName, "")).toValidationNel
                }
              }
            }
        }).get(marketplacesName).eval(InMemoryCache[ValidationNel[Throwable, MarketPlaceResults]]())
      }
    } map {
      _.foldRight((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  def listAll: ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlace", "listAll:Entry"))
    findByName(MarketPlaceInput.toStream.some) //return the folded element in the head.  
  }

  implicit val sedimentMarketPlacesResults = new Sedimenter[ValidationNel[Throwable, MarketPlaceResults]] {
    def sediment(maybeASediment: ValidationNel[Throwable, MarketPlaceResults]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->MKP:sediment:", notSed))
      notSed
    }
  }

}