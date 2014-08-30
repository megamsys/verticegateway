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
import models.tosca.{ OrganizationsResult }

/**
 * @author morpheyesh
 *
 */
class OrganizationsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[OrganizationsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  
  protected val NameKey = "name"
      protected val IdKey = "id"
     protected val CreatedAtKey ="created_at"
 

  override implicit val writer = new JSONW[OrganizationsResult] {
   
    override def write(h: OrganizationsResult): JValue = {
      JObject(
       
          JField(NameKey, toJSON(h.name)) ::
           JField(IdKey, toJSON(h.id)) ::
           JField(CreatedAtKey, toJSON(h.created_at))   ::          
          Nil)
    }
  }

  override implicit val reader = new JSONR[OrganizationsResult] {
   
    
    

    override def read(json: JValue): Result[OrganizationsResult] = {
      
       val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      

      (idField |@| nameField |@| createdAtField) {
        (id: String, name: String, created_at: String) =>
          new OrganizationsResult(id, name, created_at)
      }
    }
  }
}