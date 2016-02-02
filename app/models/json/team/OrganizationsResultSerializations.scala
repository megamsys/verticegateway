/*
** Copyright [2013-2016] [Megam Systems]
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
package models.json.team

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._
import models.team.{ OrganizationsResult }

/**
 * @author morpheyesh
 *
 */
class OrganizationsResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[OrganizationsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

      protected val IdKey = "id"
      protected val AccountIdKey = "accounts_id"
      protected val NameKey = "name"
      protected val RelatedOrgsKey = "related_orgs"
      protected val CreatedAtKey ="created_at"


  override implicit val writer = new JSONW[OrganizationsResult] {

    import RelatedOrgsListSerialization.{ writer => RelatedOrgsListWriter}

    override def write(h: OrganizationsResult): JValue = {
      JObject(
           JField(IdKey, toJSON(h.id)) ::
           JField(AccountIdKey, toJSON(h.accounts_id)) ::
           JField(JSONClazKey, toJSON("Megam::Organizations")) ::
           JField(NameKey, toJSON(h.name)) ::
          // JField(RelatedOrgsKey, toJSON(h.related_orgs)) ::
           JField(RelatedOrgsKey, toJSON(h.related_orgs)(RelatedOrgsListWriter)) ::
           JField(CreatedAtKey, toJSON(h.created_at))   ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[OrganizationsResult] {


        import RelatedOrgsListSerialization.{ reader => RelatedOrgsListReader}


    override def read(json: JValue): Result[OrganizationsResult] = {

       val idField = field[String](IdKey)(json)
       val accountIdField = field[String](AccountIdKey)(json)
      val nameField = field[String](NameKey)(json)
     // val relatedOrgsField = field[List[String]](RelatedOrgsKey)(json)
      val relatedOrgsField= field[List[String]](RelatedOrgsKey)(json)(RelatedOrgsListReader)
      val createdAtField = field[String](CreatedAtKey)(json)



      (idField |@|accountIdField |@| nameField |@| relatedOrgsField |@| createdAtField) {
        (id: String, accountId: String, name: String, related_orgs: List[String], created_at: String) =>
          new OrganizationsResult(id, accountId, name, related_orgs, created_at)
      }
    }
  }
}
