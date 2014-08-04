package models

import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.dsl.fsm.SelectState
import play.api.libs.Codecs
import com.fasterxml.jackson.annotation.JsonIgnore
import utils.ConfigUtil


/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/12/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */

class Key(
 @Column("value") val value: String
) extends KeyedEntity[Long] {
  val id: Long = 0 
}

object Key {

  private def fromKeys(keyQuery: (Key) => SelectState[Key]) = from(Library.keys)(keyQuery)

  def byValue(value: String) = fromKeys(
    u =>
      where(u.value === value)
      select(u)
  ).headOption

}



