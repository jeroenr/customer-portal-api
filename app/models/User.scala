package models

import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.dsl.fsm.SelectState
import play.api.libs.Codecs
import com.fasterxml.jackson.annotation.JsonIgnore
import utils.ConfigUtil


/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/12/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */

class User(
 @Column("login_name") val login_name: String,
 @Column("password_value") @transient val password_value: String,
 @Column("first_name") val first_name: String,
 @Column("last_name") val last_name: String
) extends KeyedEntity[Long] {
  val id: Long = 0 
  def isAuthorized(key: String, timestamp: Long) = Codecs.sha1(timestamp + User.SALT + password_value) == key
}

object User {

  private val SALT = "?#h|`nBg7qjg [)6pS(8p!RV%$;C@_=b6&b>>?TVj--o)?v[:IEhhkK|nZ2^Z7ag"

  private def fromUsers(userQuery: (User) => SelectState[User]) = from(Library.users)(userQuery)

  def all(offset: Int = 0, size: Int = 10) = fromUsers {
    u =>
      select(u)
  }.page(offset, size)

  def byLoginName(loginName: String) = fromUsers(
    u =>
      where(u.login_name === loginName)
      select(u)
  ).headOption


  def isAuthorized(key: String, timestamp: Long, password: String) = Codecs.sha1(timestamp + SALT + password) == key

}



