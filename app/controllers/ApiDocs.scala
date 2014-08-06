/**
 * Created by jeroen on 1/17/14.
 */
package controllers

import play.api.mvc._
import play.api.libs.json._
import net.eamelink.swaggerkit._
import net.eamelink.swaggerkit.play2.Writers._
import utils.ResultImplicits._

object ApiDocs extends Controller {

  def apiDocumentation(host: String) = ApiDocumentation(
    basePath = "http://" + host + "/api",
    swaggerVersion = "1.1-SNAPSHOT",
    apiVersion = "1",
    apis = List(
      ResourceDeclaration(
        path = "/users.{format}",
        description = "Operations on users",
        resourcePath = "/users",
        basePath = "http://"+host+"/api",
        swaggerVersion = "1.1-SNAPSHOT",
        apiVersion = "1",
        apis = List(
          UserController.usersApi,
          UserController.userApi,
          UserController.usersFieldMappingApi,
          UserController.usersSearchApi
        ),
        models = Map()
      ),
      ResourceDeclaration(
        path = "/customers.{format}",
        description = "Operations on customers",
        resourcePath = "/customers",
        basePath = "http://"+host+"/api",
        swaggerVersion = "1.1-SNAPSHOT",
        apiVersion = "1",
        apis = List(
          CustomerController.meApi,
          CustomerController.customersApi,
          CustomerController.customerApi,
          CustomerController.customerKeypairsApi,
          CustomerController.customerKeypairApi
        ),
        models = Map()
      )
    )
  )

  def ui = Action {
    Redirect("/assets/swagger-ui/index.html")
  }

  def discover = Action { implicit request =>
    Ok(Json.toJson(apiDocumentation(request.host))).asJsonWithAccessControlHeaders
  }

  def resource(id: String) = Action { implicit request =>
    val path = "/%s.{format}" format id
    apiDocumentation(request.host).apis.filter(_.path == path).headOption.map { doc =>
      Ok(Json.toJson(doc)).asJsonWithAccessControlHeaders
    }.getOrElse(NotFound)
  }
}
