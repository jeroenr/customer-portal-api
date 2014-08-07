package services

import org.squeryl.PrimitiveTypeMode._
import models._
import binders.{Pager}
import scala.util.Random
import scala.annotation.tailrec
import net.fromamsterdamwithlove.json.utils.JsonUtil._
import org.mindrot.jbcrypt.BCrypt

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 12/12/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
object UserService {

  def all(page: Pager) = transaction {
    User.all(page.offset, page.size).toList
  }

  def byLoginName(loginName: String) = transaction {
    User.byLoginName(loginName)
  }

}

object CustomerService {
	def create(name: String, login_name: String, password_value: String) = {
		val key = KeyService.uniqueKey(20)
      	val secret = KeyService.uniqueKey(20)
      	// BCrypt.checkpw(password, passwordHash)
      	val passwordHash = BCrypt.hashpw(password_value, BCrypt.gensalt)
	    val customer = new Customer(name, login_name, passwordHash)
		
		transaction {		
			Library.customers.insert(customer)
			customer.keypairs.associate(new Keypair(key, secret, customer.id)) 
			toJson(customer)
		}
	}

	def delete(id: Long) = transaction {
		Library.customers.delete(id)
	}

	def regenerateKeypair(id: Long) = transaction {
		val key = KeyService.uniqueKey(20)
      	val secret = KeyService.uniqueKey(20)
		val keypair = new Keypair(key, secret, id)
		val customer = Customer.byId(id).get
		customer.keypairs.associate(keypair)
	}

	def all(page: Pager) = transaction {
		toJson(Map("customers" -> Customer.all(page.offset, page.size).toList))
	}

	def byId(id: Long) = transaction {
		Customer.byId(id).map(toJson(_))
	}

	def byLoginName(name: String) = transaction {
		Customer.byLoginName(name).map { c => Map("id" -> c.id.toString, "name" -> c.name, "login_name" -> c.login_name, "password_value" -> c.password_value)}
	}

	def keypairs(id: Long) = transaction {
		Customer.byId(id).map(_.keypairs.map{ kp => Map("id" -> kp.id,"auth_key" -> kp.auth_key, "auth_secret" -> kp.auth_secret, "created_at" -> kp.created_at)}.toList.reverse)
	}

	def deleteKeypair(id: Long, keypair: Long) = transaction {
		val kp = Customer.byId(id).get.keypairs.find(_.id == keypair).get
		Library.keypairs.delete(kp.id)
	}
}

object KeyService {
	@tailrec
	def uniqueKey(length: Int): String = {
		val key = Random.alphanumeric.take(length).mkString
		val existingKey = transaction {
			Key.byValue(key)
		}
		existingKey match {
			case Some(_) => uniqueKey(length)
			case _ => {
				transaction {
					Library.keys.insert(new Key(key))
				}
				key
			}
		}
	}
}