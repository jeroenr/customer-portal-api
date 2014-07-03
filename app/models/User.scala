package models

import org.squeryl.Schema
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

/*
+----------------------+--------------+------+-----+---------+-------+
| ID                   | int(11)      | NO   | PRI | NULL    |       |
| SYSTEM               | int(11)      | YES  |     | NULL    |       |
| DATE_CREATED         | datetime     | YES  |     | NULL    |       |
| DATE_MODIFIED        | datetime     | YES  |     | NULL    |       |
| CREATED_BY           | varchar(50)  | YES  |     | NULL    |       |
| MODIFIED_BY          | varchar(50)  | YES  |     | NULL    |       |
| ENTITY_CLASS         | varchar(100) | YES  |     | NULL    |       |
| ARCHIVED             | int(11)      | NO   |     | NULL    |       |
| ARCHIVED_BY          | varchar(50)  | YES  |     | NULL    |       |
| DATE_ARCHIVED        | datetime     | YES  |     | NULL    |       |
| LAST_MODIFICATION_NR | bigint(20)   | NO   |     | NULL    |       |
| ENABLED              | int(11)      | YES  |     | NULL    |       |
| ACL_ID               | int(11)      | NO   | MUL | NULL    |       |
| ORG_CONTEXT          | varchar(254) | YES  | MUL | NULL    |       |
| ORG_CONTEXT_ID       | int(11)      | YES  | MUL | NULL    |       |
| ACL_ORG_ID           | varchar(100) | YES  | MUL | NULL    |       |
| NR                   | varchar(50)  | YES  | UNI | NULL    |       |
| NAME                 | varchar(254) | YES  |     | NULL    |       |
| SEQ_NR               | bigint(20)   | YES  |     | NULL    |       |
| LIFECYCLE_ID         | int(11)      | YES  |     | NULL    |       |
| STATE_ID             | int(11)      | YES  |     | NULL    |       |
| PREVIOUS_STATE_ID    | int(11)      | YES  |     | NULL    |       |
| TRANSITION_DATE      | datetime     | YES  |     | NULL    |       |
| LOGIN_NAME           | varchar(50)  | YES  | UNI | NULL    |       |
| PASSWORD_VALUE       | varchar(50)  | YES  |     | NULL    |       |
| FIRST_NAME           | varchar(50)  | YES  |     | NULL    |       |
| LAST_NAME            | varchar(50)  | YES  |     | NULL    |       |
| EMAIL                | varchar(100) | YES  |     | NULL    |       |
| CONFIRM_VALUE        | varchar(254) | YES  |     | NULL    |       |
| SCQ_ENABLED_ON       | datetime     | YES  |     | NULL    |       |
| PCQ_ENABLED_ON       | datetime     | YES  |     | NULL    |       |
| LOTO_ENABLED_ON      | datetime     | YES  |     | NULL    |       |
| CHQ_ENABLED_ON       | datetime     | YES  |     | NULL    |       |
| ICQ_ENABLED_ON       | datetime     | YES  |     | NULL    |       |
| MODIFIED             | datetime     | YES  |     | NULL    |       |
| CREATED              | datetime     | YES  |     | NULL    |       |
| LAST_LOGIN           | datetime     | YES  |     | NULL    |       |
+----------------------+--------------+------+-----+---------+-------+

*/
case class User(
                 @Column("ID") id: Int,
                 @Column("LOGIN_NAME") loginname: String,
                 @Column("PASSWORD_VALUE") @transient password_value: String,
                 @Column("FIRST_NAME") firstname: String,
                 @Column("LAST_NAME") lastname: String
                 ) {
  def isAuthorized(key: String, timestamp: Long) = Codecs.sha1(timestamp + User.SALT + password_value) == key
}

object User {

  private val SALT = "?#h|`nBg7qjg [)6pS(8p!RV%$;C@_=b6&b>>?TVj--o)?v[:IEhhkK|nZ2^Z7ag"

  private def fromUsers(userQuery: (User) => SelectState[User]) = from(Library.users)(userQuery)

  def all(offset: Int = 0, size: Int = 10) = fromUsers {
    u =>
      select(u)
  }.page(offset, size)

  def byLoginName(loginName: String) = fromUsers(
    u =>
      where(u.loginname === loginName)
      select(u)
  ).headOption


  def isAuthorized(key: String, timestamp: Long, password: String) = Codecs.sha1(timestamp + SALT + password) == key

}



