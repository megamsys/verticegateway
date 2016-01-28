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

package models.analytics

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.tosca._
import models.json.tosca.box._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import app.MConfig
import models.base._

import org.megam.util.Time
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.GunnySack
import scala.collection.mutable.{ LinkedHashMap, ListBuffer }

import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import com.stackmob.newman._
import com.stackmob.newman.response._
import com.stackmob.newman.dsl._
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL

import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import controllers.stack._
import spark._
/**
 * @author ranjitha
 *
 */

 case class YonpiConnector(source: String, credential: String, tables: String, dbname: String, endpoint: String, port: String) {
    val json = "{\"source\":\"" + source + "\",\"credential\":\"" + credential + "\", \"tables\":\"" + tables + "\",\"dbname\":\"" + dbname + "\", \"endpoint\":\"" + endpoint + "\",\"port\":\"" + port + "\"}"
    def toJValue: JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      val preser = new models.json.analytics.YonpiConnectorsSerialization()
      toJSON(this)(preser.writer)
    }

    def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue)
    } else {
      compactRender(toJValue)
    }
  }

  object YonpiConnector {
     def empty: YonpiConnector = new YonpiConnector(new String(), new String(), new String(), new String(), new String(), new String())


       def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[YonpiConnector] = {
         import net.liftweb.json.scalaz.JsonScalaz.fromJSON
         val preser = new models.json.analytics.YonpiConnectorsSerialization()
         fromJSON(jValue)(preser.reader)
       }

       def fromJson(json: String): Result[YonpiConnector] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
         parse(json)
       } leftMap { t: Throwable =>
         UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
       }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

   }


 case class Yonpiinput(query: String, connectors: YonpiConnectorsList) {
   val json = "{\"query\":\"" + query + "\",\"connectors\":" + YonpiConnectorsList.toJson(connectors, true) + "}"
   def toJValue: JValue = {
     import net.liftweb.json.scalaz.JsonScalaz.toJSON
     val preser = new models.json.analytics.YonpiinputSerialization()
     toJSON(this)(preser.writer)
   }

   def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
     prettyRender(toJValue)
   } else {
     compactRender(toJValue)
   }
 }
 object Yonpiinput {
   def empty: Yonpiinput = new Yonpiinput(new String(), YonpiConnectorsList.empty)

}


