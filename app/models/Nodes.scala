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

import play.api._
import play.api.mvc._
import models._
import org.megam.common.riak.{ GSRiak }
import controllers.stack.MConfig
/**
 * @author ram
 *
 */

case class NodeResult(id: String, acc_id: String, request_id: String)


object Nodes {

  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")

  /*
   * put the value in riak bucket
   */
  def create(key: String, value: String): ValidationNel[Error, Option[NodeResult]] = {
    riak.store(key, value) match  {
      case Success(msg) =>     Validation.success[Error, Option[NodeResult]](None).toValidationNel
      case Failure(err) =>     Validation.failure[Error, Option[NodeResult]](new Error("Node.create: Not Implemented.")).toValidationNel

    }

  }

  /*
   * fetch the object using their key from bucket
   */
  def findById(key: String): ValidationNel[Error, Option[NodeResult]] = {
   riak.fetch(key) match {
      case Success(msg) =>   Validation.success[Error, Option[NodeResult]](None).toValidationNel 
      case Failure(err) =>   Validation.failure[Error, Option[NodeResult]](new Error("Node.findById: Not Implemented.")).toValidationNel
    }

  }
  

   /**
   * Index on email
   */
  def findByEmail(email: String): ValidationNel[Error, Option[NodeResult]] = {
    riak.fetchIndexByValue(email)  match {
      case Success(msg) =>   Validation.success[Error, Option[NodeResult]](None).toValidationNel 
      case Failure(err) =>   Validation.failure[Error, Option[NodeResult]](new Error("Node.findById: Not Implemented.")).toValidationNel
    }
  }

}

