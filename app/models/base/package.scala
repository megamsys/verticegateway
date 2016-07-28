package models


/**
 * @author rajthilak
 *
 */
package object base {
  type MarketPlaceResults = List[Option[MarketPlaceSack]]

  object MarketPlaceResults {
    val emptyPC = List(Option.empty[MarketPlaceSack])
    //def apply(m: MarketPlaceSack): MarketPlaceResults = List(m.some)
    def empty: MarketPlaceResults = List()
  }
  type SshKeysResults = List[Option[SshKeysResult]]

  object SshKeysResults {
    val emptyNR = List(Option.empty[SshKeysResult])
    def apply(m: Option[SshKeysResult]) = List(m)
    def empty: SshKeysResults = List() //nel(emptyNR.head, emptyNR.tail)
  }

}
