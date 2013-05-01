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
/**
 * @author ram
 *
 */


case class SomeDomainObject(key: String, value: String)
object SomeDomainObject {

  implicit val domainConverter: ScaliakConverter[SomeDomainObject] = ScaliakConverter.newConverter[SomeDomainObject](
    (o: ReadObject) => new SomeDomainObject(o.key, o.stringValue).successNel,
    (o: SomeDomainObject) => WriteObject(o.key, o.value.getBytes)
  )

}

object DomainObjects  {
  import SomeDomainObject._ // put the implicits at a higher priority scope

  private lazy val logger = LoggerFactory.getLogger(getClass)

  val client = Scaliak.httpClient("http://127.0.0.1:8098/riak")
  client.generateAndSetClientId()

  val bucket = client.bucket("scaliak-example").unsafePerformIO() match {
    case Success(b) => b
    case Failure(e) => throw e
  }
  
  // store a domain object
  val key = "some-key"
  if (bucket.store(new SomeDomainObject(key, "value")).unsafePerformIO().isFailure) {
    throw new Exception("failed to store object")
  }
  
  // fetch a domain object
  val fetchResult: ValidationNel[Throwable, Option[SomeDomainObject]] = bucket.fetch(key).unsafePerformIO()
  fetchResult match {
    case Success(mbFetched) => {
      logger.debug(mbFetched some { v => v.key + ":" + v.value } none { "did not find key" })
    }
    case Failure(es) => throw es.head
  }

  def printFetchRes(v: ValidationNel[Throwable, Option[SomeDomainObject]]): IO[Unit] = v match {
    case Success(mbFetched) => {
      logger.debug(
        mbFetched some { "fetched: " + _.toString } none { "key does not exist" }
      ).pure[IO]
    }
    case Failure(es) => {
      (es.foreach(e => logger.warn(e.getMessage))).pure[IO]
    }
  }    

  // taking advantage of the IO monad
  val action = bucket.fetch(key) flatMap { r =>
    (r.toOption | none) some { obj =>
      for {
        _ <- printFetchRes(r)
        _ <- bucket.delete(obj)
        _ <- logger.debug("deleted").pure[IO]
      } yield ()
    } none {
      logger.debug("no object to delete").pure[IO]
    }
  }
  
  action.unsafePerformIO()


def authenticate(email: String): Result = {   
    Ok("dfjhkj")   

  }
}