package models.json.tosca

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import io.megam.auth.funnel.FunnelErrors._
import models.Constants._
import models.tosca.{ KeyValueField}

/**
 * @author rajthilak
 *
 */
class KeyValueFieldSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[KeyValueField] {

  protected val NameKey = "key"
  protected val ValueKey = "value"

  override implicit val writer = new JSONW[KeyValueField] {

    override def write(h: KeyValueField): JValue = {
      JObject(
          JField(NameKey, toJSON(h.key)) ::
          JField(ValueKey, toJSON(h.value)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[KeyValueField] {

    override def read(json: JValue): Result[KeyValueField] = {
      val nameField = field[String](NameKey)(json)
      val valueField = field[String](ValueKey)(json)

      (nameField |@| valueField ) {
          (name: String, value: String) =>
          new KeyValueField(name, value)
      }
    }
  }
}
