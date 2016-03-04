package com.zaneli.escalade.hipchat

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

class HipChatException private[hipchat] (val message: String) extends Exception(message)

class HipChatAuthException private[hipchat] (override val message: String) extends HipChatException(message)

object HipChatException {
  def apply(code: Int, body: String): HipChatException = {
    implicit val formats = DefaultFormats
    val errorType = (parse(body) \ "error" \ "type").extract[String]
    val message = (parse(body) \ "error" \ "message").extract[String]
    if (code == 401) {
      new HipChatAuthException(s"$code: $errorType ($message)")
    } else {
      new HipChatException(s"$code: $errorType ($message)")
    }
  }
  def apply(message: String): HipChatException = {
    new HipChatException(message)
  }
}
