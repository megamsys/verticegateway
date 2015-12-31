/*
** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.json.tosca._
import models.json.tosca.carton._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import app.MConfig
import models.base._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.GunnySack
import org.megam.util.Time
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import scala.collection.JavaConversions._
import org.yaml.snakeyaml.Yaml
import scala.collection.mutable.{ LinkedHashMap, ListBuffer }
import java.util.ArrayList
import scala.util.control.Breaks._

/**
 * @author rajthilak
 *
 */

case class CSARListOutput(key: String, value: String) {
  val json = "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}"
  val outputkey = key
  val outputvalue = value
}

case class CSARPolicyOutput(key: String, value: Any) {
  val policykey = key
  val policyvalue = value
}

object CSARJson {

  implicit val formats = DefaultFormats

  def toJson(input: String): ValidationNel[Throwable, String] = {
    val csarInput: Map[String, String] = mapAsScalaMap[String, String](new Yaml().load(input).asInstanceOf[java.util.Map[String, String]]).toMap

    var a = "{\"tosca_definitions_version\":\"tosca_simple_yaml_1_0\",\"description\":\"Template for deploying a two-tier application servers on two\"}";

    val inputsList = scala.collection.mutable.MutableList[String]()
    csarInput.get("inputs") match {
      case Some(thatGS) =>
        if (thatGS.asInstanceOf[AnyRef].getClass.getSimpleName == "LinkedHashMap") {
          val linkinput: Map[String, String] = mapAsScalaMap[String, String](thatGS.asInstanceOf[java.util.Map[String, String]]).toMap
          val cc = linkinput foreach {
            case (lkey, lvalue) =>
              inputsList += "{\"key\":\"" + lkey + "\", \"value\":\"" + lvalue + "\"}"
          }
        }
      case None => ""
    }

    val templateList = scala.collection.mutable.MutableList[scala.collection.mutable.MutableList[String]]()
    csarInput.get("node_templates") match {
      case Some(thatGS) =>
        val csarInput1: Map[String, String] = mapAsScalaMap[String, String](thatGS.asInstanceOf[java.util.Map[String, String]]).toMap
        csarInput1 foreach {
          case (key, value) =>
            templateList += createTemplateArray(key, value)
        }
      case None => ""
    }

    val policylist = scala.collection.mutable.MutableList[CSARPolicyOutput]()
    csarInput.get("group") match {
      case Some(thatGS) =>
        val csarInput1: Map[String, String] = mapAsScalaMap[String, String](thatGS.asInstanceOf[java.util.Map[String, String]]).toMap
        csarInput1 foreach {
          case (key, value) =>
            val csarInput2: Map[String, String] = mapAsScalaMap[String, String](value.asInstanceOf[java.util.Map[String, String]]).toMap
            csarInput2 foreach {
              case (key2, value2) =>
                if (key2 == "policies") {
                  val tlist = value2.asInstanceOf[java.util.ArrayList[String]].toList.size()
                  for (i <- 0 to tlist) {
                    if (i < tlist) {
                      val data = value2.asInstanceOf[java.util.ArrayList[Any]].toList(i)
                      val policydata: Map[String, String] = mapAsScalaMap[String, String](data.asInstanceOf[java.util.Map[String, String]]).toMap
                      policydata foreach {
                        case (datakey, datavalue) =>
                          if (datakey == "my_placement_policy") {
                            csarInput2 foreach {
                              case (memkey, memvalue) =>
                                if (memkey == "members") {
                                  policylist += new CSARPolicyOutput(key, memvalue)
                                }
                            }
                          }
                      }
                    }
                  }
                }
            }
        }
      case None => play.api.Logger.debug("cant parse the yaml")
    }

    val afterFitAssembly: scala.collection.mutable.MutableList[Assembly] = assemblyBuilder(inputsList, policylist, templateList)
    val afterFitAssemblies: String = assembliesBuilder(afterFitAssembly)

    Validation.success[Throwable, String](afterFitAssemblies).toValidationNel
  }

  def createTemplateArray(key: String, value: Any): scala.collection.mutable.MutableList[String] = {
    var i = 0;
    var j = 0;
    val list = scala.collection.mutable.MutableList[String]()
    list += "{\"key\":\"name\", \"value\":\"" + key + "\"}"
    val csarInput1: Map[String, String] = mapAsScalaMap[String, String](value.asInstanceOf[java.util.Map[String, String]]).toMap

    val cc = csarInput1 foreach {
      case (cckey, ccvalue) =>
        if (ccvalue != null) {
          if (ccvalue.asInstanceOf[AnyRef].getClass.getSimpleName == "String") {
            list += "{\"key\":\"" + cckey + "\", \"value\":\"" + ccvalue + "\"}"
          }
          if (ccvalue.asInstanceOf[AnyRef].getClass.getSimpleName == "ArrayList") {
            val tlist = ccvalue.asInstanceOf[java.util.ArrayList[String]].toList.size()
            val resultList = scala.collection.mutable.MutableList[String]()
            for (i <- 0 to tlist) {
              if (i < tlist) {
                val rlist: scala.collection.mutable.MutableList[String] = parseLinkedHashMap(cckey, ccvalue.asInstanceOf[java.util.ArrayList[String]].toList(i), resultList)
                for (j <- 0 to rlist.size()) {
                  if (j < rlist.size()) {
                    list += rlist(j)
                  }
                }
              }
            }
          }
          if (ccvalue.asInstanceOf[AnyRef].getClass.getSimpleName == "LinkedHashMap") {
            val linkinput: Map[String, String] = mapAsScalaMap[String, String](ccvalue.asInstanceOf[java.util.Map[String, String]]).toMap
            val cc = linkinput foreach {
              case (lkey, lvalue) =>
                list += "{\"key\":\"" + lkey + "\", \"value\":\"" + lvalue + "\"}"
            }
          }
        }
    }
    list
  }

