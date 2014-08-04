package controllers

import play.api.mvc._
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import net.fromamsterdamwithlove.collection.utils.MapImplicits
import net.fromamsterdamwithlove.json.utils.JsonUtil._
import MapImplicits._
import utils.ConfigUtil
import utils.JsonActions
import scala.util.Try
import net.eamelink.swaggerkit._
import net.eamelink.swaggerkit.SimpleTypes._
import services._
import scala.collection.Map
import scala.util.Failure
import net.eamelink.swaggerkit.Operation
import net.eamelink.swaggerkit.Api
import scala.util.Success
import play.api.Logger
import binders.{Period, Pager}
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 7/22/13
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
object CustomerController extends Controller with JsonActions {

  lazy val customersApi = Api("/customers") describedBy "A customers API" withOperations(listOperation, createOperation)
  lazy val customerApi = Api("/customers/{customerId}") describedBy "A customer API" withOperations(showOperation, updateOperation, deleteOperation)
  lazy val customerKeypairApi = Api("/customers/{customerId}/keypair") describedBy "A customer keypair API" withOperations(keypairOperation, regenerateKeypairOperation)


  lazy val listOperation = Operation("listCustomer", GET, "List all customers") takes(
    QueryParam("page.num", String) is Pager.CONSTRAINTS_DESC,
    QueryParam("page.size", String) is Pager.CONSTRAINTS_DESC
    )
  lazy val showOperation = Operation("showCustomer", GET, "Get a customer") takes (
    PathParam("CustomerId", String) is "The customer id"
    )
  lazy val keypairOperation = Operation("showCustomerKeypair", GET, "Get customer keypair") takes (
    PathParam("CustomerId", String) is "The customer id"
    )

  lazy val regenerateKeypairOperation = Operation("regenerateCustomerKeypair", PUT, "Regenerate customer keypair") takes (
    PathParam("CustomerId", String) is "The customer id"
    )
  lazy val createOperation = Operation("createOrUpdateCustomer", POST, "Create a customer") takes(
    BodyParam(String)
    )
  lazy val deleteOperation = Operation("deleteCustomer", DELETE, "Delete a customer") takes(
    PathParam("customerId", String) is "The customer id"
    )
  lazy val updateOperation = Operation("createOrUpdateCustomer", PUT, "Update a customer") takes(
    PathParam("customerId", String) is "The customer id",
    BodyParam(String)
    )

  def delete(id: Long) = JsonDeleteAction {
    implicit request =>
      CustomerService.delete(id)
      Ok
  }

  def list(page: Pager) = JsonGetAction {
    implicit request => {
        Ok(toJson(CustomerService.all(page)))
    }
  }

  def create = JsonPostAction {
    implicit parameterMap => {
      val key = KeyService.uniqueKey(20)
      val secret = KeyService.uniqueKey(20)
      val customer = CustomerService.create(parameterMap("name").asInstanceOf[String], key, secret)
      Created(toJson(customer))
    }
  }

  def byId(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.byId(id) match {
        case Some(customer) => Ok(toJson(customer))
        case _ => NotFound
      }
  }

  def keypair(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.byId(id) match {
        case Some(customer) => Ok((toJson(customer).toMapOf[AnyRef] + ("auth_secret" -> customer.auth_secret)).toJson)
        case _ => NotFound
      }
  }

  def regenerateKeypair(id: Long) = JsonGetAction {
    implicit request =>
      val key = KeyService.uniqueKey(20)
      val secret = KeyService.uniqueKey(20)
      val isRegenerated = Try(CustomerService.update(id, key, secret))
      isRegenerated match {
        case Success(_) => Ok
        case Failure(f) => 
          Logger.error(s"Couldn't update customer [$id]", f)
          NotFound(wrapExceptionInJson(f))
      }
  }

}
