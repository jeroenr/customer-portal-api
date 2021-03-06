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
import play.api.mvc.Security.AuthenticatedRequest

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 7/22/13
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
object CustomerController extends Controller with JsonActions {

  lazy val customersApi = Api("/customers") describedBy "A customers API" withOperations(listOperation, createOperation)
  lazy val meApi = Api("/customers/me") describedBy "A me API" withOperations (meOperation)
  lazy val customerApi = Api("/customers/{customerId}") describedBy "A customer API" withOperations(showOperation, updateOperation, deleteOperation)
  lazy val customerKeypairsApi = Api("/customers/{customerId}/keypairs") describedBy "A customer keypairs API" withOperations(keypairOperation, regenerateKeypairOperation)
  lazy val customerKeypairApi = Api("/customers/{customerId}/keypairs/{keypairId}") describedBy "A customer keypair API" withOperations(deleteKeypairOperation)

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

  lazy val meOperation = Operation("me", GET, "Get the currently logged in customer")

  lazy val regenerateKeypairOperation = Operation("regenerateCustomerKeypair", POST, "Regenerate customer keypair") takes (
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

  lazy val deleteKeypairOperation = Operation("deleteKeypair", DELETE, "Delete a keypair") takes(
    PathParam("customerId", String) is "The customer id",
    PathParam("keypairId", String) is "The keypair id"
    )

  def delete(id: Long) = JsonDeleteAction {
    implicit request =>
      CustomerService.delete(id)
      Ok
  }

  def list(page: Pager) = JsonGetAction {
    implicit request => {
        Ok(CustomerService.all(page))
    }
  }

  def create = JsonPostAction("customer-create") {
    implicit parameterMap => {
      val stringParams = parameterMap.asInstanceOf[Map[String, String]]
      val customer = CustomerService.create(stringParams("name"), stringParams("login_name"), stringParams("password"))
      Created(customer)
    }
  }

  def byId(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.byId(id) match {
        case Some(customer) => Ok(customer)
        case _ => NotFound
      }
  }

   def me = JsonGetAction {
    implicit request =>
      request.asInstanceOf[AuthenticatedRequest[AnyContent, Option[Map[String, String]]]].user match {
        case Some(customer) => Ok(toJson(customer))
        case _ => NoContent
      }
  }

  def keypairs(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.keypairs(id) match {
        case Some(keypairs) => Ok(Map("keypairs" -> keypairs).toJson)
        case _ => NotFound
      }
  }

  def createKeypair(id: Long) = JsonGetAction {
    implicit request =>
      val isRegenerated = Try(CustomerService.regenerateKeypair(id))
      isRegenerated match {
        case Success(_) => Ok
        case Failure(f) => 
          Logger.error(s"Couldn't update customer [$id]", f)
          NotFound(wrapExceptionInJson(f))
      }
  }

  def deleteKeypair(id: Long, keypair: Long) = JsonDeleteAction {
    implicit request =>
      Try(CustomerService.deleteKeypair(id, keypair)) match {
        case Success(true) => Ok
        case Success(_) => Accepted
        case Failure(f) => 
          Logger.error(s"Couldn't delete keypair [$keypair]", f)
          NotFound(wrapExceptionInJson(f))
      }
  }

}
