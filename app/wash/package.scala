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

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._


package object wash {

implicit def PQdResultsSemigroup: Semigroup[PQdResults] = Semigroup.instance((f1, f2) => f1.append(f2))

type PQdResults = NonEmptyList[Option[PQd]]

object PQdResults {
  val emptyNR = List(Option.empty[PQd])

  def apply(m: Option[PQd]) = nels(m)
  def apply(m: PQd): PQdResults = PQdResults(m.some)
  def empty: PQdResults = nel(emptyNR.head, emptyNR.tail)
}


}
