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
package controllers

import scalaz._
import scalaz.NonEmptyList
import Scalaz._

/*
 * @author ram
 *
 */
package object stack {

  val DEFAULT_MSG = ""

  type RawResult = (Int, Option[Map[String, String]])

  object RawResult {
    def apply(Id: Int, results: Map[String, String]) = (Id, results.some)
  }

  type ResultInError = Option[(Int, String)]
  //  type ResultInErrorList = NonEmptyList[(Int, Option[String])]

  object ResultInError {

    def apply[C](m: (Int, String)): ResultInError = m.some

  }

}



