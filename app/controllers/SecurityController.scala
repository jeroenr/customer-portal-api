package controllers

import play.api.mvc.Results._
import org.apache.commons.lang3.StringUtils
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import models.User
import org.joda.time.DateTime
import play.api.mvc._
import scala.concurrent.Future
import services.UserService
import scala.concurrent.ExecutionContext.Implicits.global
import utils.ConfigUtil
import play.api.libs.ws.WS
import scala.util.{Try, Success, Failure}
import play.Logger
import utils.JsonActions._
import utils.ResultImplicits._
import play.api.i18n.{Messages,Lang}

/**
 * Created by jeroen on 1/14/14.
 */
object SecurityController {
  private val API_TOKEN = ConfigUtil.getStringOrElse("api.token", "b7c9dd7ba726d4fa")

  val AUTH_USER_KEY = "authorized-user"

  def Authorized[A](action: Action[A]) = Action.async(action.parser){ implicit request =>
    action(request)
//      val authHeaders = request.headers.get("Authorization")
//      val result = if (authHeaders.isEmpty) {
//        Future.successful(Unauthorized(Map(
//          "message" -> Messages("login.first")(request.acceptLanguages.headOption.getOrElse(Lang("en"))),
//          "loginurl" -> s"/login",
//          "authorizeuri" -> "/api/authorize"
//        ).toJson).asJsonWithAccessControlHeaders)
//      } else {
//        val authResp = authHeaders.head.split(" ").drop(1).map(StringUtils.substringBetween(_, "\"", "\"")).toList match {
//          case token :: timestamp :: Nil =>
//            (isNoReplayAttack(timestamp) && User.isAuthorized(token, timestamp.toLong, API_TOKEN), None)
//          // user authentication
//          case token :: timestamp :: loginName :: Nil => {
//            val user = UserService.byLoginName(loginName)
//            (user.isDefined && isNoReplayAttack(timestamp) && user.get.isAuthorized(token, timestamp.toLong), user)
//          }
//          case _ => (false, None)
//        }
//        authResp match {
//          case (true, Some(user: User)) =>
//            Logger.info(s"User ${user.loginname} is authorized for this session")
//            action(request).map(_.withSession(AUTH_USER_KEY -> user.loginname))
//          case (true, _) =>
//            Logger.info("Request authorized")
//            action(request)
//          case _ =>
//            Logger.info(s"Authorization headers ${authHeaders.get} are not accepted")
//            Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> "Bearer Realm='SecurityController'").asJsonWithAccessControlHeaders)
//        }
//      }
//    result.onComplete {
//      case Failure(t) => Logger.error(s"Couldn't complete action ${t.getMessage}", t)
//      case Success(r) => Logger.debug(s"All good: $r")
//    }
//    result
  }

  private def isNoReplayAttack[A](timestamp: String) = {
    val authTime = new DateTime(Try(timestamp.toLong).getOrElse(1L))
    val oneHourAgo = DateTime.now.minusHours(1)
    authTime.isAfter(oneHourAgo)
  }

}
