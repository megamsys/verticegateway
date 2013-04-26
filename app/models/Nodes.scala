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

import scalikejdbc._
import scalikejdbc.SQLInterpolation._

/**
 * @author ram
 *
 */

case class Node(id: Int, email: String, password: String, name: String, permission: Permission)

object Nodes {

  val * = { rs: WrappedResultSet =>
    Node(
      id = rs.int("id"),
      email = rs.string("email"),
      password = rs.string("password"),
      name = rs.string("name"),
      permission = Permission.valueOf(rs.string("permission")))
  }

  def authenticate(email: String, password: String): Option[Node] = {
  //  findByEmail(email).filter { account => BCrypt.checkpw(password, account.password) }
   findByEmail(email).filter { account => true }

  }

  def findByEmail(email: String): Option[Node] = {
    DB localTx { implicit s =>
      sql"SELECT * FROM account WHERE email = ${email}".map(*).single.apply()
    }
  }

  def findById(id: Int): Option[Node] = {
    DB localTx { implicit s =>
      sql"SELECT * FROM account WHERE id = ${id}".map(*).single.apply()
    }
  }

  def findAll: Seq[Node] = {
    DB localTx { implicit s =>
      sql"SELECT * FROM account".map(*).list.apply()
    }
  }

  def create(account: Node) {
    DB localTx { implicit s =>
      import account._
      //val pass = BCrypt.hashpw(account.password, BCrypt.gensalt())
      val pass = ""
      sql"INSERT INTO account VALUES (${id}, ${email}, ${pass}, ${name}, ${permission.toString})".update.apply()
    }
  }
}