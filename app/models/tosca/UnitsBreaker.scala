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
import models.json.tosca._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import models.base._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset


class UnitsBreaker(input: String, authBag: Option[io.megam.auth.stack.AuthBag] ) {

  implicit val formats = DefaultFormats

  private lazy val toObject: ValidationNel[Throwable, AssembliesInput] = {
    val aio: ValidationNel[Throwable, AssembliesInput] = (Validation.fromTryCatchThrowable[AssembliesInput, Throwable] {
      parse(input).extract[AssembliesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

    for { //for comprehension is abused here. :)
      aip <- aio
    } yield {
      aip
    }
  }


  private def till = toObject.map(_.number_of_units).getOrElse(0)

  private def uno  = (till == 1)


  //if its a uno (single launch, then return and use the name)
  private def nameOfUnit(i: Int, n: String): String = {
    if (uno)  n else  toObject.map(_.nameOverriden(n + i)).getOrElse(n + i)
  }

  def break: ValidationNel[Throwable, AssembliesInputList]  = {
    if (till < 1) return (new MalformedBodyError(input, "Can't parse assemblies input.").failureNel[AssembliesInputList])

    ((1 to till).map(i => mkLaunchable(i, input)).some  map {
      _.foldRight((AssembliesInputList.empty).successNel[Throwable])(_ +++ _)
    }).head
 }


  private def mkLaunchable(i: Int, input: String) = {
    for {
      too <- toObject
    } yield {
        val changed = too.assemblies.map { ai =>
        val scrubInputs = PatternLabeler(ai.inputs, authBag).labeled

        val decompkvs = FieldSplitter(till, KeyValueField("quota_ids", "quota_id"), scrubInputs).merged
        Assembly(nameOfUnit(i, ai.name), ai.components, ai.tosca_type,
                                        ai.policies, decompkvs.get(i).get, ai.outputs, ai.status, ai.state)
        }

      List(AssembliesInput(too.name, too.org_id, changed, too.inputs))
    }
  }
}

case class PatternLabeler(inputs: KeyValueList, authBag: Option[io.megam.auth.stack.AuthBag]) {

  lazy val email  = authBag.get.email
  lazy val org_id = authBag.get.org_id

  lazy val pi   = PatternedLabel(inputs, email, org_id)
  lazy val name = (new PatternedLabelReplacer(pi)).name

  def labeled = {
      name match {
        case Some(succ) => KeyValueList.merge(inputs,Map(succ._1 -> succ._2.getOrElse("")))
        case None       => inputs
      }
  }

}

case class FieldSplitter(nos: Int, field: KeyValueField, kvs: KeyValueList) {

  val VALUE_KEY = field.value

  val FILTER_KEY = field.key

  val COMMA = ","

  val filter = KeyValueList.filter(kvs, FILTER_KEY)

  val filterNot = KeyValueList.filterNot(kvs, FILTER_KEY)

  val split  = (filter.map { x =>
    if(x.value.contains(COMMA)) {
      x.value.split(COMMA).map(KeyValueField(VALUE_KEY, _)).toList
    } else {
      List(KeyValueField(VALUE_KEY, x.value)).toList
    }}).flatten.zipWithIndex.map(x => List(x._1)
)

  val merged: Map[Int, KeyValueList] = ({
     if (split.isEmpty) {
      ((1 to nos).toList.map((_, filterNot)))
    } else {
       ((1 to nos).toList.zip(split.map(_  ++ filterNot)))
     }
  }).toMap
}
