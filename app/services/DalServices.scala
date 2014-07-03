package services

import org.squeryl.PrimitiveTypeMode._
import models.{User}
import binders.{Pager}

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