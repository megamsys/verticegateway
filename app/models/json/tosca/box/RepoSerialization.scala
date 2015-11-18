/*
** Copyright [2013-2015] [Megam Systems]
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
package models.json.tosca.box

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
import models.tosca.{ Repo, KeyValueList }

/**
 * @author rajthilak
 *
 */

object RepoSerialization extends models.json.SerializationBase[Repo] {

  protected val RepoTypeKey = "rtype"
  protected val SourceKey = "source"
  protected val OneclickKey = "oneclick"
  protected val UrlKey = "url"

  override implicit val writer = new JSONW[Repo] {


    override def write(h: Repo): JValue = {
      JObject(
        JField(RepoTypeKey, toJSON(h.rtype)) ::
          JField(SourceKey, toJSON(h.source)) ::
          JField(OneclickKey, toJSON(h.oneclick)) ::
          JField(UrlKey, toJSON(h.url)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Repo] {

    override def read(json: JValue): Result[Repo] = {
      val repotypeField = field[String](RepoTypeKey)(json)
      val sourceField = field[String](SourceKey)(json)
      val oneclickField = field[String](OneclickKey)(json)
      val urlField = field[String](UrlKey)(json)

      (repotypeField |@| sourceField |@| oneclickField |@| urlField) {
        (rtype: String, source: String, oneclick: String, url: String) =>
          new Repo(rtype, source, oneclick, url)
      }
    }
  }
}
