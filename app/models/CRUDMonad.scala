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
package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._


//for our 1.0 rewrite of gwy.
trait CRUDMonad[I,O] {

  /**
   * performs a Store
   * @param input the input body against which to perform the request
   * @return the {{{O}}} that can execute the request with the given parameters
   */
  def map(input: I): O

  /**
   * performs a Fetch by email
   * @param input the input email against which to perform the request
   * @return the {{{O}}} that can execute the request with the given parameters
   */
  def flatMap(input: I): O


}
