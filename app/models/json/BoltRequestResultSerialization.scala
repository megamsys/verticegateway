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
import models.{BoltRequestResult}

/**
 * @author ram
 *
 */
class BoltRequestResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[BoltRequestResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeIDKey = "node_id"
  protected val NodeNameKey = "node_name"
  protected val ReqTypeKey = "req_type"  
  protected val LcApplyKey = "lc_apply"
  protected val LcAdditionalKey = "lc_additional"
  protected val LcWhenKey = "lc_when" 
  protected val BoltDefnsIdKey = "boltdefns_id"
  protected val CreatedAtKey ="created_at" 

  override implicit val writer = new JSONW[BoltRequestResult] {
    
    override def write(h: BoltRequestResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NodeIDKey, toJSON(h.node_id)) ::
          JField(JSONClazKey, toJSON("Megam::BoltRequest")) ::
          JField(NodeNameKey, toJSON(h.node_name)) ::
          JField(ReqTypeKey, toJSON(h.req_type)) ::
          JField(LcApplyKey, toJSON(h.lc_apply)) ::
          JField(LcAdditionalKey, toJSON(h.lc_additional)) ::
          JField(LcWhenKey, toJSON(h.lc_when)) ::
          JField(BoltDefnsIdKey, toJSON(h.boltdefns_id)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   :: Nil)
    }
  }

  override implicit val reader = new JSONR[BoltRequestResult] {
    
    override def read(json: JValue): Result[BoltRequestResult] = {
      val idField = field[String](IdKey)(json)
      val nodeIdField = field[String](NodeIDKey)(json)
      val nodeNameField = field[String](NodeNameKey)(json)
      val reqtypeField = field[String](ReqTypeKey)(json)
      val lcapplyField = field[String](LcApplyKey)(json)
      val lcadditionalField = field[String](LcAdditionalKey)(json)
      val lcwhenField = field[String](LcWhenKey)(json)
      val boltdefnsidField = field[String](BoltDefnsIdKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nodeIdField |@| nodeNameField |@| boltdefnsidField |@| reqtypeField |@| lcapplyField |@| lcadditionalField |@| lcwhenField |@| createdAtField) {
        (id: String, node_id: String, node_name: String, boltdefns_id: String, req_type: String, lc_apply: String, lc_additional: String, lc_when: String, created_at: String) =>
          new BoltRequestResult(id, node_id, node_name, boltdefns_id, req_type, lc_apply, lc_additional, lc_when, created_at)
      }
    }
  }
}