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
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.cache._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import scala.collection.JavaConversions._
import models.cache._
import org.yaml.snakeyaml.Yaml
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.ListBuffer
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
  //  val json = "{\"key\":\"" + key + "\",\"value\":" + value + "}"
  val policykey = key
  val policyvalue = value
}

object CSARJson {

  implicit val formats = DefaultFormats

  def toJson(input: String): ValidationNel[Throwable, String] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARs", "CSAR to JSON"))
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

    val componentList = scala.collection.mutable.MutableList[scala.collection.mutable.MutableList[String]]()
    csarInput.get("node_templates") match {
      case Some(thatGS) =>
        val csarInput1: Map[String, String] = mapAsScalaMap[String, String](thatGS.asInstanceOf[java.util.Map[String, String]]).toMap
        csarInput1 foreach {
          case (key, value) =>
            componentList += createComponentArray(key, value)
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
      case None => play.api.Logger.debug("-------***************None case--------------")
    }
   
    val afterFitComponents: scala.collection.mutable.MutableList[Component] = componentInputsBuilder(inputsList, componentList)
    val afterFitAssembly: scala.collection.mutable.MutableList[Assembly] = assemblyBuilder(afterFitComponents, policylist)
    val afterFitAssemblies: String = assembliesBuilder(afterFitAssembly)    
    
    Validation.success[Throwable, String](afterFitAssemblies).toValidationNel
  }

  def createComponentArray(key: String, value: Any): scala.collection.mutable.MutableList[String] = {
    var i = 0;
    var j = 0;
    val list = scala.collection.mutable.MutableList[String]()
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARs Parse Hash", "CSAR to JSON"))
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

  def componentInputsBuilder(inputsList: scala.collection.mutable.MutableList[String], componentList: scala.collection.mutable.MutableList[scala.collection.mutable.MutableList[String]]): scala.collection.mutable.MutableList[Component] = {
    val duplicateComponentList: scala.collection.mutable.MutableList[scala.collection.mutable.MutableList[String]] = componentList
    var component_inputs_lists = new ListBuffer[KeyValueField]()
   
    val cc: scala.collection.mutable.MutableList[Component] = componentList.map {
      case (lvalue) =>              

          if (getValue("domain", lvalue) != "") {
              component_inputs_lists += KeyValueField("domain", getValue("domain", lvalue))
           }

	  if (getValue("version", lvalue) != "") {
              component_inputs_lists += KeyValueField("version", getValue("version", lvalue))
           }

	 if (getValue("source", lvalue) != "") {
              component_inputs_lists += KeyValueField("source", getValue("source", lvalue))
           }

	val valu = new Component(getValue("name", lvalue), getValue("type", lvalue),
          component_inputs_lists.toList, KeyValueList.empty, new Artifacts("", "", KeyValueList.empty),
          BindLinks.empty, OperationList.empty, "LAUNCHING")

        valu
    }
    return cc
  }

  def assemblyBuilder(componentList: scala.collection.mutable.MutableList[Component], policyList: scala.collection.mutable.MutableList[CSARPolicyOutput]): scala.collection.mutable.MutableList[Assembly] = {
    val assemblylist = scala.collection.mutable.MutableList[Assembly]()
    if (policyList.size() > 0) {
      val plist = new ListBuffer[String]()
      policyList foreach {
        case (plistvalue) =>
          val tlist = plistvalue.policyvalue.asInstanceOf[java.util.ArrayList[String]].toList.size()
          for (i <- 0 to tlist) {
            if (i < tlist) {
              plist += plistvalue.policyvalue.asInstanceOf[java.util.ArrayList[String]].toList(i)
            }
          }
      }
      policyList foreach {
        case (pvalue) =>
          val clist = new ListBuffer[Component]()
          val tlist = pvalue.policyvalue.asInstanceOf[java.util.ArrayList[String]].toList.size()
          for (i <- 0 to tlist) {
            if (i < tlist) {
              componentList foreach {
                case (component) =>
                  if (component.name == pvalue.policyvalue.asInstanceOf[java.util.ArrayList[String]].toList(i)) {
                    clist += component
                  }
              }
            }
          }
          assemblylist += new Assembly(pvalue.policykey, clist.toList, "", KeyValueList.empty, PoliciesList.empty, KeyValueList.empty, OperationList.empty, KeyValueList.empty, "LAUNCHING")
      }
      componentList foreach {
        case (cvalue) =>
          var flag = false
          for (i <- 0 to plist.size()) {
            if (i < plist.size()) {
              if (plist(i) == cvalue.name) {
                flag = true
              }
            }
          }
          if (flag != true) {
            assemblylist += new Assembly(getRandomName(), List(cvalue), "", KeyValueList.empty, PoliciesList.empty, KeyValueList.empty, OperationList.empty, KeyValueList.empty, "")
          }
      }
    } else {
      componentList foreach {
        case (cvalue) =>
          assemblylist += new Assembly(getRandomName(), List(cvalue), "", KeyValueList.empty, PoliciesList.empty, KeyValueList.empty, OperationList.empty, KeyValueList.empty, "")
      }
    }
    return assemblylist
  }

  def assembliesBuilder(assemblyList: scala.collection.mutable.MutableList[Assembly]): String = {
    var assembly_lists = new ListBuffer[Assembly]()
    for (assembly <- assemblyList) {
      assembly_lists += assembly
    }
    val cc: String = new AssembliesInput(getRandomName(), assembly_lists.toList, KeyValueList.empty).json
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


