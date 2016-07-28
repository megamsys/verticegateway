package cache

/**
 * @author ram
 *
 */
trait Sedimenter[A] {
  def sediment(maybeASed: A) : Boolean
}
