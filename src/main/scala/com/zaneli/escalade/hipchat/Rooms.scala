package com.zaneli.escalade.hipchat

import com.zaneli.escalade.hipchat.model.{ Message, RateLimit, Room }
import com.zaneli.escalade.hipchat.param.{ Color, MessageFormat }
import com.zaneli.escalade.hipchat.util.DataHandler
import net.liftweb.json.{ DefaultFormats, parse }

class Rooms(private[this] val token: String) extends HttpExecutor with DataHandler {

  private[this] val version = "v1"
  private[this] val category = "rooms"

  object create extends AuthClientBase(httpExecute("post") _, "create", version, category, token) {
    def call(name: String, ownerUserId: Int, privateMode: Boolean = false, topic: Option[String] = None, guestAccess: Boolean = false): (Room, RateLimit) = {
      val params = Map(
        "name" -> name,
        "owner_user_id" -> ownerUserId,
        "privacy" -> (if (privateMode) "private" else "public"),
        "topic" -> topic,
        "guest_access" -> bool2Int(guestAccess))
      val (res, rateLimit) = execute(params)
      val room = parse(res).children.map { Room.apply }.head
      (room, rateLimit)
    }
  }

  object delete extends AuthClientBase(httpExecute("post") _, "delete", version, category, token) {
    def call(roomId: Int): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("room_id" -> roomId))
      val deleted = (parse(res) \ "deleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object history extends AuthClientBase(httpExecute("get") _, "history", version, category, token) {
    def call(roomId: Int, date: Option[String] = None, timezone: Option[String] = None): (List[Message], RateLimit) = {
      val (res, rateLimit) = execute(Map("room_id" -> roomId, "date" -> date.getOrElse("recent"), "timezone" -> timezone))
      val rooms = (parse(res) \ "messages").children.map { Message.apply }
      (rooms, rateLimit)
    }
  }

  object list extends AuthClientBase(httpExecute("get") _, "list", version, category, token) {
    def call: (List[Room], RateLimit) = {
      val (res, rateLimit) = execute()
      val rooms = (parse(res) \ "rooms").children.map { Room.apply }
      (rooms, rateLimit)
    }
  }

  object message extends AuthClientBase(httpExecute("post") _, "message", version, category, token) {
    def call(
      roomId: Int, from: String, message: String,
      messageFormat: Option[MessageFormat] = None, notify: Boolean = false, color: Option[Color] = None): (String, RateLimit) = {
      implicit val formats = DefaultFormats

      val params = Map(
        "room_id" -> roomId,
        "from" -> from,
        "message" -> message,
        "message_format" -> messageFormat,
        "notify" -> bool2Int(notify),
        "color" -> color)
      val (res, rateLimit) = execute(params)
      val status = (parse(res) \ "status").extract[String]
      (status, rateLimit)
    }
  }

  object topic extends AuthClientBase(httpExecute("post") _, "topic", version, category, token) {
    def call(
      roomId: Int, topic: String, from: Option[String] = None): (String, RateLimit) = {
      implicit val formats = DefaultFormats

      val params = Map("room_id" -> roomId, "topic" -> topic, "from" -> from)
      val (res, rateLimit) = execute(params)
      val status = (parse(res) \ "status").extract[String]
      (status, rateLimit)
    }
  }

  object show extends AuthClientBase(httpExecute("get") _, "show", version, category, token) {
    def call(roomId: Int): (Room, RateLimit) = {
      val (res, rateLimit) = execute(Map("room_id" -> roomId))
      val room = parse(res).children.map { Room.apply }.head
      (room, rateLimit)
    }
  }
}
