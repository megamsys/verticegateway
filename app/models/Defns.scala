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
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import Scalaz._
import org.megam.common.enumeration._
import controllers.funnel.FunnelErrors._

/**
 * @author ram
 *
 */
object Defns {

  sealed abstract class DefnType(override val stringVal: String) extends Enumeration

  object DefnType {
    object APP extends DefnType("APP")
    object BOLT extends DefnType("BOLT")
    object WRENCH extends DefnType("WRENCH")
    object NONE extends DefnType("NONE")

    implicit val DefnTypToReader = upperEnumReader(APP, BOLT, WRENCH,NONE)
  }
  
  def defnType(s: String):DefnType = {
    s match {
      case "APP" => return DefnType.APP
      case "BOLT" => return DefnType.BOLT
      case "WRENCH" => return DefnType.WRENCH
      case _ => return DefnType.NONE
    }
  }

  def create(email: String, node_id: String, nir: NodeInput): ValidationNel[Throwable, Tuple2[String, String]] = {
    val inpdefn_type: DefnType = defnType(nir.node_type.trim.toUpperCase) 
    inpdefn_type match {
      case DefnType.APP => //return app defn        
        for {
          adef <- (AppDefns.createforNewNode("{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + nir.node_name + "\"," + nir.appdefjson + "}") leftMap { t: NonEmptyList[Throwable] => t })
        } yield {
          val adf = adef.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "App Definitions create failed (or) not found. Retry again."))
          Tuple2(adf._1, nir.node_type)
        }
      case DefnType.BOLT => //return bolt defn
        for {
          bdef <- (BoltDefns.createforNewNode("{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + nir.node_name + "\"," + nir.boltdefjson + "}") leftMap { t: NonEmptyList[Throwable] => t })
        } yield {
          val bdf = bdef.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "Bolt Definitions create failed (or) not found. Retry again."))
          Tuple2(bdf._1, nir.node_type)
        }
      case _ => Validation.failure[Throwable,Tuple2[String,String]](new ResourceItemNotFound(inpdefn_type.stringVal, "supported [APP, BOLT]")).toValidationNel    
    }
  }

} 

