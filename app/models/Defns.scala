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

import org.megam.common.enumeration._

/**
 * @author ram
 *
 */
object Defns {

  sealed abstract class DefnType(override val stringVal: String) extends Enumeration

  object DefnType {
    object APP_DEFN extends DefnType("APP")
    object BOLT_DEFN extends DefnType("BOLT")
    object WRENCH_DEFN extends DefnType("WRENCH")

    implicit val DefnTypToReader = upperEnumReader(APP_DEFN, BOLT_DEFN, WRENCH_DEFN)
  }

  def create(node_defn_type: DefnType) = {
    node_defn_type match {
      case DefnType.APP_DEFN  => //return app defn
      case DefnType.BOLT_DEFN => //return bolt defn
      case _    => //return a VNEL(invalid node_type    
    }
  }

}