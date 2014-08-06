package models

import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.dsl.fsm.SelectState
import play.api.libs.Codecs
import com.fasterxml.jackson.annotation.JsonIgnore
import utils.ConfigUtil
import java.sql.Timestamp

class BaseEntity extends KeyedEntity[Long] {
    val id: Long = 0
}

trait CreationTimeMonitoring {
    val created_at: Timestamp = new Timestamp(System.currentTimeMillis)
}

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/12/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */

class Customer(
 @Column("name") var name: String,
 @Column("login_name") var login_name: String,
 @Column("password_value") @transient var password_value: String
) extends BaseEntity with CreationTimeMonitoring {
  lazy val keypairs = Library.customerToKeypairs.left(this) 
}

object Customer {

  private val SALT = "?#h|`nBg7qjg [)6pS(8p!RV%$;C@_=b6&b>>?TVj--o)?v[:IEhhkK|nZ2^Z7ag"

  private def fromCustomers(customerQuery: (Customer) => SelectState[Customer]) = from(Library.customers)(customerQuery)

  def all(offset: Int = 0, size: Int = 10) = fromCustomers {
    u =>
      select(u)
  }.page(offset, size)

  def byId(id: Long) = fromCustomers(
    u =>
      where(u.id === id)
      select(u)
  ).headOption

  def byLoginName(name: String) = fromCustomers(
    u =>
      where(u.login_name === name)
      select(u)
  ).headOption

}

class Key(
 @Column("value") var value: String
) extends BaseEntity

object Key {

  private def fromKeys(keyQuery: (Key) => SelectState[Key]) = from(Library.keys)(keyQuery)

  def byValue(value: String) = fromKeys(
    u =>
      where(u.value === value)
      select(u)
  ).headOption

}

class Keypair(
 @Column("auth_key") var auth_key: String,
 @Column("auth_secret") @transient var auth_secret: String,
 @Column("customer_id") var customer_id: Long
) extends BaseEntity with CreationTimeMonitoring

object Keypair {

  private val SALT = "?#h|`nBg7qjg [)6pS(8p!RV%$;C@_=b6&b>>?TVj--o)?v[:IEhhkK|nZ2^Z7ag"

  private def fromKeypairs(keypairQuery: (Keypair) => SelectState[Keypair]) = from(Library.keypairs)(keypairQuery)

  def all(offset: Int = 0, size: Int = 10) = fromKeypairs {
    u =>
      select(u)
  }.page(offset, size)

  def byId(id: Long) = fromKeypairs(
    u =>
      where(u.id === id)
      select(u)
  ).headOption

}

class User(
 @Column("login_name") var login_name: String,
 @Column("password_value") @transient var password_value: String,
 @Column("first_name") var first_name: String,
 @Column("last_name") var last_name: String
) extends BaseEntity with CreationTimeMonitoring {
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

