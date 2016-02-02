/*
 ** Copyright [2013-2016] [Megam Systems]
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
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.tosca._
import models.json.tosca.carton._
import controllers.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.util.Time
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

//for our 1.0 rewrite of gwy.
trait CRUD[I,O] {

  /**
   * performs a Store
   * @param input the input body against which to perform the request
   * @return the {{{O}}} that can execute the request with the given parameters
   */
  def create(input: I): O

  /**
   * performs a Fetch by email
   * @param input the input email against which to perform the request
   * @return the {{{O}}} that can execute the request with the given parameters
   */
  def get(input: I): O

  /**
   * performs a Fetch by id
   * @param id the id against which to perform the request
   * @return the {{{O}}} that can execute the request with the given parameters
   */
  def getById(id: I): O

}
