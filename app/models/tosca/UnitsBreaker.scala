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
import models.json.tosca.carton._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import models.base._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset


class UnitsBreaker(input: String) {

  implicit val formats = DefaultFormats

  private def toObject: ValidationNel[Throwable, AssembliesInput] = {
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
      val changed = too.assemblies.map(ai => Assembly(nameOfUnit(i, ai.name), ai.components, ai.tosca_type,
                                        ai.policies, ai.inputs, ai.outputs, ai.status, ai.state))
      List(AssembliesInput(too.name, too.org_id, changed, too.inputs))
    }
  }
}
