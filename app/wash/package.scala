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
