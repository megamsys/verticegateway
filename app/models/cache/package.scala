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

import scalaz.State
import play.api.cache.CacheAPI

/**
 * @author ram
 *
 */
package object cache {

  type StateCache[+A] = State[InMemory[SinglePredef], A]

  case class SinglePredef(value: String)

  case class GroupedPredefs(stream: Stream[String])
  
  trait InMemory[A] extends Cache {
    def mem(u: String): StateCache[A]
  }

  trait Cache {
    def getAs[T](c: String): Option[Timestamped[T]]
    def update[T](u: String, s: Timestamped[T]): InMemory[T]
  }

  
}