package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.json._
import models.base._
import db._
import cache._
import app.MConfig
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import models.tosca.{ KeyValueField, KeyValueList}

import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import com.twitter.util.{ Future, Await }
import com.twitter.conversions.time._

import com.websudos.phantom.dsl._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import controllers.stack.ImplicitJsonFormats

case class MarketPlaceSack(
  settings_name: String,
  cattype: String,
  flavor: String,
  image: String,
  catorder: String,
  url: String,
  json_claz: String,
  envs: KeyValueList,
  options: KeyValueList,
  plans: Map[String, String]) {}



//table class for holding the ds of a particular type(mkp in our case)
sealed class MarketPlaceT extends CassandraTable[MarketPlaceT, MarketPlaceSack] with ImplicitJsonFormats  {

  object settings_name extends StringColumn(this)
  object cattype extends StringColumn(this)
  object flavor extends StringColumn(this) with PrimaryKey[String]
  object image extends StringColumn(this)
  object catorder extends StringColumn(this)
  object url extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object envs extends JsonListColumn[MarketPlaceT, MarketPlaceSack, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object options extends JsonListColumn[MarketPlaceT, MarketPlaceSack, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object plans extends MapColumn[MarketPlaceT, MarketPlaceSack, String, String](this)

  override def fromRow(r: Row): MarketPlaceSack = {
    MarketPlaceSack(
      settings_name(r),
      cattype(r),
      flavor(r),
      image(r),
      catorder(r),
      url(r),
      json_claz(r),
      envs(r),
      options(r),
      plans(r))
  }
}

/*
 *   This class talks to scylla and performs the actions
 */
abstract class ConcreteMkp extends MarketPlaceT with ScyllaConnector {

  override lazy val tableName = "marketplaces"

  def listRecords(): ValidationNel[Throwable, Seq[MarketPlaceSack]] = {
    val res = select.collect()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(flavor: String): ValidationNel[Throwable, Option[MarketPlaceSack]] = {
    val res = select.allowFiltering().where(_.flavor eqs flavor).get()
    Await.result(res, 5.seconds).successNel
  }
}

object MarketPlaces extends ConcreteMkp {

  def listAll(): ValidationNel[Throwable, Seq[MarketPlaceSack]] = {
    (listRecords() leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Marketplace items = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[MarketPlaceSack] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[MarketPlaceSack]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[MarketPlaceSack]](new ResourceItemNotFound("", "Marketplace items = nothing found.")).toValidationNel
    }
  }

  def findByFlavor(mkpFlavor: Option[List[String]]): ValidationNel[Throwable, MarketPlaceResults] = {
    (mkpFlavor map {
      _.map { mkp_fla =>
        (getRecord(mkp_fla) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(mkp_fla, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[MarketPlaceSack] =>
          xso match {
            case Some(xs) => {
              Validation.success[Throwable, MarketPlaceResults](List(xs.some)).toValidationNel //screwy kishore, every element in a list ?
            }
            case None => {
              Validation.failure[Throwable, MarketPlaceResults](new ResourceItemNotFound(mkp_fla, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

}
