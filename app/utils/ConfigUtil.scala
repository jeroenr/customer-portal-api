package utils

import play.api.Play
import org.apache.commons.lang3.StringUtils

object ConfigUtil {
  def isNotEmpty(key: String) = {
    val EMPTY_STRING = """"""""
    Play.current.configuration.keys.contains(key) && StringUtils.isNotBlank(Play.current.configuration.getString(key).get) && !StringUtils.equals(Play.current.configuration.getString(key).get, EMPTY_STRING)
  }
	def getString(key: String) = Play.current.configuration.getString(key).get
	def getStringOrElse(key: String, default: String) = Play.current.configuration.getString(key).getOrElse(default)
	def getIntOrElse(key: String, default: Int) = Play.current.configuration.getInt(key).getOrElse(default)
	def getBooleanOrElse(key: String, default: Boolean) = Play.current.configuration.getBoolean(key).getOrElse(default)
}