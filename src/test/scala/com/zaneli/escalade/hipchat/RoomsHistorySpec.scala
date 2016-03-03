package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.{ DateTime, DateTimeZone }
import com.zaneli.escalade.hipchat.model.{ File, RateLimit, UserIdentifier }
import org.specs2.mutable.Specification

class RoomsHistorySpec extends Specification with TestUtil {

  "rooms/history" should {
    "call (min params)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("Messages", (limit, remaining, reset.getMillis / 1000))

      val (messages, rate) = rooms.history.call(7)

      holder.method must_== "get"
      holder.url must_== "http://api.hipchat.com/v1/rooms/history"
      holder.params must_== Map("room_id" -> "7", "date" -> "recent", "auth_token" -> "token")

      messages must have size 3

      {
        val message = messages(0)
        message.date.withZone(DateTimeZone.UTC) must_== new DateTime(2010, 11, 19, 15, 48, 19, DateTimeZone.forID("US/Pacific")).withZone(DateTimeZone.UTC)
        message.from must_== UserIdentifier(Right(10), "Garret Heaton")
        message.message must_== "Good morning! This is a regular message."
        message.file must beNone
      }
      {
        val message = messages(1)
        message.date.withZone(DateTimeZone.UTC) must_== new DateTime(2010, 11, 19, 15, 49, 44, DateTimeZone.forID("US/Pacific")).withZone(DateTimeZone.UTC)
        message.from must_== UserIdentifier(Right(10), "Garret Heaton")
        message.message must_== "This is a file upload"
        message.file must beSome(File("Screenshot.png", 141909, "http://uploads.hipchat.com/xxx/Screenshot.png"))
      }
      {
        val message = messages(2)
        message.date.withZone(DateTimeZone.UTC) must_== new DateTime(2010, 11, 19, 16, 13, 40, DateTimeZone.forID("US/Pacific")).withZone(DateTimeZone.UTC)
        message.from must_== UserIdentifier(Left("api"), "Deploy Bot")
        message.message must_== "This message is sent via the API so the user_id is 'api'."
        message.file must beNone
      }

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, rooms) = mockRooms("Room", (100, 98, DateTime.now.getMillis / 1000))

      rooms.history.call(7, Some((2013, 12, 1)), Some("Asia/Tokyo"))

      holder.method must_== "get"
      holder.url must_== "http://api.hipchat.com/v1/rooms/history"
      holder.params must_== Map("room_id" -> "7", "date" -> "2013-12-01", "timezone" -> "Asia/Tokyo", "auth_token" -> "token")
    }

    "call (invalid date)" in {
      val (_, rooms) = mockRooms("Room", (100, 98, DateTime.now.getMillis / 1000))

      rooms.history.call(7, Some((2013, 13, 1))) must throwA[HipChatException]("""\QInvalid date: Set (YYYY, MM, DD).\E""")
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.history.call(7) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/history?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 97
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.history.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.history.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
