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
package models.json.tosca

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
import models.tosca.{ ServiceInputs }

/**
 * @author rajthilak
 *
 */

object ServiceInputsSerialization extends SerializationBase[ServiceInputs] {

  protected val DBNameKey = "dbname"
  protected val DBPasswordKey = "dbpassword" 

  override implicit val writer = new JSONW[ServiceInputs] {

    override def write(h: ServiceInputs): JValue = {
      JObject(    
          JField(DBNameKey, toJSON(h.dbname)) ::
          JField(DBPasswordKey, toJSON(h.dbpassword)) ::         
           Nil)
    }
  }

  override implicit val reader = new JSONR[ServiceInputs] {

    override def read(json: JValue): Result[ServiceInputs] = {  
      val dbnameField = field[String](DBNameKey)(json)    
      val dbpasswordField = field[String](DBPasswordKey)(json) 
      
      (dbnameField |@| dbpasswordField ) { 
        (dbname: String, dbpassword: String) =>
          new ServiceInputs(dbname, dbpassword)
      }
    }
  }
}