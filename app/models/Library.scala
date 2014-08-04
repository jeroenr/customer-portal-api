package models

import org.squeryl.Schema

/**
 * Created by jeroen on 2/12/14.
 */

object Library extends Schema {
  val users = table[User]("user")
  val customers = table[Customer]("customer")
}
