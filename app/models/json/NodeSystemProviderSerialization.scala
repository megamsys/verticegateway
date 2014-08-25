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
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.{ NodeSystemProvider, NodeProvider }

/**
 * @author ram
 *
 */
object NodeSystemProviderSerialization extends SerializationBase[NodeSystemProvider] {
  protected val ProviderKey = "provider"

  override implicit val writer = new JSONW[NodeSystemProvider] {

    import NodeProviderSerialization.{ writer => NodeProviderWriter }

    override def write(h: NodeSystemProvider): JValue = {
      JObject(
        JField(ProviderKey, toJSON(h.provider)(NodeProviderWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeSystemProvider] {

    import NodeProviderSerialization.{ reader => NodeProviderReader }

    override def read(json: JValue): Result[NodeSystemProvider] = {
      val sys_provValSF = field[NodeProvider](ProviderKey)(json)(NodeProviderReader)
      sys_provValSF map {provVal => new NodeSystemProvider(provVal) }      
    }
  }
}