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
package models.json


import scalaz._
import Scalaz._
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.{ NodeResult, NodePredefs, NodeRequest, NodeStatusType, NodeAppDefns }
import models.NodeStatusType._
import org.megam.common.enumeration._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * @author ram
 *
 */
class NodeResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[NodeResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeNameKey = "node_name"
  protected val AccountsIDKey = "accounts_id"
  protected val NodeTypeKey = "node_type"
  protected val StatusKey = "status"
  protected val RequestKey = "request"
  protected val PredefsKey = "predefs"
  protected val AppDefnsIdKey = "appdefnsid"
  protected val BoltDefnsIdKey = "boltdefnsid"
  protected val CreatedAtKey = "created_at"

   implicit override val writer = new JSONW[NodeResult] {

    import NodeRequestSerialization.{ writer => NodeRequestWriter }
    import NodePredefsSerialization.{ writer => NodePredefsWriter }
    import NodeStatusTypeSerialization.{ writer => NodeStatusTypeWriter }
    import NodeAppDefnsSerialization.{ writer => NodeAppDefnsWriter }

    override def write(h: NodeResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NodeNameKey, toJSON(h.node_name)) ::
          JField(AccountsIDKey, toJSON(h.accounts_id)) ::
          JField(NodeTypeKey, toJSON(h.node_type)) ::
          JField(JSONClazKey, toJSON("Megam::Node")) ::
          JField(StatusKey, toJSON(h.status)(NodeStatusTypeWriter)) ::
          JField(RequestKey, toJSON(h.request)(NodeRequestWriter)) ::
          JField(PredefsKey, toJSON(h.predefs)(NodePredefsWriter)) ::
          JField(AppDefnsIdKey, toJSON(h.appdefnsid)) ::
          JField(BoltDefnsIdKey, toJSON(h.boltdefnsid)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[NodeResult] {

    import NodeRequestSerialization.{ reader => NodeRequestReader }
    import NodePredefsSerialization.{ reader => NodePredefsReader }
    import NodeStatusTypeSerialization.{ reader => NodeStatusTypeReader }
    import NodeAppDefnsSerialization.{ reader => NodeAppDefnsReader }

    override def read(json: JValue): Result[NodeResult] = {
      val statusField = field[NodeStatusType](StatusKey)(json)(NodeStatusTypeReader)

      (statusField.flatMap { statusType: NodeStatusType =>
        val idField = field[String](IdKey)(json)
        val nodenameField = field[String](NodeNameKey)(json)
        val accountField = field[String](AccountsIDKey)(json)
        val nodetypeField = field[String](NodeTypeKey)(json)
        val requestField = field[NodeRequest](RequestKey)(json)(NodeRequestReader)
        val predefsField = field[NodePredefs](PredefsKey)(json)(NodePredefsReader)
        val appdefnsidField = field[String](AppDefnsIdKey)(json)
        val boltdefnsidField = field[String](BoltDefnsIdKey)(json)
        val createdAtField = field[String](CreatedAtKey)(json)
        val noderes_fn = idField |@| nodenameField |@| accountField |@| nodetypeField |@| requestField |@| predefsField |@| appdefnsidField |@| boltdefnsidField |@| createdAtField

        val res: ValidationNel[Error, NodeResult] = statusType match {
          case NodeStatusType.NONE => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.NONE, _, _, _, _, _))
          case NodeStatusType.AM_HUNGRY => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.AM_HUNGRY, _, _, _, _, _))
          case NodeStatusType.LAUNCHING => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.LAUNCHING, _, _, _, _, _))
          case NodeStatusType.RUNNING => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.RUNNING, _, _, _, _, _))
          case NodeStatusType.NOTRUNNING => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.NOTRUNNING, _, _, _, _, _))
          case NodeStatusType.DELETED => noderes_fn(NodeResult(_, _, _, _, NodeStatusType.DELETED, _, _, _, _, _))
          case _ => UncategorizedError("status type",
            "unsupported status type %s".format(statusType.stringVal),
            List()).failureNel
        }
        res

      }) 
    }
  }
}