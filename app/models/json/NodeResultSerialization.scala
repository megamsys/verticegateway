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
import models.{ NodeResult, NodePredefs, NodeRequest}


/**
 * @author ram
 *
 */
class NodeResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[NodeResult] {
  protected val IdKey = "id"
  protected val AccountsIDKey = "accounts_id"
  protected val RequestKey = "request"
  protected val PredefsKey = "predefs"

  override implicit val writer = new JSONW[NodeResult] {
    
    import NodeRequestSerialization.{ writer => NodeRequestWriter }
    import NodePredefsSerialization.{ writer => NodePredefsWriter }

    override def write(h: NodeResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountsIDKey, toJSON(h.accounts_id)) ::
          JField (RequestKey, toJSON(h.request)(NodeRequestWriter)) ::
          JField(PredefsKey, toJSON(h.predefs)(NodePredefsWriter)) ::Nil)
    }
  }

  override implicit val reader = new JSONR[NodeResult] {
    
    import NodeRequestSerialization.{ reader => NodeRequestReader }
    import NodePredefsSerialization.{ reader => NodePredefsReader }

    override def read(json: JValue): Result[NodeResult] = {
      val idField = field[String](IdKey)(json)
      val accountField = field[String](AccountsIDKey)(json)
      val requestField = field[NodeRequest](RequestKey)(json)(NodeRequestReader)
      val predefsField = field[NodePredefs](PredefsKey)(json)(NodePredefsReader)

      (idField |@| accountField |@| requestField |@| predefsField) {
        (id: String, account_id: String, request: NodeRequest, predefs: NodePredefs) =>
          new NodeResult(id, account_id, request, predefs)
      }
    }
  }
}