  def assemblyBuilder(inputsList: scala.collection.mutable.MutableList[String], policyList: scala.collection.mutable.MutableList[CSARPolicyOutput], templateList: scala.collection.mutable.MutableList[scala.collection.mutable.MutableList[String]]): scala.collection.mutable.MutableList[Assembly] = {
    val assemblylist = scala.collection.mutable.MutableList[Assembly]()
    var assembly_inputs_lists = new ListBuffer[KeyValueField]()
    val cc: scala.collection.mutable.MutableList[Assembly] = templateList.map {
      case (template) =>
        val clist = new ListBuffer[Component]()
        if (getValue("type", template).split('.')(1) != "torpedo") {
          clist += componentbuilder(inputsList, template)
        }

        if (getValue("domain", template) != "") {
          assembly_inputs_lists += KeyValueField("domain", getValue("domain", template))
        }

        if (getValue("sshkey", template) != "") {
          assembly_inputs_lists += KeyValueField("sshkey", getValue("sshkey", template))
        }

        new Assembly(getValue("name", template), clist.toList, get_tosca_type(getValue("type", template), "ASSEMBLY"), List[Policy](), assembly_inputs_lists.toList, List[KeyValueField](), "LAUNCHING")
    }
    return cc
  }

  def componentbuilder(inputsList: scala.collection.mutable.MutableList[String], template: scala.collection.mutable.MutableList[String]): Component = {
    var component_inputs_lists = new ListBuffer[KeyValueField]()
    val fruits: List[String] = List("domain", "version", "source", "dbusername", "dbuserpassword", "dbname", "dbpassword")

    for (name <- fruits) {
      if (getValue(name, template) != "") {
        component_inputs_lists += KeyValueField(name, getValue(name, template))
      }
    }

    val valu = new Component(getValue("name", template), getValue("type", template),
      component_inputs_lists.toList, List[KeyValueField](), List[KeyValueField](), new Artifacts("", "", List[KeyValueField]()),
      List[String](), List[Operation](), new Repo("", "", "", ""), "LAUNCHING")
    return valu

  }

  def assembliesBuilder(assemblyList: scala.collection.mutable.MutableList[Assembly]): String = {
    var assembly_lists = new ListBuffer[Assembly]()
    for (assembly <- assemblyList) {
      assembly_lists += assembly
    }
    val cc: String = new AssembliesInput(getRandomName(), "", assembly_lists.toList, List[KeyValueField]()).json
    return cc

  }

  def parseLinkedHashMap(checkKey: String, mapvalue: Any, resultList: scala.collection.mutable.MutableList[String]): scala.collection.mutable.MutableList[String] = {
    val csarInput1: Map[String, String] = mapAsScalaMap[String, String](mapvalue.asInstanceOf[java.util.Map[String, String]]).toMap
    csarInput1.map {
      case (key, value) =>
        if (value.asInstanceOf[AnyRef].getClass.getSimpleName == "String") {
          resultList += "{\"key\":\"" + key + "\", \"value\":\"" + value + "\"}"
        }
        if (value.asInstanceOf[AnyRef].getClass.getSimpleName == "LinkedHashMap") {
          parseLinkedHashMap(key, value, resultList)
        }
    }
    return resultList
  }

  def get_tosca_type(key: String, camptype: String): String = {
    val split_key: Array[String] = key.split('.')
    camptype match {
      case "ASSEMBLY" => {
        if (split_key(1) != "torpedo") {
          return "tosca.torpedo.ubuntu"
        } else {
          return key
        }
      }
      case "COMPONENT" => return key
    }
  }

  def getValue(key: String, value: scala.collection.mutable.MutableList[String]): String = {
    var str = ""
    value foreach {
      case (lvalue) =>
        val parsedValue = parse(lvalue).extract[CSARListOutput]
        if (key == parsedValue.outputkey) {
          str = parsedValue.outputvalue
        }
    }
    str
  }

  def getRandomName(): String = {
    val random = new scala.util.Random
    val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
    val s = Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(10).mkString
    return s
  }

}
