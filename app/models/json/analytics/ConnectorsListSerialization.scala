package models.json.analytics

 import scalaz._
 import scalaz.NonEmptyList._
 import Scalaz._
 import net.liftweb.json._
 import net.liftweb.json.scalaz.JsonScalaz._
 import models.tosca._
 import java.nio.charset.Charset
 import models.analytics.{ConnectorsList, Connectors}

 /**
  * @author ranjitha
  *
  */
 object ConnectorsListSerialization extends models.json.SerializationBase[ConnectorsList] {

   implicit override val writer = new JSONW[ConnectorsList] {
     override def write(h: ConnectorsList): JValue = {
       val nrsList: Option[List[JValue]] = h.map {
         nrOpt: Connectors => nrOpt.toJValue
       }.some

       JArray(nrsList.getOrElse(List.empty[JValue]))
     }
   }

   implicit override val reader = new JSONR[ConnectorsList] {
     override def read(json: JValue): Result[ConnectorsList] = {
       json match {
         case JArray(jObjectList) => {
           val list = jObjectList.flatMap { jValue: JValue =>
             Connectors.fromJValue(jValue) match {
               case Success(nr) => List(nr)
               case Failure(fail) => List[Connectors]()
             }
           }.some

           val nrs: ConnectorsList = ConnectorsList(list.getOrElse(ConnectorsList.empty))
           nrs.successNel[Error]
         }
         case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[ConnectorsList]
       }
     }
   }
 }
