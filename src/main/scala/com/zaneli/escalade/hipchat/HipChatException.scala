package com.zaneli.escalade.hipchat

import net.liftweb.json.{ DefaultFormats, parse }
import scalaj.http.HttpException

class HipChatException private (val message: String, val cause: Exception) extends Exception(message, cause)

object HipChatException {
  def apply(cause: HttpException): HipChatException = {
    implicit val formats = DefaultFormats
    val message = (parse(cause.body) \ "error" \ "message").extract[String]
    new HipChatException(s"${cause.code}: ${cause.message} (${message})", cause)
  }
  def apply(message: String): HipChatException = {
    new HipChatException(message, null)
  }
}
