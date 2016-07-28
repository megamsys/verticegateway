package models.json.tosca

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.tosca._
import java.nio.charset.Charset
/**
 * @author rajthilak
 *
 */
object KeyValueListSerialization extends io.megam.json.SerializationBase[KeyValueList] {

  implicit override val writer = new JSONW[KeyValueList] {
    override def write(h: KeyValueList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: KeyValueField => nrOpt.toJValue
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[KeyValueList] {
    override def read(json: JValue): Result[KeyValueList] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            KeyValueField.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[KeyValueField]()
            }
          }.some

          val nrs: KeyValueList = KeyValueList(list.getOrElse(KeyValueList.empty))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[KeyValueList]
      }
    }
  }
}
