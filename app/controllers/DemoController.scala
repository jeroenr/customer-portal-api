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
object DemoController extends Controller with JsonActions {

  def generateDemoToken(user: String) = JsonGetAction {
    implicit request =>
      val key = "RFoHfODnI4wilFZGrhdo"
      val secret = "CdvFNGpjG3qQKp5glHWt"
      Ok(
        Map(
          "auth_id" -> 1,
          "auth_key" -> key,
          "user_id" -> user,
          "access_token" -> SecurityController.generateAccessToken(key, secret, user) 
        ).toJson
      )
  }
  

}
