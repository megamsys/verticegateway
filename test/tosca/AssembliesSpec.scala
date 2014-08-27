/* 
** Copyright [2013-2014] [Megam Systems]
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
package test.tosca

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models.tosca._
import models.tosca.Assemblies
import test.{Context}
/**
 * @author ram
 *
 */
class AssembliesSpec extends Specification {

  def is =
    "AssembliesSpec".title ^ end ^ """
  AssembliesSpec is the implementation that calls the megam_play API server with the /assemblies url
  """ ^ end ^
      "The Client Should" ^     
      "Correctly do POST assemblies with a valid userid and api key" ! Post.succeeds ^
  end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "assemblies/content"

    protected override def bodyToStick: Option[String] = {
      val inputs = new AssembliesInputs("9c8281e6.637d8", "tab", "Sheet 2")
    /*  val components1 = Components(List(new Component("component_java", "tosca.web.java", "requirements", new ComponentInputs("8dc70ddc.7238f", "385", "103", "9c8281e6.637d8"), "url", new Artifacts("tosca type", "", "requirement_type"), "relatedcomponents", new ComponentOperations("", "")), new Component("component_play", "tosca.web.play", "requirements", new ComponentInputs("bac8df1e.45372", "385", "217", "9c8281e6.637d8"), "url", new Artifacts("tosca type", "", "requirement_type"), "relatedcomponents", new ComponentOperations("", ""))))
      val components2 = Components(List(new Component("component_ruby", "tosca.web.ruby", "requirements", new ComponentInputs("57444c17.a8bbb4", "216", "162", "9c8281e6.637d8"), "url", new Artifacts("tosca type", "", "requirement_type"), "relatedcomponents", new ComponentOperations("", ""))))
      println(components1)
      println("*******************")
      println(components2)
      val assembly = new Assembly("app_java", components1, "policies", "", "")
      println(assembly.json)
      val assembl = List(new Assembly("app_java", components1, "policies", "", ""), new Assembly("app_ruby", components2, "policies", "", ""))
      println("*******************")
       println(assembl)
      val assemblies = AssembliesList(List(new Assembly("app_java", components1, "policies", "", ""), new Assembly("app_ruby", components2, "policies", "", "")))
      
      println(assemblies)
      val contentToEncode = AssembliesInput("Sheet 2", assemblies, inputs).json*/
      //val contentToEncode = "{\"name\":\"Sheet 2\",\"assemblies\":"+ AssembliesList.(assemblies,true) + ",\"inputs\": " + inputs + "}"
       val contentToEncode = "{\"name\":\"Sheet 2\",\"assemblies\":[{\"name\":\"app_java\",\"components\":[" +
                                  "{\"name\":\"component_java\",\"tosca_type\":\"tosca.web.java\",\"requirements\":\"requirements\",\"inputs\":{\"id\":\"8dc70ddc.7238f\",\"x\":385,\"y\":103,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":\"url\",\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":\"requirement_type\"},\"related_components\":\"relatedcomponents\",\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}},"+
                                  "{\"name\":\"component_play\",\"tosca_type\":\"tosca.web.play\",\"requirements\":\"requirements\",\"inputs\":{\"id\":\"bac8df1e.45372\",\"x\":385,\"y\":217,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":\"url\",\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":\"requirement_type\"},\"related_components\":\"relatedcomponents\",\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}}],"+
                            "\"policies\":\"policies\",\"inputs\":\"\",\"operations\":\"\"},"+
                            "{\"name\":\"app_ruby\",\"components\":[{\"name\":\"component_ruby\",\"tosca_type\":\"tosca.web.ruby\",\"requirements\":\"host\",\"inputs\":{\"id\":\"57444c17.a8bbb4\",\"x\":216,\"y\":162,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":\"url\",\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":\"requirement_type\"},\"related_components\":\"relatedcomponents\",\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}}],"+
                            "\"policies\":\"policies\",\"inputs\":\"\",\"operations\":\"\"}],\"inputs\": " + inputs + "}"
      //val contentToEncode = "{\"name\":\"Sheet 2\",\"assemblies\":[{\"name\":\"app_java\", "+
      //                      "\"components\":[{\"name\":\"component_java\",\"tosca_type\":\"tosca.web.java\",\"requirements\":{\"host\":\"app_java.undefined\"},\"inputs\":{\"id\":\"8dc70ddc.7238f\",\"x\":385,\"y\":103,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":{\"url\":\"\"},\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":{\"requirement_type\":\"create\"}},\"related_components\":[],\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}},"+
       //                     "{\"name\":\"component_play\",\"tosca_type\":\"tosca.web.play\",\"requirements\":{\"host\":\"app_java.undefined\"},\"inputs\":{\"id\":\"bac8df1e.45372\",\"x\":385,\"y\":217,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":{\"url\":\"\"},\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":{\"requirement_type\":\"create\"}},\"related_components\":[],\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}}],"+
      //                      "\"policies\":{\"placement_policy\":{\"name\":\"placement policy\",\"type\":\"colocated\",\"members\":[\"component_java\"]}},"+
      //                      "\"inputs\":\"\",\"operations\":\"\"},{\"name\":\"app_ruby\",\"components\":[{\"name\":\"component_ruby\",\"tosca_type\":\"tosca.web.ruby\",\"requirements\":{\"host\":\"app_ruby.undefined\"},\"inputs\":{\"id\":\"57444c17.a8bbb4\",\"x\":216,\"y\":162,\"z\":\"9c8281e6.637d8\"},\"external_management_resource\":{\"url\":\"\"},\"artifacts\":{\"artifact_type\":\"tosca type\",\"content\":\"\",\"requirements\":{\"requirement_type\":\"create\"}},\"related_components\":[],\"operation\":{\"operation_type\":\"\",\"target_resource\":\"\"}}],"+
      //                      "\"policies\":{\"placement_policy\":{\"name\":\"placement policy\",\"type\":\"colocated\",\"members\":[\"component_ruby\"]}},\"inputs\":\"\",\"operations\":\"\"}],\"inputs\":{\"id\":\"9c8281e6.637d8\",\"type\":\"tab\",\"label\":\"Sheet 2\"}}"
    //  val inputs = new AssembliesInputs("9c8281e6.637d8", "tab", "Sheet 2").json
    //  val contentToEncode = "{\"name\":\"Sheet 2\",\"assemblies\":\"assemblies\",\"inputs\": " + inputs + "}"
       println(contentToEncode)
       Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Created)
    }
  }
  

}