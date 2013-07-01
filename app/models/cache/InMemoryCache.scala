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
package models.cache

import scalaz.State
import models.cache._

/**
 * @author ram
 *
 */

case class Timestamped[A](value: A, timestamp: Long)

object InMemoryCache {

  def get(u: String): StateCache[SinglePredef] = for {
    ofs <- checkMem(u)
    fs <- ofs.map(State.state[InMemory[SinglePredef], SinglePredef]).getOrElse(retrieve(u))
  } yield fs  

  private def checkMem(u: String): StateCache[Option[SinglePredef]] = for {
    ofs <- State.gets { c: InMemory[SinglePredef] =>
      c.getAs[SinglePredef](u).collect {
        case Timestamped(fs, ts) if !stale(ts) => fs
      }
    }
  } yield ofs

  private def stale(ts: Long): Boolean = {
    System.currentTimeMillis - ts > (5 * 60 * 1000L)
  }

  private def retrieve(u: String): StateCache[SinglePredef] = for {
    fs <- State.state(callRiak(u))
    tfs = Timestamped(fs, System.currentTimeMillis)
    _ <- State.modify[InMemory[SinglePredef]] { _.update(u, tfs) }
  } yield fs

  private def callRiak(u: String): SinglePredef = SinglePredef(u)
}

