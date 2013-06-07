/* 
** Copyright [2012] [Megam Systems]
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
import play.api._
import play.api.Logger
import play.api.mvc._
import models._


/**
 * @author rajthilak
 *
 */

object Accounts {

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
  def findById(source: ScaliakClient, bucketName: String, key: String): Option[Domain] = {
    val bucket = bucketCreate(source, bucketName)
    val fetch = DomainObjects.fetch(bucket, key)
    Logger.debug("Fetched (%s) => %s".format(bucketName, fetch))
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