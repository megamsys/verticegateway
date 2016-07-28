package models.json.tosca.carton

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import scala.collection.mutable.ListBuffer
import models.tosca._
import models.json.tosca._
import java.nio.charset.Charset
/**
 * @author rajthilak
 *
 */
object MembersListSerialization extends io.megam.json.SerializationBase[MembersList] {
  implicit val formats = DefaultFormats

  implicit override val writer = new JSONW[MembersList] {
    override def write(h: MembersList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: String => toJSON(nrOpt)
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[MembersList] {
    override def read(json: JValue): Result[MembersList] = {
      var list = new ListBuffer[String]()
      json match {
        case JArray(jObjectList) => {
         jObjectList.foreach { jValue: JValue =>
            list += jValue.extract[String]
          }.some

          val nrs: MembersList = list.toList
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[MembersList]
      }
    }
  }
}
