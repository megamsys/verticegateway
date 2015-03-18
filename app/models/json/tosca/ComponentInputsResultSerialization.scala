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
import models.tosca.{ ComponentInputsResult, DesignInputs, ServiceInputs, CI }

/**
 * @author rajthilak
 *
 */

object ComponentInputsResultSerialization extends SerializationBase[ComponentInputsResult] {

  protected val DomainKey = "domain"
  protected val PortKey = "port"
  protected val UserNameKey = "username"
  protected val PasswordKey = "password" 
  protected val VersionKey = "version"
  protected val SourceKey = "source"
  protected val DesignInputsKey = "design_inputs"
  protected val ServiceInputsKey = "service_inputs"
  protected val CIDKey = "ci_id"

  override implicit val writer = new JSONW[ComponentInputsResult] {

     import DesignInputsSerialization.{ writer => DesignInputsWriter }
     import ServiceInputsSerialization.{ writer => ServiceInputsWriter }
    
    override def write(h: ComponentInputsResult): JValue = {
      JObject(       
          JField(DomainKey, toJSON(h.domain)) ::
          JField(PortKey, toJSON(h.port)) :: 
          JField(UserNameKey, toJSON(h.username)) :: 
          JField(PasswordKey, toJSON(h.password)) ::        
          JField(VersionKey, toJSON(h.version)) ::
          JField(SourceKey, toJSON(h.source)) ::
           JField(DesignInputsKey, toJSON(h.design_inputs)(DesignInputsWriter)) ::
          JField(ServiceInputsKey, toJSON(h.service_inputs)(ServiceInputsWriter)) ::
          JField(CIDKey, toJSON(h.ci_id)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[ComponentInputsResult] {

    import DesignInputsSerialization.{ reader => DesignInputsReader }
    import ServiceInputsSerialization.{ reader => ServiceInputsReader }
    
    override def read(json: JValue): Result[ComponentInputsResult] = {   
      val domainField = field[String](DomainKey)(json)    
      val portField = field[String](PortKey)(json)
      val usernameField = field[String](UserNameKey)(json)
      val passwordField = field[String](PasswordKey)(json)   
      val versionField = field[String](VersionKey)(json)
      val sourceField = field[String](SourceKey)(json)
      val designinputsField = field[DesignInputs](DesignInputsKey)(json)(DesignInputsReader)
      val serviceinputsField = field[ServiceInputs](ServiceInputsKey)(json)(ServiceInputsReader)
      val ciidField = field[String](CIDKey)(json)
      
      (domainField |@| portField |@| usernameField |@| passwordField |@| versionField |@| sourceField |@| designinputsField |@| serviceinputsField |@| ciidField) { 
        (domain: String, port: String, username: String, password: String, version: String, source: String, design_inputs: DesignInputs, service_inputs: ServiceInputs, ci_id: String) =>
          new ComponentInputsResult(domain, port, username, password, version, source, design_inputs, service_inputs, ci_id)
      }
    }
  }
}