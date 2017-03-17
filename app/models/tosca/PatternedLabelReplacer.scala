package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.Constants._
import models.json.tosca._
import models.base.RequestInput
import io.megam.auth.funnel.FunnelErrors._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats

case class PatternedLabel(kv: KeyValueList, email: String, org_id: String) {

  def find(setKey: String) = {
    val kvl = KeyValueList.filter(kv, setKey).some
    kvl.map(_.map(_.value))
  }

  lazy val pattern    = find(PatternConstants.LAB_PATTERN).map(_.mkString)

  lazy val regionOpt  = find(PatternConstants.LAB_REGION).map(_.mkString)
  lazy val region     = regionOpt.getOrElse("")

  lazy val profileOpt = "([A-Za-z]+)".r.findFirstIn(email)
  lazy val profile    = profileOpt.getOrElse("")
}

object PatternConstants {
  val LAB_PATTERN = "user.launch.patternname"
  val LAB_REGION  = " user.launch.region"

  val LAB_NAME    = "user.launch.labeledname"

  val PROFILE     = "{{profile}}"
  val REGION      = "{{region}}"

  val PLUS_PLUS   = "{{++}}"
  val A_PLUS_PLUS = "{{a++}}"
  val R_PLUS_PLUS = "{{r++}}"

  val ZERO:Long = 0
}

class PatternedLabelReplacer(pl: PatternedLabel) {

  lazy val pls =   Map[String,PatternLabelerCondition](
    PatternConstants.PROFILE       ->  new ProfileLabler(pl.pattern),
    PatternConstants.REGION        ->  new RegionNameLabler(pl.pattern),
    PatternConstants.PLUS_PLUS     ->  new GlobalIncrementedLabler(pl.pattern),
    PatternConstants.A_PLUS_PLUS   ->  new SeriesIncrementedLabler(pl.pattern),
    PatternConstants.R_PLUS_PLUS   ->  new RandomPatternLabler(pl.pattern))


  def name: Option[Tuple2[String, Option[String]]]  = {
    for {
      m <- pls.filter( x =>   x._2.pmatch).some
    } yield {
      (PatternConstants.LAB_NAME, replace(m.values.toList, pl.pattern))
    }
  }

  private def replace(pxs: List[PatternLabelerCondition], optReplaced: Option[String]): Option[String] = {
    optReplaced match {
      case Some(succ) => {
        if (pxs.isEmpty) optReplaced
        else replace(pxs.tail, pxs.head.apply(optReplaced,pl))
      }
     case None => optReplaced
    }
  }

}


trait PatternLabelerCondition {
  def pmatch: Boolean
  def apply(newfp: Option[String], pl: PatternedLabel): Option[String]
}

class ProfileLabler(fp: Option[String]) extends PatternLabelerCondition {
  private val PATTERN = PatternConstants.PROFILE

  def pmatch = fp.map(_.contains(PATTERN)).getOrElse(false)

  def apply(newfp: Option[String], pl: PatternedLabel) = newfp.map(_.r.replaceAllIn(PATTERN, pl.profile))
}


class RegionNameLabler(fp: Option[String]) extends PatternLabelerCondition {
  private val PATTERN = PatternConstants.REGION

  def pmatch = fp.map(_.contains(PATTERN)).getOrElse(false)

  def apply(newfp: Option[String], pl: PatternedLabel) = newfp.map(_.r.replaceAllIn(PATTERN, pl.region))
}


class GlobalIncrementedLabler(fp: Option[String]) extends PatternLabelerCondition {
  private val PATTERN = PatternConstants.PLUS_PLUS

  def pmatch = fp.map(_.contains(PATTERN)).getOrElse(false)

  def apply(newfp: Option[String],pl: PatternedLabel) = {
    val optGloinc = (Assembly.countAllRecords match {
      case Success(succ) => succ
      case Failure(err)  => None
    })

    val gloinc = optGloinc.getOrElse(PatternConstants.ZERO)

    newfp.map(_.r.replaceAllIn(PATTERN, gloinc.toString))
  }
}

class SeriesIncrementedLabler(fp: Option[String]) extends PatternLabelerCondition {
  private val PATTERN = PatternConstants.A_PLUS_PLUS

  def pmatch = fp.map(_.contains(PATTERN)).getOrElse(false)

  def apply(newfp: Option[String],pl: PatternedLabel) = {
    val optSerinc = (Assembly.countByOrgId(pl.org_id) match {
      case Success(succ) => succ
      case Failure(err)  => None
    })

    val serinc =optSerinc.getOrElse(PatternConstants.ZERO)

    newfp.map(_.r.replaceAllIn(PATTERN, serinc.toString))
  }
}

class RandomPatternLabler(fp: Option[String]) extends PatternLabelerCondition {
  private val PATTERN = PatternConstants.R_PLUS_PLUS

  def pmatch = fp.map(_.contains(PATTERN)).getOrElse(false)


  def apply(newfp: Option[String],pl: PatternedLabel) = newfp.map(_.r.replaceAllIn(PATTERN, scala.util.Random.nextString(4)))
}