case class YonpiinputResult(id: String, query: String, connectors: YonpiConnectorsList, created_at: String) {
  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.analytics.YonpiinputResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object YonpiinputResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[YonpiinputResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
        import models.json.analytics.YonpiinputResultSerialization
    val preser = new YonpiinputResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[YonpiinputResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}
case class Connectors(source: String, endpoint: String, port: String, dbname: String, inputs: models.tosca.KeyValueList, tables: TablesList) {
  val json = "{\"source\":\"" + source + "\",\"endpoint\":\"" + endpoint + "\",\"port\":\"" + port + "\",\"dbname\":\"" + dbname + "\",\"inputs\":" + models.tosca.KeyValueList.toJson(inputs, true) + ",\"tables\":" + TablesList.toJson(tables, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.analytics.ConnectorsSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object Connectors {
  def empty: Connectors = new Connectors(new String(), new String(), new String(), new String(), models.tosca.KeyValueList.empty, TablesList.empty)


    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Connectors] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      val preser = new models.json.analytics.ConnectorsSerialization()
      fromJSON(jValue)(preser.reader)
    }

    def fromJson(json: String): Result[Connectors] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
      parse(json)
    } leftMap { t: Throwable =>
      UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
    }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}



case class Tables(name: String, table_id: String, schemas: models.tosca.KeyValueList, links: models.tosca.KeyValueList) {
  val json = "{\"name\":\"" + name + "\",\"table_id\":\"" + table_id + "\",\"schemas\":" + models.tosca.KeyValueList.toJson(schemas, true) + ",\"links\":" + models.tosca.KeyValueList.toJson(links, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.analytics.TablesSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object Tables {
  def empty: Tables = new Tables(new String(), new String(), models.tosca.KeyValueList.empty, models.tosca.KeyValueList.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Tables] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new models.json.analytics.TablesSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Tables] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class WorkbenchesResult(id: String, name: String, connectors: ConnectorsList, created_at: String) {
  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.analytics.WorkbenchesResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object WorkbenchesResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[WorkbenchesResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
        import models.json.analytics.WorkbenchesResultSerialization
    val preser = new WorkbenchesResultSerialization()
    fromJSON(jValue)(preser.reader)

  }

  def fromJson(json: String): Result[WorkbenchesResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class WorkbenchesInput(name: String, connectors: ConnectorsList) {
  val json = "{\"name\":\"" + name + "\",\"connectors\":" + ConnectorsList.toJson(connectors, true) + "}"
}
case class ExecuteInput(name: String, query: String) {
  val json = "{\"name\":\"" + name + "\",\"query\":\"" + query + "\"}"

}

case class Workbenches(name: String, connectors: ConnectorsList) {
  val json = "{\"name\":\"" + name + "\",\"connectors\":" + ConnectorsList.toJson(connectors, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.analytics.WorkbenchesSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object Workbenches {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("workbenches")

  val metadataKey = "Workbenches"
  val metadataVal = "Workbenches Creation"
  val bindex = "workbenches"

  def empty: Workbenches = new Workbenches(new String(), ConnectorsList.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Workbenches] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new models.json.analytics.WorkbenchesSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Workbenches] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }


  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val workbenchesInput: ValidationNel[Throwable, WorkbenchesInput] = (Validation.fromTryCatchThrowable[WorkbenchesInput, Throwable] {
      parse(input).extract[WorkbenchesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      event <- workbenchesInput
      aor <- (models.base.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "wob").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id)
      //val bvalue = Set(event.a_id)
      val json = new WorkbenchesResult(uir.get._1 + uir.get._2, event.name, event.connectors, Time.now.toString).toJson(false)
      new GunnySack(event.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }
  def create(email: String, input: String): ValidationNel[Throwable, Option[WorkbenchesResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[WorkbenchesResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"Workbenches created. success", Console.RESET))
              (parse(gs.get.value).extract[WorkbenchesResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

def execute(email: String, input: String): ValidationNel[Throwable, Option[SparkjobsResult]] = {

  val executeInput: ValidationNel[Throwable, ExecuteInput] = (Validation.fromTryCatchThrowable[ExecuteInput, Throwable] {
    parse(input).extract[ExecuteInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  play.api.Logger.debug("%-20s -->[%s]".format("IN",  "+++++++++++++++++"))
  play.api.Logger.debug("%-20s -->[%s]".format("INPUT",  executeInput))
  for {
    yi <- executeInput
    aor <- (models.analytics.Workbenches.findByName(List(yi.name).some) leftMap { t: NonEmptyList[Throwable] => t })
    su <- spark.SparkSubmitter.empty.wbsubmit(false, email,  new YonpiInputBuilder(yi.query, aor).toMap)

  } yield {

    su flatMap {  so =>
      new SparkjobsResult("", so._2.code, so._2.status, so._2.result.job_id, Time.now.toString).some
     //new Yonpiinput(so._2.query, so._2.connectors).some
  }
}
}

 def findByName(workbenchesList: Option[List[String]]): ValidationNel[Throwable, WorkbenchesResults] = {
   (workbenchesList map {
     _.map { workbenchesName =>
       play.api.Logger.debug("models.WorkbenchesName findByName: Workbenches:" + workbenchesName)
       (riak.fetch(workbenchesName) leftMap { t: NonEmptyList[Throwable] =>
         new ServiceUnavailableError(workbenchesName, (t.list.map(m => m.getMessage)).mkString("\n"))
       }).toValidationNel.flatMap { xso: Option[GunnySack] =>
         xso match {
           case Some(xs) => {
             (Validation.fromTryCatchThrowable[models.analytics.WorkbenchesResult,Throwable] {
               parse(xs.value).extract[WorkbenchesResult]
             } leftMap { t: Throwable =>
               new ResourceItemNotFound(workbenchesName, t.getMessage)
             }).toValidationNel.flatMap { j: WorkbenchesResult =>
               Validation.success[Throwable, WorkbenchesResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
             }
           }
           case None => Validation.failure[Throwable, WorkbenchesResults](new ResourceItemNotFound(workbenchesName, "")).toValidationNel
         }
       }
     } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
   } map {
     _.foldRight((WorkbenchesResults.empty).successNel[Throwable])(_ +++ _)
   }).head //return the folded element in the head.
 }

  def findById(workbenchesID: Option[List[String]]): ValidationNel[Throwable, WorkbenchesResults] = {
    (workbenchesID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Workbenches ID", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[WorkbenchesResult, Throwable] {
                parse(xs.value).extract[WorkbenchesResult]
              } leftMap { t: Throwable => new MalformedBodyError(xs.value, t.getMessage) }).toValidationNel.flatMap { j: WorkbenchesResult =>
                play.api.Logger.debug(("%-20s -->[%s]").format("Workbenches result", j))
                Validation.success[Throwable, WorkbenchesResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => {
              Validation.failure[Throwable, WorkbenchesResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((WorkbenchesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }
  def findByEmail(email: String): ValidationNel[Throwable, WorkbenchesResults] = {
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, WorkbenchesResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the workbenches before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        play.api.Logger.debug(("%-20s -->[%s]").format("analytics.Workbenches", "findByEmail" + aor.get.id))
        new GunnySack("workbenches", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findById(nm.some) else
          new ResourceItemNotFound(email, "Workbenches = nothing found for the user.").failureNel[WorkbenchesResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "Workbenches = nothing found for the users.").failureNel[WorkbenchesResults])
  }

}
