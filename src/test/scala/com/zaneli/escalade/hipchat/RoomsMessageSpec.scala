package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.{ DateTime, DateTimeZone }
import com.zaneli.escalade.hipchat.model.RateLimit
import com.zaneli.escalade.hipchat.param.{ Color, MessageFormat }
import org.specs2.mutable.Specification

class RoomsMessageSpec extends Specification with TestUtil {

  "rooms/message" should {
    "call (min params)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("StatusSent", (limit, remaining, reset.getMillis / 1000))

      val (status, rate) = rooms.message.call(7, "user1", "Hello!")

      holder.method must_== "post"
      holder.path must_== "v1/rooms/message"
      holder.params must_== Map("room_id" -> "7", "from" -> "user1", "message" -> "Hello!", "notify" -> "0", "auth_token" -> "token")

      status must_== "sent"

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, rooms) = mockRooms("StatusSent", (100, 98, DateTime.now.getMillis / 1000))

      rooms.message.call(7, "user2", "Goodbye!", Some(MessageFormat.Text), true, Some(Color.Green))

      holder.method must_== "post"
      holder.path must_== "v1/rooms/message"
      holder.params must_== Map(
        "room_id" -> "7", "from" -> "user2", "message" -> "Goodbye!", "message_format" -> "text", "notify" -> "1", "color" -> "green", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.message.call(7, "user1", "Hello!") must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/message?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 97
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.message.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.message.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
