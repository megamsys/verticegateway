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
import controllers.stack.MConfig
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import models._
import org.megam.common.riak.{ GSRiak, GunnySack }

/**
 * @author ram
 *
 */

case class NodeResult(id: String, acc_id: String, request_id: String)

object Nodes {
  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")
  val key = "mykey15"
  /*
   * put the value in riak bucket
   */
  def create(input: String): ValidationNel[Error, Option[NodeResult]] = {
    val metadataKey = "Field"
    val metadataVal = "1002"
    val json = parse(input)
    val m = json.extract[NodeResult]    
    val bindex = BinIndex.named("accountID")
    val bvalue = Set(m.acc_id)

    val storeValue = riak.store(new GunnySack(key, input, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    storeValue match {
      case Success(msg) => Validation.success[Error, Option[NodeResult]](None).toValidationNel
      case Failure(err) => Validation.failure[Error, Option[NodeResult]](new Error("Node.create: Not Implemented.")).toValidationNel
    }
  }

  /*
   * fetch the object using their key from bucket
   */
  def findById(key: String): ValidationNel[Error, Option[NodeResult]] = {
    riak.fetch(key) match {
      case Success(msg) => {       
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[NodeResult]        
        Validation.success[Error, Option[NodeResult]](Some(m)).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, Option[NodeResult]](new Error("Node.findById: Not Implemented.")).toValidationNel
    }
  }

  /**
   * Index on email
   */
  def findByEmail(email: String): ValidationNel[Error, Option[NodeResult]] = {
    val metadataKey = "Field"
    val metadataVal = "1002"
    val bindex = BinIndex.named("")
    val bvalue = Set("")  
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountID", email, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {       
        val key1 = msg match {
          case List(x) => x
        }        
        Nodes.findById(key1)
      }
      case Failure(err) => Validation.failure[Error, Option[NodeResult]](new Error("Email is not already exists")).toValidationNel
    }
  }

}

