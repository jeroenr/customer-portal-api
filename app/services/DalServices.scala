package services

import org.squeryl.PrimitiveTypeMode._
import models.{User, Customer, Key, Library}
import binders.{Pager}
import scala.util.Random
import scala.annotation.tailrec

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
	def create(name: String, key: String, secret: String) = { 
		val customer = new Customer(name, key, secret)
		transaction {
			Library.customers.insert(customer)
		}
		customer
	}

	def all(page: Pager) = transaction {
		Customer.all(page.offset, page.size).toList
	}

	def byId(id: Long) = transaction {
		Customer.byId(id)
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
			case _ => key
		}
	}
}