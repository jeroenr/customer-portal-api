package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

/**
 * Created by jeroen on 2/12/14.
 */

object Library extends Schema {
  val users = table[User]("user")
  val customers = table[Customer]("customer")
  val keys = table[Key]("key_register")
  val keypairs = table[Keypair]("keypair")

  val customerToKeypairs = oneToManyRelation(customers, keypairs).via((c,k) => c.id === k.customer_id)
}
