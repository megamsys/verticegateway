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
     // "Correctly do GET  requests with an valid Assemblies ID" ! findByIDApp.succeeds ^
  end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "assemblies/content"

    protected override def bodyToStick: Option[String] = {
    //  val inputs = new AssembliesInputs("9c8281e6.637d8", "tab", "Sheet 2")
     val contentToEncode = "{\"name\":\"Sheet 2\", \"assemblies\":[{ " + 
  " \"name\":\"app_java\"," +
  "\"components\":[{" +
    "\"name\":\"component_java\"," +
    "\"tosca_type\":\"tosca.web.java\"," +
    "\"requirements\":{" +
       "\"host\":\"google517278021527273472\"," +
       "\"dummy\":\"\"" +
    "}," +   
    "\"inputs\":{" +      
      "\"domain\":\"megam.co\"," +
      "\"port\":\"\"," +
      "\"username\":\"\"," +
      "\"password\":\"\"," +      
      "\"version\":\"\"," +
      "\"source\":\"\"," +
      "\"design_inputs\":{" +
             "\"id\":\"8dc70ddc.7238f\"," +
             "\"x\":\"385\"," +
             "\"y\":\"103\"," +
             "\"z\":\"9c8281e6.637d8\"," +
             "\"wires\":[\"\", \"\"]" +
          "}," +   
      "\"service_inputs\":{" +
             "\"dbname\":\"\"," +
             "\"dbpassword\":\"\"" +
          "}" +           
    "}," +
    "\"external_management_resource\":\"url\"," +
    "\"artifacts\":{" +
      "\"artifact_type\":\"tosca type\"," +
      "\"content\":\"\"," +
      "\"requirements\":\"requirement_type\"" +
    "}," +
    "\"related_components\":\"relatedcomponents\"," +
    "\"operations\":{" +
      "\"operation_type\":\"\"," +
      "\"target_resource\":\"\"" +
    "}" +
  "},{" +
    "\"name\":\"component_play\"," +
    "\"tosca_type\":\"tosca.web.play\"," +
    "\"requirements\":{" +
      "\"host\":\"google517278021527273472\"," +
       "\"dummy\":\"\"" +
    "}," +  
    "\"inputs\":{" +
      "\"domain\":\"megam.co\"," +
      "\"port\":\"\"," +
      "\"username\":\"\"," +
      "\"password\":\"\"," +      
      "\"version\":\"\"," +
      "\"source\":\"\"," +
      "\"design_inputs\":{" +
             "\"id\":\"bac8df1e.45372\"," +
             "\"x\":\"385\"," +
             "\"y\":\"217\"," +
             "\"z\":\"9c8281e6.637d8\"," +
             "\"wires\":[\"\", \"\"]" +
          "}," +   
      "\"service_inputs\":{" +
             "\"dbname\":\"\"," +
             "\"dbpassword\":\"\"" +
          "}" +      
    "}," +
    "\"external_management_resource\":\"url\"," +
    "\"artifacts\":{" +
      "\"artifact_type\":\"tosca type\"," +
      "\"content\":\"\"," +
      "\"requirements\":\"requirement_type\"" +
    "}," +
    "\"related_components\":\"relatedcomponents\"," +
    "\"operations\":{" +
      "\"operation_type\":\"\"," +
      "\"target_resource\":\"\"" +
    "}" +
  "}]," +
  "\"policies\":\"policies\"," +
  "\"inputs\":\"\"," +
  "\"operations\":\"\"" +
"},{" +
  "\"name\":\"app_ruby\"," +
  "\"components\":[{" +
    "\"name\":\"component_ruby\"," +
    "\"tosca_type\":\"tosca.web.ruby\"," +
    "\"requirements\":{" +
    "\"host\":\"ec2517276647657832448\"," +
       "\"dummy\":\"\"" +
    "}," +  
    "\"inputs\":{" +
     "\"domain\":\"megam.co\"," +
      "\"port\":\"\"," +
      "\"username\":\"\"," +
      "\"password\":\"\"," +      
      "\"version\":\"\"," +
      "\"source\":\"\"," +
      "\"design_inputs\":{" +
             "\"id\":\"57444c17.a8bbb4\"," +
             "\"x\":\"216\"," +
             "\"y\":\"162\"," +
             "\"z\":\"9c8281e6.637d8\"," +
             "\"wires\":[\"\", \"\"]" +
          "}," +   
      "\"service_inputs\":{" +
             "\"dbname\":\"\"," +
             "\"dbpassword\":\"\"" +
          "}" +        
    "}," +
    "\"external_management_resource\":\"url\"," +
    "\"artifacts\":{" +
      "\"artifact_type\":\"tosca type\"," +
      "\"content\":\"\"," +
      "\"requirements\":\"requirement_type\"" +
    "}," +
    "\"related_components\":\"relatedcomponents\"," +
    "\"operations\":{" +
      "\"operation_type\":\"\"," +
      "\"target_resource\":\"\"" +
    "}" +
  "}]," +
  "\"policies\":\"policies\"," +
  "\"inputs\":\"\"," +
  "\"operations\":\"\"" +
"}],\"inputs\":{\"id\": \"9c8281e6.637d8\", \"assemblies_type\":\"tab\",\"label\" : \"Sheet 2\", \"cloudsettings\":[ "+
 "{ "+
"\"id\":\"f07af88d.0f8508\", "+
"\"cstype\":\"cloudsettings\", "+
"\"cloudsettings\":\"clouddefault510348255477891072\", "+
"\"x\":255, "+
"\"y\":347, "+
"\"z\":\"962485d7.69db78\", "+
"\"wires\":[ "+
"\"c363c3a9.3c9c4\", "+
"\"bca0b279.435f5\", "+
"\"fdf59de8.020a6\" "+
"]"+
"}, "+
"{ "+
"\"id\":\"1e168d54.e1e973\", "+
"\"cstype\":\"cloudsettings\", "+
"\"cloudsettings\":\"clouddefault510348255477891072\", "+
"\"x\":256, "+
"\"y\":519, "+
"\"z\":\"962485d7.69db78\", "+
"\"wires\":[ "+
"\"85e6a00a.7a196\""+
"]"+
"}] "+
"}}" 
 
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
  
  case object findByIDApp extends Context {
    protected override def urlSuffix: String = "assemblies/AMS508915982803140608"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  

}