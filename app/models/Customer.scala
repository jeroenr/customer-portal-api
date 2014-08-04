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

class Customer(
 @Column("name") var name: String,
 @Column("auth_key") var auth_key: String,
 @Column("auth_secret") @transient var auth_secret: String
) extends KeyedEntity[Long] {
  val id: Long = 0 
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

  def byAuthKey(authKey: String) = fromCustomers(
    u =>
      where(u.auth_key === authKey)
      select(u)
  ).headOption

  def deleteByAuthKey(authKey: String) = 
    Library.customers.deleteWhere(
      c =>
        c.auth_key === authKey
    )

}



