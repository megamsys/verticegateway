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
import Scalaz._
import scalaz.effect.IO
import com.stackmob.scaliak._
import org.slf4j.LoggerFactory
import play.api._
import play.api.mvc._
import models._
import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import play.api.libs.json.Json
import play.api.libs.json.JsString

/**
 * @author ram
 *
 */

object Nodes {

  /*
     * create the riak source 
     */
  def create()(implicit source: ScaliakClient): ScaliakClient = {
    val client = source
    client
  }

  /*
   * connect the existing bucket in riak
   */
  def bucketCreate(source: ScaliakClient, bucketName: String): ScaliakBucket = {
    val bucket = DomainObjects.bucketCreate(source, bucketName)
    bucket
  }

  /*
   * fetch the object using their key from bucket
   */
  def findById(bucketName: String, key: String)(implicit source: ScaliakClient): Option[Domain] = {
    val bucket = bucketCreate(source, bucketName)
    val fetch = DomainObjects.fetch(bucket, key)
    println("Fetched Value.............:" + fetch)
    fetch
  }

  /*
   * put the value in riak bucket
   */
  def put(bucketName: String, key: String, value: String)(implicit source: ScaliakClient) {
    val bucket = bucketCreate(source, bucketName)
    DomainObjects.put(source, bucket, key, value)
    val fetch = DomainObjects.fetch(bucket, key)
  }
}

