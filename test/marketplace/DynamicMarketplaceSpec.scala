package test


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
import org.specs2.Specification
import org.junit.runner._



object DynamicMarketplaceSpec extends Specification {

def is =
    "DynamicMarketplace:".title ^ br ^
      "Functionality for loading marketplace dynamically." ^ br ^
      "Load marketplace" ^ br ^
      "can do a load of yaml" ! LoadMarketplaceYaml.succeeds ^ br ^
      end

case object LoadMarketplaceYaml {



   def succeeds = {
    val contentToEncode = scala.io.Source.fromFile("./test/marketplace/market.yaml").mkString
    val contentInput: Map[String, String] = mapAsScalaMap[String, String](new Yaml().load(contentToEncode).asInstanceOf[java.util.Map[String, String]]).toMap
    println(contentInput)
    contentInput must beEqualTo(1)
    //some(new String(contentInput))

}

}

}
