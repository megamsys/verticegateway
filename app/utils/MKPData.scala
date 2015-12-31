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
import models.base.{ MarketPlaceInput, MarketPlacePlans, MarketPlacePlan  }
import models.tosca.{KeyValueList, KeyValueField}

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

//marketplaceInput and marketplacePlans are loded dynamically to mkMap
val contentToEncode = scala.io.Source.fromFile(Constants.OJA_MKT_YAML).mkString
val kMap: Map[String, String] = mapAsScalaMap[String, String](new Yaml().load(contentToEncode).asInstanceOf[java.util.Map[String, String]]).toMap
val list = scala.collection.mutable.MutableList[String]()
val plist = scala.collection.mutable.MutableList[String]()

  var mkMap = Map[String, MarketPlaceInput]()

  kMap.get("marketplaces") match {
    case Some(innerlink) => {
      play.api.Logger.debug(("%s %s").format("YAML ", Constants.OJA_MKT_YAML))
      if (innerlink.asInstanceOf[AnyRef].getClass.getSimpleName == "LinkedHashMap") {
        val hashmapinput: Map[String, String] = mapAsScalaMap[String, String](innerlink.asInstanceOf[java.util.Map[String, String]]).toMap
        val cc = hashmapinput foreach {
          case (lkey, lvalue) =>
            val innerhashmapinput: Map[String, String] = mapAsScalaMap[String, String](lvalue.asInstanceOf[java.util.Map[String, String]]).toMap
            val plans = innerhashmapinput.get("plans").getOrElse(new java.util.LinkedHashMap[String,String]())
            val planInput: Map[String, String] = mapAsScalaMap[String, String](plans.asInstanceOf[java.util.Map[String, String]]).toMap
            var planList: List[MarketPlacePlan] = planInput.map(x => {
              val plandesc: Map[String, String] = mapAsScalaMap[String, String](x._2.asInstanceOf[java.util.Map[String, String]]).toMap
               MarketPlacePlan(plandesc.get(plandesc.keySet.head).getOrElse(""), String.valueOf(x._1))
            }).toList
            val envs = innerhashmapinput.get("envs").getOrElse(new java.util.LinkedHashMap[String,String]())
            val envInput: Map[String, String] = mapAsScalaMap[String, String](envs.asInstanceOf[java.util.Map[String, String]]).toMap
            var envList: List[KeyValueField] = (envInput.map { x => if (String.valueOf(x._2) == "null")  KeyValueField(String.valueOf(x._1),"") else KeyValueField(String.valueOf(x._1), String.valueOf(x._2))}).toList
            mkMap += lkey -> MarketPlaceInput(lkey, innerhashmapinput.get("cattype").getOrElse(""), String.valueOf(innerhashmapinput.get("order").getOrElse("")), innerhashmapinput.get("image").getOrElse(""), innerhashmapinput.get("url").getOrElse("").trim, envList.toList , planList.toList)
        }
      }
    }
    case None => println("Failed to parse the " + Constants.OJA_MKT_YAML)
  }
}
