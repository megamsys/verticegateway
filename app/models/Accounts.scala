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
package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._
import play.api._
import play.api.mvc._
import net.liftweb.json._


import models._
import org.megam.common.riak.{ GSRiak }

/**
 * @author rajthilak
 *
 */

case class AccountResult(id: String, email: String, api_key: String, authority: String)

object Accounts {
  
    implicit val formats = DefaultFormats


  private lazy val riak: GSRiak = GSRiak(MConfig.rialurl, "accounts")

  def create(input: String): ValidationNel[Error, Option[AccountResult]] = {
    riak.store(input) match {
      case Success(msg) =>     Validation.success[Error, Option[AccountResult]](None).toValidationNel
      case Failure(err) =>     Validation.failure[Error, Option[AccountResult]](new Error("Account.create Not Implemented")).toValidationNel
    }
  }

  def findById(key: String): ValidationNel[Error, Option[AccountResult]] = {
    //extract the json into ValidationNel
    riak.fetch(key) match {
      case Success(msg) =>    { Validation.success[Error, Option[AccountResult]](None).toValidationNel 
       parse(msg.value).extract[AccountResult]  
      }
      case Failure(err) =>     Validation.failure[Error, Option[AccountResult]](new Error("Account.create Not Implemented")).toValidationNel
    }
  }

  /**
   * Index on email
   */
  def findByEmail(email: String): ValidationNel[Error, Option[AccountResult]] = {
    riak.fetchIndexByValue(email) match {
      case Success(msg) =>    { Validation.success[Error, Option[AccountResult]](None).toValidationNel 
             parse(msg.value).extract[AccountResult] 
      }
      case Failure(err) =>     Validation.failure[Error, Option[AccountResult]](new Error("Account.create Not Implemented")).toValidationNel
    }
  }

}