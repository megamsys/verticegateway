package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import models.Constants._

/**
 * @author ram
 *
 */
package object tosca {


  type AssembliesInputList = List[AssembliesInput]

  object AssembliesInputList {
    def empty: AssembliesInputList = List()
  }

  type ComponentsList = List[Component]

  type AssemblysList = List[Assembly]

  type AssembliesResults = List[Option[AssembliesResult]]

  object AssembliesResults {
    val emptyNR = List(Option.empty[AssembliesResult])
    def apply(m: Option[AssembliesResult]) = List(m)
    def apply(m: AssembliesResult): AssembliesResults = AssembliesResults(m.some)
    def empty: AssembliesResults = List() //nel(emptyNR.head, emptyNR.tail)
  }

  type AssemblyResults = List[Option[AssemblyResult]]

  object AssemblyResults {
    val emptyNR = List(Option.empty[AssemblyResult])
    def apply(m: Option[AssemblyResult]) = List(m)
    def apply(m: AssemblyResult): AssemblyResults = AssemblyResults(m.some)
    def empty: AssemblyResults = List()
  }

  type ComponentResults = List[Option[ComponentResult]]

  object ComponentResults {
    val emptyNR = List(Option.empty[ComponentResult])
    def apply(m: Option[ComponentResult]) = List(m)
    def apply(m: ComponentResult): ComponentResults = ComponentResults(m.some)
    def empty: ComponentResults = List()
  }

  type ComponentLists = NonEmptyList[Option[ComponentResult]]
  object ComponentLists {
    val emptyNR = List(Option.empty[ComponentResult])
    def apply(m: Option[ComponentResult]) = nels(m)
    def empty: ComponentLists = nel(emptyNR.head, emptyNR.tail)
  }

  type AssemblysLists = NonEmptyList[Option[AssemblyResult]]

  object AssemblysLists {
    val emptyNR = List(Option.empty[AssemblyResult])
    def apply(m: Option[AssemblyResult]) = nels(m)
    def apply(m: AssemblyResult): AssemblysLists = AssemblysLists(m.some)
    def empty: AssemblysLists = nel(emptyNR.head, emptyNR.tail)
  }

  type AssemblyLinks = List[String]

  object AssemblyLinks {
    val emptyRR = List("")
    def apply(assemblysList: List[String]): AssemblyLinks = assemblysList
    def empty: List[String] = emptyRR
  }

 type ComponentLinks = List[String]

  object ComponentLinks {
    val emptyRR = List("")
    def apply(plansList: List[String]): ComponentLinks = plansList
    def empty: List[String] = emptyRR
  }

  type MetricsList = List[Metrics]

  type PoliciesList = List[Policy]
  object PoliciesList {
    def empty: List[Policy] = List[Policy]()
  }

  type MembersList = List[String]

  object MembersList {
    val emptyRR = List("")
    def toJValue(nres: MembersList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.MembersListSerialization.{ writer => MembersListWriter }
      toJSON(nres)(MembersListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MembersList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.carton.MembersListSerialization.{ reader => MembersListReader }
      fromJSON(jValue)(MembersListReader)
    }

    def toJson(nres: MembersList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): MembersList = plansList

    def empty: List[String] = emptyRR

  }

  type KeyValueList = List[KeyValueField]

  object KeyValueList {
    val MKT_FLAG_EMAIL        = "<email>"
    val MKT_FLAG_APIKEY       = "<api_key>"
    val MKT_FLAG_ASSEMBLY_ID  = "<assembly_id>"
    val MKT_FLAG_COMP_ID      = "<component_id>"
    val MKT_FLAG_HOST         = "<host>"

    val emptyRR = List(KeyValueField.empty)

    def toJValue(nres: KeyValueList): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
      toJSON(nres)(KeyValueListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[KeyValueList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }
      fromJSON(jValue)(KeyValueListReader)
    }

    def toJson(nres: KeyValueList, prettyPrint: Boolean = false, flagsMap: Map[String, String] = Map()): String = {
      val nrec = nres.map { x => KeyValueField(x.key, flagsMap.get(x.value).getOrElse(x.value)) }
      if (prettyPrint) {
        prettyRender(toJValue(nrec))
      } else {
        compactRender(toJValue(nrec))
      }
    }

    def merge(nres: KeyValueList, flagsMap: Map[String, String] = Map()): KeyValueList = {
      val nrec = nres.map { x => KeyValueField(x.key, flagsMap.get(x.value).getOrElse(x.value)) }
      nrec
    }

    def apply(m: Map[String, String]): KeyValueList = m.toList.map { x => KeyValueField(x._1, x._2) }

    def apply(plansList: List[KeyValueField]): KeyValueList = plansList

    def empty: List[KeyValueField] = emptyRR

    def toMap(nres: KeyValueList) = (nres.map { x => (x.key, x.value) }).toMap

  }

  type OperationList = List[Operation]

  type BindLinks = List[String]

  object BindLinks {
    val emptyRR = List("")
    def apply(plansList: List[String]): BindLinks = plansList
    def empty: List[String] = emptyRR
  }
}
