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
import com.basho.riak.client.raw.http.HTTPClientAdapter
import play.api.libs.json.Json
import play.api.libs.json.JsString
/**
 * @author ram
 *
 */
case class Domain(key: String, value: String)
object DomainObjects {

  implicit val domainConverter: ScaliakConverter[Domain] = ScaliakConverter.newConverter[Domain](
    (o: ReadObject) => new Domain(o.key, o.stringValue).successNel,
    (o: Domain) => WriteObject(o.key, o.value.getBytes))

  private lazy val logger = LoggerFactory.getLogger(getClass)

  //Create the scaliak client from riak
  def clientCreate(): ScaliakClient = {
    val client = Scaliak.httpClient("http://127.0.0.1:8098/riak")
    client
  }

  /*
   * connect the existing bucket in riak client
   * if doesn't bucket then to create the new bucket
   */
  def bucketCreate(client: ScaliakClient, bucketName: String): ScaliakBucket = {
    client.generateAndSetClientId()
    val bucket = client.bucket(bucketName).unsafePerformIO() match {
      case Success(b) => b
      case Failure(e) => throw e
    }
    bucket
  }

  /*
   * put the specified key and their value to riak bucket
   */
  def put(client: ScaliakClient, bucket: ScaliakBucket, key: String, value: String) {
    // store a domain object   
    if (bucket.store(new Domain(key, value)).unsafePerformIO().isFailure) {
      throw new Exception("failed to store object")
    }
  }

  /*
   * fetch a domain object
   * and return the option node object
   */
  def fetch(bucket: ScaliakBucket, key: String): Option[Domain] = {
    val fetchResult: ValidationNel[Throwable, Option[Domain]] = bucket.fetch(key).unsafePerformIO()
    fetchResult match {
      case Success(mbFetched) => {
        logger.debug(mbFetched some { v => v.key + ":" + v.value } none { "did not find key" })
        mbFetched
      }
      case Failure(es) => throw es.head
    }
  }

  def printFetchRes(v: ValidationNel[Throwable, Option[Domain]]): IO[Unit] = v match {
    case Success(mbFetched) => {
      logger.debug(
        mbFetched some { "fetched: " + _.toString } none { "key does not exist" }).pure[IO]
    }
    case Failure(es) => {
      (es.foreach(e => logger.warn(e.getMessage))).pure[IO]
    }
  }

}