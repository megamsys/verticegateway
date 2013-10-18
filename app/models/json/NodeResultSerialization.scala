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
import models.{ NodeResult, NodePredefs, NodeRequest, NodeStatusType, NodeAppDefns}
import models.NodeStatusType._
import org.megam.common.enumeration._

/**
 * @author ram
 *
 */
class NodeResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[NodeResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val AccountsIDKey = "accounts_id"
  protected val StatusKey = "status"
  protected val RequestKey = "request"
  protected val PredefsKey = "predefs"
  protected val AppDefnsIdKey = "appdefnsid"  
    
    
  override implicit val writer = new JSONW[NodeResult] {

    import NodeRequestSerialization.{ writer => NodeRequestWriter }
    import NodePredefsSerialization.{ writer => NodePredefsWriter }
    import NodeStatusTypeSerialization.{ writer => NodeStatusTypeWriter }
    import NodeAppDefnsSerialization.{ writer => NodeAppDefnsWriter }
    
    override def write(h: NodeResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountsIDKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::Node")) ::
          JField(StatusKey, toJSON(h.status)(NodeStatusTypeWriter)) ::
          JField(RequestKey, toJSON(h.request)(NodeRequestWriter)) ::
          JField(PredefsKey, toJSON(h.predefs)(NodePredefsWriter)) :: 
          JField(AppDefnsIdKey, toJSON(h.appdefnsid)) :: 
           Nil)
    }
  }

  override implicit val reader = new JSONR[NodeResult] {

    import NodeRequestSerialization.{ reader => NodeRequestReader }
    import NodePredefsSerialization.{ reader => NodePredefsReader }
    import NodeStatusTypeSerialization.{ reader => NodeStatusTypeReader }
    import NodeAppDefnsSerialization.{ reader => NodeAppDefnsReader }
    
    override def read(json: JValue): Result[NodeResult] = {
      val statusField = field[NodeStatusType](StatusKey)(json)(NodeStatusTypeReader)

      statusField.flatMap { statusType: NodeStatusType =>
        val idField = field[String](IdKey)(json)
        val accountField = field[String](AccountsIDKey)(json)
        val requestField = field[NodeRequest](RequestKey)(json)(NodeRequestReader)
        val predefsField = field[NodePredefs](PredefsKey)(json)(NodePredefsReader)
        val appdefnsidField = field[String](AppDefnsIdKey)(json)        
        val noderes_fn = idField |@| accountField |@| requestField |@| predefsField |@| appdefnsidField 

        val res: ValidationNel[Error, NodeResult] = statusType match {
          case NodeStatusType.AM_HUNGRY              => noderes_fn(NodeResult(_, _, NodeStatusType.REQ_CREATED_AT_SOURCE, _, _, _))
          case NodeStatusType.REQ_CREATED_AT_SOURCE  => noderes_fn(NodeResult(_, _, NodeStatusType.REQ_CREATED_AT_SOURCE, _, _, _))
          case NodeStatusType.NODE_CREATED_AT_SOURCE => noderes_fn(NodeResult(_, _, NodeStatusType.NODE_CREATED_AT_SOURCE, _, _, _))
          case NodeStatusType.PUBLISHED              => noderes_fn(NodeResult(_, _, NodeStatusType.PUBLISHED, _, _, _))
          case NodeStatusType.STARTED                => noderes_fn(NodeResult(_, _, NodeStatusType.STARTED, _, _, _))
          case NodeStatusType.LAUNCH_SUCCESSFUL      => noderes_fn(NodeResult(_, _, NodeStatusType.LAUNCH_SUCCESSFUL, _, _, _))
          case NodeStatusType.LAUNCH_FAILED          => noderes_fn(NodeResult(_, _, NodeStatusType.LAUNCH_FAILED, _, _, _))
          case _ => UncategorizedError("request type",
            "unsupported request type %s".format(statusType.stringVal),
            List()).failNel
        }
        res

      }
    }
  }
}