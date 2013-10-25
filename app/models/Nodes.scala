/* 
** Copyright [2012-2013] [Megam Systems]
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
package models

import scalaz._
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import Scalaz._
import com.twitter.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import org.megam.common.enumeration._
import models._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import java.nio.charset.Charset

/**
 * @author ram
 *
 */
case class NodeInput(node_name: String, node_type: String, noofinstances: Int, req_type: String, command: NodeCommand, predefs: NodePredefs, appdefns: NodeAppDefns, boltdefns: NodeBoltDefns, appreq: NodeAppReq, boltreq: NodeBoltReq) {
  val formatReqsJson = "\"command\": " + command.json
  val appdefjson = "\"appdefns\": " + appdefns.json
  val boltdefjson = "\"boltdefns\": " + boltdefns.json
}

case class NodeResult(id: String, accounts_id: String, node_type: String, status: NodeStatusType, request: NodeRequest, predefs: NodePredefs, appdefnsid: String, boltdefnsid: String, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"accounts_id\":\"" + accounts_id + "\",\"node_type\":\"" + node_type + "\",\"status\":\"" + status.stringVal +
    "\",\"request\":{" + request.toString + "} ,\"predefs\":{" + predefs.toString + "},\"appdefnsid\":\"" + appdefnsid + "\",\"boltdefnsid\":\"" + boltdefnsid + "\",\"created_at\":\"" + created_at + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.NodeResultSerialization
    val nrsser = new NodeResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object NodeResult {

  //def apply(id: String, accounts_id: String, status: NodeStatusType, request: NodeRequest, predefs: NodePredefs, appdefnsid: String) = new NodeResult(new String(), new String(), NodeStatusType.AM_HUNGRY, new NodeRequest(), new NodePredefs(
  //new String(), new String(), new String, new String(), new String()), new String())

  def apply = new NodeResult(new String(), new String(), new String(), NodeStatusType.AM_HUNGRY, new NodeRequest(), new NodePredefs(
    new String(), new String(), new String, new String(), new String()), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[NodeResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.NodeResultSerialization
    val nrsser = new NodeResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[NodeResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class NodeProcessedResult(key: String, node_type: String, req_id: String, req_type: String) {
  val json = "{\"key\": \"" + key + "\",\"node_type\":\"" + node_type + "\",\"req_id\":\"" + req_id + "\",\"req_type\":\"" + req_type + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.NodeProcessedResultSerialization
    val nrsser = new NodeProcessedResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object NodeProcessedResult {

  def apply = new NodeProcessedResult(new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[NodeProcessedResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.NodeProcessedResultSerialization
    val nrsser = new NodeProcessedResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[NodeProcessedResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class NodeRequest(req_id: String, req_type: String, command: NodeCommand) {
  def this() = this(new String(), new String(), NodeCommand.empty)
  override def toString = "\"req_id\":\"" + req_id + "\",\"req_type\":\"" + req_type + "\",\"command\": " + command.json
}

case class NodePredefs(name: String, scm: String, war: String, db: String, queue: String) {
  override def toString = "\"name\":\"" + name + "\",\"scm\":\"" + scm + "\",\"war\":\"" + war + "\",\"db\":\"" + db + "\",\"queue\":\"" + queue + "\""
}

case class NodeAppDefns(timetokill: String, metered: String, logging: String, runtime_exec: String) {
  val json = "{\"timetokill\":\"" + timetokill + "\",\"metered\":\"" + metered + "\",\"logging\":\"" + logging + "\",\"runtime_exec\":\"" + runtime_exec + "\"}"
}

case class NodeBoltDefns(username: String, apikey: String, store_name: String, url: String, prime: String, timetokill: String, metered: String, logging: String, runtime_exec: String) {
  val json = "{\"username\":\"" + username + "\",\"apikey\":\"" + apikey + "\",\"store_name\":\"" + store_name + "\",\"url\":\"" + url + "\",\"prime\":\"" + prime + "\",\"timetokill\":\"" + timetokill + "\",\"metered\":\"" + metered + "\",\"logging\":\"" + logging + "\",\"runtime_exec\":\"" + runtime_exec + "\"}"

}

case class NodeAppReq() {
  override def toString = ""
}

case class NodeBoltReq() {
  override def toString = ""
}

sealed abstract class NodeStatusType(override val stringVal: String) extends Enumeration

object NodeStatusType {
  object AM_HUNGRY extends NodeStatusType("AM_HUNGRY")
  object REQ_CREATED_AT_SOURCE extends NodeStatusType("REQ_CREATED_AT_SOURCE")
  object NODE_CREATED_AT_SOURCE extends NodeStatusType("NODE_CREATED_AT_SOURCE")
  object PUBLISHED extends NodeStatusType("PUBLISHED")
  object STARTED extends NodeStatusType("STARTED")
  object LAUNCH_SUCCESSFUL extends NodeStatusType("LAUNCH_SUCCESSFUL")
  object LAUNCH_FAILED extends NodeStatusType("LAUNCH_FAILED")

  implicit val NodeStatusTypeToReader = upperEnumReader(AM_HUNGRY, REQ_CREATED_AT_SOURCE, NODE_CREATED_AT_SOURCE,
    PUBLISHED, STARTED, LAUNCH_SUCCESSFUL, LAUNCH_FAILED)
}

case class NodeCommand(systemprovider: NodeSystemProvider, compute: NodeCompute, cloudtool: NodeCloudToolService) {
  val json = "{\"systemprovider\": " + systemprovider.json + "}, \"compute\": " + compute.json + "\"}}, \"cloudtool\": {" +
    cloudtool.json + "\"}}}"
}

object NodeCommand {
  //this is a very ugly hack. I am tad lazy to write individual objects.
  def empty: NodeCommand = new NodeCommand(NodeSystemProvider.empty,
    new NodeCompute(new String(), new NodeComputeDetail(new String(), new String(), new String()),
      new NodeComputeAccess(new String(), new String(), new String())),
    NodeCloudToolService.empty)
}

case class NodeSystemProvider(provider: NodeProvider) {
  val json = "{\"provider\" : {" + provider.json + "}"
}

object NodeSystemProvider {
  def empty: NodeSystemProvider = new NodeSystemProvider(new NodeProvider())
}

case class NodeProvider(prov: String = "chef") {
  val json = "\"prov\": \"" + prov + "\""
}

object NodeProvider {
  def empty: NodeProvider = new NodeProvider()
}

case class NodeCompute(cctype: String, cc: NodeComputeDetail, access: NodeComputeAccess) {
  val json = "{\"cctype\": \"" + cctype + "\", " + "\"cc\": " + "{" + cc.json + "}, \"access\" : " + access.json
}

case class NodeComputeDetail(groups: String, image: String, flavor: String) {
  val json = "\"groups\": \"" + groups + "\", " + "\"image\": \"" + image + "\", " + "\"flavor\": \"" + flavor + "\""
}

case class NodeComputeAccess(ssh_key: String, identity_file: String, ssh_user: String) {
  val json = "{\"ssh_key\": \"" + ssh_key + "\", " + "\"identity_file\": \"" + identity_file + "\", " + "\"ssh_user\": \"" + ssh_user
}

case class NodeCloudToolService(chef: NodeCloudToolChef) {
  val json = "\"chef\": {" + chef.json
}

object NodeCloudToolService {
  def empty: NodeCloudToolService = new NodeCloudToolService(new NodeCloudToolChef(new String(), new String(), new String(), new String()))
}

case class NodeCloudToolChef(command: String, plugin: String, run_list: String, name: String) {
  val json = "\"command\": \"" + command + "\", " + "\"plugin\": \"" + plugin + "\"," +
    "\"run_list\": \"" + run_list + "\", " + "\"name\": \"" + name
}

object Nodes {

  implicit val formats = DefaultFormats

  implicit def NodeResultsSemigroup: Semigroup[NodeResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  implicit def NodeProcessedResultsSemigroup: Semigroup[NodeProcessedResults] = Semigroup.instance((f3, f4) => f3.append(f4))

  private def riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")

  val metadataKey = "Node"
  val newnode_metadataVal = "New Node Creation"
  val newnode_bindex = BinIndex.named("accountId")

  private def nodetypedecision(email: String, node_id: String, nir: NodeInput): ValidationNel[Throwable, Tuple2[String, String]] = {
    if (nir.node_type != "BOLT") {
      for {
        adef <- (AppDefns.createforNewNode("{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + nir.node_name + "\"," + nir.appdefjson + "}") leftMap { t: NonEmptyList[Throwable] => t })
      } yield {
        val adf = adef.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "App Definitions create failed (or) not found. Retry again."))
        Tuple2(adf._1, nir.node_type)
      }
    } else {
      for {
        bdef <- (BoltDefns.createforNewNode("{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + nir.node_name + "\"," + nir.boltdefjson + "}") leftMap { t: NonEmptyList[Throwable] => t })
      } yield {
        val bdf = bdef.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "Bolt Definitions create failed (or) not found. Retry again."))
        Tuple2(bdf._1, nir.node_type)
      }
    }
  }

  /**
   * The no_of_instances as sent as the input dictates the 'x' times the private
   * create method is called.
   * If you need 1 instance send no_of_instance as 1
   */
  def createMany(email: String, input: String): ValidationNel[Throwable, NodeProcessedResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "createMany:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val nodeInput: ValidationNel[Throwable, NodeInput] = (Validation.fromTryCatch {
      parse(input).extract[NodeInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("nodeInput", nodeInput))

    val res = (for {
      nir <- nodeInput
    } yield {
      broadenNodes(nir)
    }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
      nodpl: Option[List[NodeInput]] =>
        (nodpl map {
          _.map { nodinp =>
            play.api.Logger.debug(("%-20s -->[%s]").format("node", nodinp))
            (create(email, nodinp))
          }
        } map {
          _.foldRight((NodeProcessedResults.empty).successNel[Throwable])(_ +++ _)
        }).head
    }
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", res))
    res.getOrElse(new ResourceItemNotFound(email, "nodes = ah. ouh. ror some reason.").failureNel[NodeProcessedResults])
    res
  }

  private def broadenNodes(inp: NodeInput): Option[List[NodeInput]] = {
    val fn1 = inp.node_name.some map { fs1 => fs1.split('.').take(1).mkString }
    val rn1 = inp.node_name.some map { fs2 => fs2.split('.').drop(1).mkString(".", ".", "") }

    ((1 to inp.noofinstances).toList map { sufc: Int =>
      val fnsxrn = ((fn1 |@| sufc.some |@| rn1).apply { _ + _ + _ })
      ((fnsxrn |@| inp.node_type.some |@| inp.noofinstances.some |@| inp.req_type.some
        |@| inp.command.some |@| inp.predefs.some |@| inp.appdefns.some
        |@| inp.boltdefns.some |@| inp.appreq.some |@| inp.boltreq.some)(NodeInput)).get
    }).some
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  private def create(email: String, input: NodeInput): ValidationNel[Throwable, NodeProcessedResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    for {
      ogsi <- mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err }
      nrip <- NodeResult.fromJson(ogsi.get.value) leftMap { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] => println("osgi\n" + ogsi.get.value); play.api.Logger.debug(JSONParsingError(t).toString); nels(JSONParsingError(t)) }
      ogsr <- riak.store(ogsi.get) leftMap { t: NonEmptyList[Throwable] => play.api.Logger.debug("--------> ooo"); t }
    } yield {
      play.api.Logger.debug(("%-20s -->[%s],riak returned: %s").format("Node.created successfully", email, ogsr))
      ogsr match {
        case Some(thatGS) => {
          nels(NodeProcessedResult(thatGS.key, nrip.node_type, nrip.request.req_id, nrip.request.req_type).some)
        }
        case None => {
          play.api.Logger.warn(("%-20s -->[%s]").format("Node.created successfully", "Scaliak returned => None. Thats OK."))
          nels(NodeProcessedResult(ogsi.get.key, nrip.node_type, nrip.request.req_id, nrip.request.req_type).some)
        }
      }
    }

    /*
    val reqPush: ValidationNel[Throwable, NodeInput] = (Validation.fromTryCatch {
      MessageObjects.Publish(Message)
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("nodeinp", nodeInput))
  */
  }

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, nir: NodeInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("nir", nir))

    for {
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "nod").get leftMap { ut: NonEmptyList[Throwable] => ut })
      req <- (Requests.createforNewNode("{\"node_id\": \"" + (uir.get._1 + uir.get._2) + "\",\"node_name\": \"" + nir.node_name + "\",\"req_type\": \"" + nir.req_type + "\"," + nir.formatReqsJson + "}") leftMap { t: NonEmptyList[Throwable] => t })
      abid <- (nodetypedecision(email, (uir.get._1 + uir.get._2), nir) leftMap { err: NonEmptyList[Throwable] => err })

    } yield {
      aor match {
        //TO-DO: The  / errors may not be needed. But its a trap to see if it happens
        //      If it happens, then we need to progate them in the NEL(Error,..)
        case Some(asuc) => {
          val nuid = uir.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "UID not found (or) server unavailable. Retry again."))
          val rres = req.getOrElse(throw new ServiceUnavailableError("[" + email + ":" + nir.node_name + "]", "Request create failed (or) not found. Retry again."))
          play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "request created."))
          val bvalue = Set(asuc.id)
          val jsonobj = if (nir.node_type == "APP") {
            NodeResult((nuid._1 + nuid._2), asuc.id, nir.node_type, NodeStatusType.REQ_CREATED_AT_SOURCE,
              NodeRequest(rres._1, nir.req_type, rres._2), nir.predefs, abid._1, "", Time.now.toString)
          } else {
            NodeResult((nuid._1 + nuid._2), asuc.id, nir.node_type, NodeStatusType.REQ_CREATED_AT_SOURCE,
              NodeRequest(rres._1, nir.req_type, rres._2), nir.predefs, "", abid._1, Time.now.toString)
          }

          play.api.Logger.debug(("%-20s -->[%s]").format("formatted node store", jsonobj.toJson(true)))
          val json = jsonobj.json
          new GunnySack(nir.node_name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
            Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
        }
        case None => throw new ResourceItemNotFound(email, "Account with email was retrieved but nothing to use. Retry again.")
      }
    }
  }

  /**
   * Measure the duration to accomplish a usecase by listing an user who has 10 nodes using single threaded
   * (current behaviour)
   * https://github.com/twitter/util (Copy the Duration code into a new perf pkg in megam_common.)
   * val elapsed: () => Duration = Stopwatch.start()
   * val duration: Duration = elapsed()
   *
   * TODO: Converting to Async Futures.
   * ----------------------------------
   * takes an input list of nodenames which will return a Future[ValidationNel[Error, NodeResults]]
   */
  def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, NodeResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    (nodeNameList map {
      _.map { nodeName =>
        play.api.Logger.debug(("%-20s -->[%s]").format("nodeName", nodeName))
        (riak.fetch(nodeName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(nodeName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (NodeResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: NodeResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("noderesult", j))
                  Validation.success[Throwable, NodeResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
                }
            }
            case None => {
              Validation.failure[Throwable, NodeResults](new ResourceItemNotFound(nodeName, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((NodeResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 
  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[NodeResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[NodeResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, NodeResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, NodeResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = BinIndex.named("")
        val bvalue = Set("")
        val metadataVal = "Nodes-name"
        new GunnySack("accountId", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByNodeName(nm.some) else
          new ResourceItemNotFound(email, "nodes = nothing found.").failureNel[NodeResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", res))
    res.getOrElse(new ResourceItemNotFound(email, "nodes = ah. ouh. ror some reason.").failureNel[NodeResults])
  }

}
