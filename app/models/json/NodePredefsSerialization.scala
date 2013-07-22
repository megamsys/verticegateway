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
import models.{ NodeResult, NodePredefs, NodeRequest }

/**
 * @author ram
 *
 */
object NodePredefsSerialization extends SerializationBase[NodePredefs] {
  protected val NameKey = "name"
  protected val SCMKey = "scm"
  protected val WARKey = "war"
  protected val DBKey = "db"
  protected val QueueKey = "queue"

  override implicit val writer = new JSONW[NodePredefs] {

    override def write(h: NodePredefs): JValue = {
      JObject(
        JField(NameKey, toJSON(h.name)) ::
          JField(SCMKey, toJSON(h.scm)) ::
          JField(WARKey, toJSON(h.war)) ::
          JField(DBKey, toJSON(h.db)) ::
          JField(QueueKey, toJSON(h.queue)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodePredefs] {

    override def read(json: JValue): Result[NodePredefs] = {
      val nameField = field[String](NameKey)(json)
      val scmField = field[String](SCMKey)(json)
      val warField = field[String](WARKey)(json)
      val dbField = field[String](DBKey)(json)
      val queueField = field[String](QueueKey)(json)

      (nameField |@| scmField |@| warField |@| dbField |@| queueField) {
        (name: String, scm: String, war: String, db: String, queue: String) =>
          new NodePredefs(name, scm, war, db, queue)
      }
    }
  }
}