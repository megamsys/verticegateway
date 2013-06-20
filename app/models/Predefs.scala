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
case class PredefsJSON(id: String, name: String, provider: String, role: String, packaging: String)

object Predefs extends PredefsHelper {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predeftest10")

  def create = {
    val metadataKey = "predef"
    val metadataVal = "predefs Creation"
    val bindex = BinIndex.named("predefName")
     //val bvalue = Set("nodejs")
     //val storeValue = riak.store(new GunnySack("nodejs", nodejsJSON, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    val storeList = List(riakJSON, nodejsJSON, playJSON, akkaJSON, redisJSON)
    val store = storeList.map(a => {
      val m = parse(a).extract[PredefsJSON]
      val bvalue = Set(m.name)
      val storeValue = riak.store(new GunnySack(m.name, a, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    })    
  }

  /*
   * fetch the object using their key from bucket
   */
  def findByKey(key: String): ValidationNel[Error, Option[PredefsJSON]] = {
    riak.fetch(key) match {
      case Success(msg) => {
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[PredefsJSON]
        Validation.success[Error, Option[PredefsJSON]](Some(m)).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, Option[PredefsJSON]](new Error("Predef.findById: Not Implemented.")).toValidationNel
    }
  }

  def listKeys: ValidationNel[Error, List[String]] = {
    riak.keysList match {
      case Success(key) => {
        println("+++++++++++++++++++++"+(key.toList))
        Validation.success[Error, List[String]](key.toList).toValidationNel
      }
       case Failure(err) => Validation.failure[Error, List[String]](new Error("Predef.list: Not Implemented.")).toValidationNel
    }
  }
  
   def createPredef  = {    
    val res = models.Predefs.listKeys match {
      case Success(t) =>  { 
           if(Nil == t) {
              models.Predefs.create              
           }
             else 
               println("list Value fetch failure")
      }
      case Failure(err) =>
         println("Value fetch failure")
         models.Predefs.create        
    }
  }
  
}