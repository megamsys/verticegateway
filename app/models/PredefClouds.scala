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

case class PredefCloudInput(name: String, predefType: TypeCloud, access: AccessCloud)

case class AccessCloud(ssh_key: String, identity_file: String, ssh_user: String){
  val getPredefsAccessJson = "\"ssh_key\":\"" + ssh_key + "\",\"identity_file\":\"" + identity_file + "\",\"ssh_user\":\"" + ssh_user + "\""
}

case class TypeCloud(typeName: String, groups: String, image: String, flavor: String){
  val getPredefsTypeJson = "\"typeName\":\"" + typeName + "\",\"groups\":\"" + groups + "\",\"image\":\"" + image + "\",\"flavor\":\"" + flavor + "\""
}

case class PredefCloudResult(id: String, name: String, acc_id: String, predefType: TypeCloud, access: AccessCloud, ideal: String, performance: String)

object PredefClouds extends Helper {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predefclouds")

  def create(value: String, acc_id: String) = {
    val metadataKey = "predefClouds"
    val metadataVal = "predefs Creation"
    val bindex = BinIndex.named("predefcloud")
    val m = parse(value).extract[PredefCloudInput]
    val typeJson = (m.predefType).getPredefsTypeJson
    val accessJson = (m.access).getPredefsAccessJson
    val ideal = ""
    val performance = ""
    val json = "{\"id\":\"" + getUID("pcd") + "\",\"name\":\"" + m.name + "\",\"acc_id\":\"" + acc_id + "\",\"predefType\":{" + typeJson + "},\"access\":{" + accessJson + "},\"ideal\":\"" + ideal + "\",\"performance\":\"" + performance + "\"}"                                                          
    val bvalue = Set(acc_id)
    val store = riak.store(new GunnySack(m.name, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

  }
  
   /*
   * fetch the object using their key from bucket
   */
  def findByKey(key: String): ValidationNel[Error, Option[PredefCloudResult]] = {
    riak.fetch(key) match {
      case Success(msg) => {
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[PredefCloudResult]
        Validation.success[Error, Option[PredefCloudResult]](Some(m)).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, Option[PredefCloudResult]](new Error("""
               |In this predef '%s' is doesn't exists in your list 
               |Please add predef's in your list.""".format(key).stripMargin + "\n ")).toValidationNel
    }
  }

  /*
   * Index on ID
   * fetch the object using index
   */
  def findById(id: String): ValidationNel[Error, List[ValidationNel[Error, Option[PredefCloudResult]]]] = {
    val metadataKey = "Nodes"
    val metadataVal = "Nodes-name"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("predefcloud", id, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        val result = msg.map(a => {
          PredefClouds.findByKey(a)
        })
        /*Nodes.findByKey(a) match {
            case Success(msg) => {
              val m = msg.get
              m
            }
            case Failure(_) => ""
          }
        })*/
        Validation.success[Error, List[ValidationNel[Error, Option[PredefCloudResult]]]](result).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, List[ValidationNel[Error, Option[PredefCloudResult]]]](new Error("""
               |In this predef '%s' is doesn't exists in your list 
               |Please add predef's in your list.""".format(id).stripMargin + "\n ")).toValidationNel
    }
  }


}