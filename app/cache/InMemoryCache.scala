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
package cache

import scalaz.State
import cache._
import play.api.Play.current
import play.api.cache._
import models.base.Accounts._

/**
 * @author ram (don't ask me, i don't remember - nada!)
 *
 */

/**
 * A timestamped value in the Cache. eg: ValidationNel[Throwable, AccountsResult]
 */
case class Timestamped[A](value: A, timestamp: Long)

/**
 * A play.api.cache.Cache wrapper. Has two methods to
 * - update play.api.cache.Cache, (and)
 * - getAs a type from play.api.cache.Cache
 */
case class InMemoryCache[A]() {
  def update(u: String, s: Timestamped[A]): InMemoryCache[A] = {
    play.api.cache.Cache.set(u, s)
    new InMemoryCache[A]
  }
  def getAs(c: String): Option[Timestamped[A]] = {
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
  def apply[A](f: String => A)(implicit sedv: Sedimenter[A]) = new InMemoryImpl[A](f)(sedv)
}

/**
 * An InMemory impl, which take the value type A,
 * and a function (f: String => A) which produces A.
 * The function f will be called when the value in the cache is stale or doesn't exists.
 */
class InMemoryImpl[A](f: String => A)(implicit sedv: Sedimenter[A]) extends InMemory[A] {

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
   * A prestore check is done to see if the result fetched by the model
   * was successful. This helps to keep the Cache with good objects.
   */
  private def checkMem(u: String)(implicit sed: Sedimenter[A]): StateCacheO[A] = for {
    ofs <- State.gets { c: InMemoryCache[A] =>
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->checkMem:", u))
      c.getAs(u).collect {
        case Timestamped(fs, ts) if (sed.sediment(fs) && !stale(ts)) => fs
      }
    }
  } yield ofs

  /**
   * Produce a stale timer
   * Mask it for development (2s), which means invalidate it every 2s.
   * probably control it using  a key
   */
  private def stale(ts: Long): Boolean = {
    val sweetPieTime = if (play.api.Play.application(play.api.Play.current).mode == play.api.Mode.Dev) (5 * 60 * 100L) else (5 * 60 * 1000L)
    System.currentTimeMillis - ts > sweetPieTime
  }

  /**
   * Call the function, and produce a value A, and modify the state. Upon modification,
   * return back a new InMemoryCache. In this case we return a new InMemorycache, as this just a fascade
   * to play.api.cache.Cache.
   */
  private def retrieve(u: String): StateCache[A] = for {
    fs <- State.state(f(u))
    tfs = Timestamped(fs, System.currentTimeMillis)
    _ <- State.modify[S[A]] { _.update(u, tfs) }
  } yield {
    play.api.Logger.debug("%-20s -->[%s]".format("\\_/-->retrieve:", u));
    fs
  }

}
