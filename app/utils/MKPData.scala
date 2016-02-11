package utils

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import org.yaml.snakeyaml.Yaml
import controllers.Constants
import scala.collection.JavaConversions._
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.ListBuffer
import java.nio.charset.Charset
import scala.util.control._
import java.io.{ File, FileInputStream }
//import models.base.{ MarketPlaceInput, MarketPlacePlans, MarketPlacePlan }
import models.tosca.{ KeyValueList, KeyValueField }

/**
 *
 * java:
 * cattype: APP
 * order: 1
 * image: java.png
 * url: http://apache.tomcat.org
 * envs:
 * plans:
 * 8.x:
 * description: "Tomcat that run java apps faster"
 * 7.x:
 * description: "Tomcat legacy that sleeps faster."
 *
 */

object MKPData {

  implicit val formats = DefaultFormats
//  var mkMap = Map[String, MarketPlaceInput]()
var mkMap = ""
}
