package controllers

import play.api.mvc.Results._
import org.apache.commons.lang3.StringUtils
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import models.User
import org.joda.time.DateTime
import play.api.mvc._
import scala.concurrent.{Await, Future}
import services.UserService
import scala.concurrent.ExecutionContext.Implicits.global
import utils.ConfigUtil
import play.api.libs.ws.WS
import scala.util.Try
import play.Logger
import utils.ResultImplicits._
import play.api.i18n.{Messages,Lang}
import scala.concurrent.duration._
import scala.util.Failure
import scala.Some
import scala.util.Success
import play.api.mvc.Security.AuthenticatedRequest
import services._
import org.mindrot.jbcrypt.BCrypt
import collection.Map
import org.apache.commons.codec.binary.Base64
import com.nimbusds.jose.crypto._
import com.nimbusds.jwt._
import com.nimbusds.jose._
import java.util.Date

/**
 * Created by jeroen on 1/14/14.
 */
object SecurityController {

   private lazy val basicSt = "basic " 

  val AUTH_USER_KEY = "authorized-user"

  def Authorized[A](action: Action[A]) = Action.async(action.parser){ implicit request =>
    val authHeaders = request.headers.get("Authorization")
    val result = if (authHeaders.isEmpty) {
      Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=\"ChatSavvy\""))
    } else {
      val authResp = decodeBasicAuth(authHeaders.get) match {
        case Some((loginName, password)) => {
          val customer = CustomerService.byLoginName(loginName)
          (customer.isDefined && BCrypt.checkpw(password, customer.get("password_value")), customer)
        }
        case _ => (false, None)
      }
      authResp match {
        case (true, Some(customer: Map[String, String])) =>
          Logger.info(s"User ${customer("login_name")} is authorized for this session")
          action(new AuthenticatedRequest(Some(customer - "password_value"), request))
        case _ =>
          Logger.info(s"Authorization headers ${authHeaders.get} are not accepted")
          Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=\"ChatSavvy\"").asJsonWithAccessControlHeaders)
      }
    }
    result.onComplete {
      case Failure(t) => Logger.error(s"Couldn't complete action ${t.getMessage}", t)
      case Success(r) => Logger.debug(s"All good: $r")
    }
    result
  }

  // private def isNoReplayAttack[A](timestamp: String) = {
  //   val authTime = new DateTime(Try(timestamp.toLong).getOrElse(1L))
  //   val oneHourAgo = DateTime.now.minusHours(1)
  //   authTime.isAfter(oneHourAgo)
  // }

  private def decodeBasicAuth(auth: String): Option[(String, String)] = {
      if (auth.length() < basicSt.length()) {
          return None
      }
      val basicReqSt = auth.substring(0, basicSt.length())
      if (basicReqSt.toLowerCase() != basicSt) {
          return None
      }
      val basicAuthSt = auth.replaceFirst(basicReqSt, "")
      val decoded = Base64.decodeBase64(basicAuthSt)
      val decodedAuthSt = new String(decoded, "UTF-8")
      val usernamePassword = decodedAuthSt.split(":")
      if (usernamePassword.length >= 2) {
          //account for ":" in passwords
          return Some(usernamePassword(0), usernamePassword.splitAt(1)._2.mkString)
      }
      None
  }

  def generateAccessToken(key:String, secret: String, user: String) = {
    val signer = new MACSigner(secret.getBytes)
    val claimsSet = new JWTClaimsSet
    claimsSet.setIssueTime(new Date)
    claimsSet.setIssuer(key)
    claimsSet.setCustomClaim("user", user)
    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
    signedJWT.sign(signer)
    signedJWT.serialize
  }

}
