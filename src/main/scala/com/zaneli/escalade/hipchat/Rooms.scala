package com.zaneli.escalade.hipchat

import com.zaneli.escalade.hipchat.model.{ Message, RateLimit, Room }
import com.zaneli.escalade.hipchat.param.{ Color, MessageFormat }
import com.zaneli.escalade.hipchat.util.DataHandler
import net.liftweb.json.{ DefaultFormats, parse }

class Rooms(private[this] val token: String) extends HttpExecutor with DataHandler {

  private[this] val version = "v1"
  private[this] val category = "rooms"

  object create extends AuthClientBase(httpExecute("post") _, "create", version, category, token) {
    /**
     * Creates a new room.
     *
     * @param name Name of the room.
     * @param ownerUserId User ID of the room's owner.
     * @param privateMode Privacy.
     * @param topic Initial room topic.
     * @param guestAccess Whether or not to enable guest access for this room.
     * @return Room info and Rate Limiting info
     */
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
    /**
     * Deletes a room and kicks the current participants.
     *
     * @param roomId ID of the room.
     * @return Delete result and Rate Limiting info
     */
    def call(roomId: Int): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("room_id" -> roomId))
      val deleted = (parse(res) \ "deleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object history extends AuthClientBase(httpExecute("get") _, "history", version, category, token) {
    /**
     * Fetch chat history for this room.
     *
     * @param roomId ID of the room.
     * @param date Date to fetch history for in YYYY-MM-DD format. If no value set, fetch the latest 75 messages.
     * @param timezone Your timezone. Must be a supported timezone(https://www.hipchat.com/docs/api/timezones).
     * @return Messages info and Rate Limiting info
     */
    def call(roomId: Int, date: Option[String] = None, timezone: Option[String] = None): (List[Message], RateLimit) = {
      val (res, rateLimit) = execute(Map("room_id" -> roomId, "date" -> date.getOrElse("recent"), "timezone" -> timezone))
      val rooms = (parse(res) \ "messages").children.map { Message.apply }
      (rooms, rateLimit)
    }
  }

  object list extends AuthClientBase(httpExecute("get") _, "list", version, category, token) {
    /**
     * List rooms for this group.
     *
     * @return Rooms info and Rate Limiting info
     */
    def call: (List[Room], RateLimit) = {
      val (res, rateLimit) = execute()
      val rooms = (parse(res) \ "rooms").children.map { Room.apply }
      (rooms, rateLimit)
    }
  }

  object message extends AuthClientBase(httpExecute("post") _, "message", version, category, token) {
    /**
     * Send a message to a room.
     *
     * @param roomId ID of the room.
     * @param from Name the message will appear be sent from.
     * @param message The message body.
     * @param messageFormat Determines how the message is treated by our server and rendered inside HipChat applications.
     * @param notify Whether or not this message should trigger a notification for people in the room.
     * @param color Background color for message.
     * @return Sent result and Rate Limiting info
     */
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
    /**
     * Set a room's topic.
     *
     * @param roomId ID of the room.
     * @param topic The topic body.
     * @param from Name of the service changing the topic.
     * @return Set result and Rate Limiting info
     */
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
    /**
     * Get room details.
     *
     * @param roomId ID of the room.
     * @return Room info and Rate Limiting info
     */
    def call(roomId: Int): (Room, RateLimit) = {
      val (res, rateLimit) = execute(Map("room_id" -> roomId))
      val room = parse(res).children.map { Room.apply }.head
      (room, rateLimit)
    }
  }
}
