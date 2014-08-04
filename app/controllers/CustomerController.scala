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
  lazy val customerDetailsApi = Api("/customers/{customerId}/details") describedBy "A customer API" withOperations(detailsOperation)


  lazy val listOperation = Operation("listCustomer", GET, "List all customers") takes(
    QueryParam("page.num", String) is Pager.CONSTRAINTS_DESC,
    QueryParam("page.size", String) is Pager.CONSTRAINTS_DESC
    )
  lazy val showOperation = Operation("showCustomer", GET, "Get a customer") takes (
    PathParam("CustomerId", String) is "The customer id"
    )
  lazy val detailsOperation = Operation("showCustomerDetails", GET, "Get customer details") takes (
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

  val ONE_HUNDRED_MB_IN_BYTES = 1024 * 1024 * 100

  def delete(id: Long) = JsonDeleteAction {
    implicit request =>
      Ok
  }

  def list(page: Pager) = JsonGetAction {
    implicit request => {
        Ok(toJson(CustomerService.all(page)))
    }
  }

  def create = JsonPostAction {
    implicit parameterMap => {
      val customer = CustomerService.create("key","secret")
      Created(toJson(customer))
    }
  }

  def update(id: Long) = JsonPostAction {
    implicit parameterMap => {
      Ok
    }
  }

  def byId(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.byId(id) match {
        case Some(customer) => Ok(toJson(customer))
        case _ => NotFound
      }
  }

  def details(id: Long) = JsonGetAction {
    implicit request =>
      CustomerService.byId(id) match {
        case Some(customer) => Ok((toJson(customer).toMapOf[AnyRef] + ("auth_secret" -> customer.auth_secret)).toJson)
        case _ => NotFound
      }
  }

}
