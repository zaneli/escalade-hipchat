package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.{ DateTime, DateTimeZone }
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class RoomsTopicSpec extends Specification with TestUtil {

  "rooms/topic" should {
    "call (min params)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("StatusOk", (limit, remaining, reset.getMillis / 1000))

      val (status, rate) = rooms.topic.call(7, "Topic")

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/rooms/topic"
      holder.params must_== Map("room_id" -> "7", "topic" -> "Topic", "auth_token" -> "token")

      status must_== "ok"

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, rooms) = mockRooms("StatusSent", (100, 98, DateTime.now.getMillis / 1000))

      rooms.topic.call(7, "Topic", Some("escalade-hipchat"))

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/rooms/topic"
      holder.params must_== Map("room_id" -> "7", "topic" -> "Topic", "from" -> "escalade-hipchat", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.topic.call(7, "Topic") must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/topic?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 97
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.topic.test must beSuccessfulTry.which {
        case (result, rate) =>
          result.code must_== 202
          result.authType must_== "Accepted"
          result.message must_== "This auth_token has access to use this method."

          rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.topic.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
