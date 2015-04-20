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
import models.tosca.{ ProfileResult }

/**
 * @author morpheyesh
 *
 */

class ProfileResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[ProfileResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  
      protected val IdKey = "id"
      protected val AccountIdKey = "accounts_id"
      protected val FirstNameKey = "first_name"
      protected val LastNameKey = "last_name"
      //protected val PhoneKey = "phone"
      //protected val UserTypeKey = "user_type"
      protected val EmailKey = "email"
      protected val ApiTokenKey = "api_token"
      protected val PasswordKey = "password"
      protected val PasswordConfirmationKey = "password_confirmation"
      //protected val VerificationHashKey = "verification_hash"
      //protected val AppAttributesKey = "app_attributes"
      //protected val CloudIdentityAttributesKey = "cloud_identity_attributes"
      //protected val AppsItemAttributesKey = "apps_item_attributes"
      protected val PasswordResetTokenKey = "password_reset_token"
      //protected val PasswordResetSentAtKey = "password_reset_sent_at"
      protected val CreatedAtKey ="created_at"
      
      
 

  override implicit val writer = new JSONW[ProfileResult] {
   
    override def write(h: ProfileResult): JValue = {
      JObject(
           JField(IdKey, toJSON(h.id)) ::
           JField(AccountIdKey, toJSON(h.accounts_id)) ::
           JField(JSONClazKey, toJSON("Megam::Profile")) ::
           JField(FirstNameKey, toJSON(h.first_name)) ::   
           JField(LastNameKey, toJSON(h.last_name)) ::        
           //JField(PhoneKey, toJSON(h.phone)) ::        
           //JField(UserTypeKey, toJSON(h.user_type)) ::        
           JField(EmailKey, toJSON(h.email)) ::        
           JField(ApiTokenKey, toJSON(h.api_token)) ::        
           JField(PasswordKey, toJSON(h.password)) ::        
           JField(PasswordConfirmationKey, toJSON(h.password_confirmation)) ::        
           //JField(VerificationHashKey, toJSON(h.verification_hash)) ::        
           //JField(AppAttributesKey, toJSON(h.app_attributes)) ::        
           //JField(CloudIdentityAttributesKey, toJSON(h.cloud_identity_attributes)) ::        
           //JField(AppsItemAttributesKey, toJSON(h.apps_item_attributes)) ::        
           JField(PasswordResetTokenKey, toJSON(h.password_reset_token)) ::        
           //JField(PasswordResetSentAtKey, toJSON(h.password_reset_sent_at)) ::        
           JField(CreatedAtKey, toJSON(h.created_at))   ::          
          Nil)
    }
  }

  override implicit val reader = new JSONR[ProfileResult] {
   
    
    

    override def read(json: JValue): Result[ProfileResult] = {
      
       val idField = field[String](IdKey)(json)
       val accountIdField = field[String](AccountIdKey)(json)
       val firstNameField = field[String](FirstNameKey)(json)
       val lastNameField = field[String](LastNameKey)(json)
       //val phoneField = field[String](PhoneKey)(json)
       //val usertypeField = field[String](UserTypeKey)(json)
       val emailField = field[String](EmailKey)(json)
       val apitokenField = field[String](ApiTokenKey)(json)
       val passwordField = field[String](PasswordKey)(json)
       val passwordConfirmationField = field[String](PasswordConfirmationKey)(json)
       //val verificationHashField = field[String](VerificationHashKey)(json)
       //val appAttributesField = field[String](AppAttributesKey)(json)
       //val cloudIdentityAttributesField = field[String](CloudIdentityAttributesKey)(json)
       //val appItemAttributesField = field[String](AppsItemAttributesKey)(json)
       val passwordResetTokenField = field[String](PasswordResetTokenKey)(json)
       //val passwordResetSentAtField = field[String](PasswordResetSentAtKey)(json)
       val createdAtField = field[String](CreatedAtKey)(json)
      
       (idField |@| accountIdField |@| firstNameField |@| lastNameField |@| emailField |@| apitokenField |@| passwordField |@| passwordConfirmationField  |@| passwordResetTokenField |@| createdAtField ) {
       (id: String, accounts_id: String, first_name: String, last_name: String, email: String, api_token: String, password: String, password_confirmation: String, password_reset_token: String, created_at: String) =>
         new ProfileResult(id, accounts_id, first_name, last_name, email, api_token, password, password_confirmation, password_reset_token, created_at) 
      }
    }
  }
}