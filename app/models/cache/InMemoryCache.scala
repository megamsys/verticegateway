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
import play.api.Play.current

/**
 * @author ram
 *
 */

/**
 * A timestamped value in the Cache. eg: ValidationNel[Throwable, PredefResult]
 */
case class Timestamped[A](value: A, timestamp: Long)

/**
 * A play.api.cache.Cache wrapper. Has two methods to
 * - update play.api.cache.Cache, (and)
 * - getAs a type from play.api.cache.Cache
 */
case class InMemoryCache[A]() {
  def update[A](u: String, s: Timestamped[A]): InMemoryCache[A] = {
    play.api.Logger.debug("%-20s -->[%s]".format("InMemoryCache:", "update"))
    play.api.Logger.debug("%-20s -->[%s]".format("InMemoryCache:", u + "=" + s))
    play.api.cache.Cache.set(u, s)
    new InMemoryCache[A]
  }
  def getAs[A](c: String): Option[Timestamped[A]] = {
    play.api.Logger.debug("%-20s -->[%s]".format("InMemoryCache:", "getAs"))
    play.api.Logger.debug("%-20s -->[%s]".format("InMemoryCache:", "getAs ?" + c))
    play.api.cache.Cache.getAs[Timestamped[A]](c)
  }
}

/**
 * An InMemory trait, which returns a StateCache
 */
trait InMemory[A] {
  def get(u: String): StateCache[A]
}

object InMemory {
  def apply[A](f: String => A) = new InMemoryImpl[A](f)
}

/**
 * An InMemory impl, which take the value type A,
 * and a function (f: String => A) which produces A.
 * The function f will be called when the value in the cache is stale or doesn't exists.
 */
class InMemoryImpl[A](f: String => A) extends InMemory[A] {

  /**
   * Implement by doing a check if it exists in the InMemoryCache (which just a fascade to play.api.cache.Cache)
   * If it doesn't then do a retrieve using the string key, but calling retrieve
   */
  def get(u: String): StateCache[A] = for {
    ofs <- checkMem(u)
    fs <- ofs.map(State.state[InMemoryCache[A], A]).getOrElse(retrieve(u))
  } yield fs

  /**
   * Verify if the string exists in the InMemoryCache
   */
  private def checkMem(u: String): StateCacheO[A] = for {
    ofs <- State.gets { c: InMemoryCache[A] =>
      play.api.Logger.debug("%-20s -->[%s]".format("|^^|-->checkMem:", u))
      c.getAs[A](u).collect {
        case Timestamped(fs, ts) if !stale(ts) => fs
      }
    }
  } yield ofs

  /**
   * Produce a stale timer
   */
  private def stale(ts: Long): Boolean = {
    System.currentTimeMillis - ts > (5 * 60 * 1000L)
  }

  /**
   * Call the function, and produce a value A, and modify the state. Upon modification,
   * return back a new InMemoryCache. In this case we return a new InMemorycache, as this just a fascade
   * to play.api.cache.Cache.
   */
  private def retrieve(u: String): StateCache[A] = for {
    fs <- State.state(f(u));
    tfs = Timestamped(fs, System.currentTimeMillis)
    _ <- State.modify[S[A]] { _.update(u, tfs) }
  } yield {
    play.api.Logger.debug("%-20s -->[%s]".format("\\_/-->retrieve:", u));
    fs
  }

}