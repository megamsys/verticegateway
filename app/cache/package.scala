package cache

import scalaz.State
import InMemory._
/**
 * @author ram
 *
 */
package object cache {
  type S[A] = InMemoryCache[A]
  type StateCache[A] = State[S[A], A]
  type StateCacheO[A] = State[S[A], Option[A]]
}
