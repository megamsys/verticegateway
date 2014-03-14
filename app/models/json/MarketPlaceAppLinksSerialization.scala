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
package models.json

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.{ MarketPlaceAppLinks }

/**
 * @author rajthilak
 *
 */

object MarketPlaceAppLinksSerialization extends SerializationBase[MarketPlaceAppLinks] {
 
  protected val FreeSupportKey = "free_support"
  protected val PaidSupportKey = "paid_support"  
  protected val HomeLinkKey = "home_link"
  protected val InfoLinkKey = "info_link"
  protected val ContentLinkKey = "content_link"
  protected val WikiLinkKey = "wiki_link"
    protected val SourceLinkKey = "source_link"

  override implicit val writer = new JSONW[MarketPlaceAppLinks] {

    override def write(h: MarketPlaceAppLinks): JValue = {
      JObject(        
          JField(FreeSupportKey, toJSON(h.free_support)) :: 
          JField(PaidSupportKey, toJSON(h.paid_support)) :: 
          JField(HomeLinkKey, toJSON(h.home_link)) :: 
          JField(InfoLinkKey, toJSON(h.info_link)) :: 
          JField(ContentLinkKey, toJSON(h.content_link)) :: 
          JField(WikiLinkKey, toJSON(h.wiki_link)) :: 
          JField(SourceLinkKey, toJSON(h.source_link)) :: 
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAppLinks] {

    override def read(json: JValue): Result[MarketPlaceAppLinks] = {     
      val freesupportField = field[String](FreeSupportKey)(json)
      val paidsupportField = field[String](PaidSupportKey)(json)
      val homelinkField = field[String](HomeLinkKey)(json)
      val infolinkField = field[String](InfoLinkKey)(json)
      val contentlinkField = field[String](ContentLinkKey)(json)
      val wikilinkField = field[String](WikiLinkKey)(json)
      val sourcelinkField = field[String](SourceLinkKey)(json)
      
      (freesupportField |@| paidsupportField |@| homelinkField |@| infolinkField |@| contentlinkField |@| wikilinkField |@| sourcelinkField) {
        (free_support: String, paid_support: String, home_link: String, info_link: String, content_link: String, wiki_link: String, source_link: String) =>
          new MarketPlaceAppLinks(free_support, paid_support, home_link, info_link, content_link, wiki_link, source_link)
      }
    }
  }
}