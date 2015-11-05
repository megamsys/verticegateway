package models.utils

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
//import scala.collection.mutable.Map
import java.io.{ File, FileInputStream }
import models.{ MarketPlaceInput, MarketPlacePlans, MarketPlacePlan }

/**
 *
 * ubuntu:
 * cattype: Torpedo
 * order: 1
 * image: ubuntu.png
 * url: http://ubuntu.com
 *
 * plans:
 * 14.04:
 * description: "Ubuntu 14.04 LTS (Trusty) is the blah..blah."
 * 12.04:
 * description: "Ubuntu 12.04 LTS (Precise Pangolin) is the blah..blah."
 *
 */

object MKPData {

  implicit val formats = DefaultFormats

  //marketplaceInput and marketplacePlans are loded dynamically to mkMap
  val contentToEncode = scala.io.Source.fromFile(Constants.MEGAM_MKT_YAML).mkString
  val kMap: Map[String, String] = mapAsScalaMap[String, String](new Yaml().load(contentToEncode).asInstanceOf[java.util.Map[String, String]]).toMap
  val list = scala.collection.mutable.MutableList[String]()
  val plist = scala.collection.mutable.MutableList[String]()

  var mkMap = Map[String, MarketPlaceInput]()

  kMap.get("marketplaces") match {
    case Some(innerlink) => {
      play.api.Logger.debug(("%s").format("Entered To Inner KeyValue of YAML"))
      if (innerlink.asInstanceOf[AnyRef].getClass.getSimpleName == "LinkedHashMap") {
        val hashmapinput: Map[String, String] = mapAsScalaMap[String, String](innerlink.asInstanceOf[java.util.Map[String, String]]).toMap
        val cc = hashmapinput foreach {
          case (lkey, lvalue) =>
            val innerhashmapinput: Map[String, String] = mapAsScalaMap[String, String](lvalue.asInstanceOf[java.util.Map[String, String]]).toMap
            val plans = innerhashmapinput.get("plans").getOrElse(new java.util.LinkedHashMap[String, String]())
            val planinput: Map[String, String] = mapAsScalaMap[String, String](plans.asInstanceOf[java.util.Map[String, String]]).toMap
            var planList = new ListBuffer[MarketPlacePlan]()
            planinput.map(x => {
              val plandesc: Map[String, String] = mapAsScalaMap[String, String](x._2.asInstanceOf[java.util.Map[String, String]]).toMap
              planList += MarketPlacePlan(plandesc.get(plandesc.keySet.head).getOrElse(""), String.valueOf(x._1))
            })
            //MarketPlaceInput and MarketPlacePlans are loaded dynamically to mkMap
            mkMap += lkey -> MarketPlaceInput(lkey, innerhashmapinput.get("cattype").getOrElse(""), String.valueOf(innerhashmapinput.get("order").getOrElse("")), innerhashmapinput.get("image").getOrElse(""), innerhashmapinput.get("url").getOrElse("").trim, innerhashmapinput.get("host").getOrElse(""), innerhashmapinput.get("port").getOrElse(""), innerhashmapinput.get("username").getOrElse(""), innerhashmapinput.get("password").getOrElse(""), planList.toList)
        }
      }
    }
    case None => println("Failure")
  }
}
