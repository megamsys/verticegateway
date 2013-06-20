/* 
** Copyright [2012] [Megam Systems]
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

import play.api._
import play.api.mvc._
import scalaz._
import Scalaz._
import net.liftweb.json._
import controllers.stack._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import models._
import org.megam.common.riak.{ GSRiak, GunnySack }

/**
 * @author rajthilak
 *
 */
object PredefClouds {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predefclouds")

  def create(value: String, id: String) = {
    val metadataKey = "predefClouds"
    val metadataVal = "predefs Creation"
    val bindex = BinIndex.named("predefcloud")
    val storeValue = List(riakJSON, nodejsJSON, playJSON, akkaJSON, redisJSON)
    val store = storeList.map(a => {
      val m = parse(a).extract[PredefsJSON]
      val bvalue = Set(m.name)
      val storeValue = riak.store(new GunnySack(m.name, a, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    })    
  }

  
}