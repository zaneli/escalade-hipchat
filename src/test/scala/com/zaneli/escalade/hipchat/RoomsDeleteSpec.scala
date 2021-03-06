package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class RoomsDeleteSpec extends Specification with TestUtil {

  "rooms/delete" should {
    "call" in {
      val limit = 100L
      val remaining = 99L
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("Deleted", (limit, remaining, reset.getMillis / 1000))

      val (deleted, rate) = rooms.delete.call(7)

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/rooms/delete"
      holder.params must_== Map("room_id" -> "7", "auth_token" -> "token")

      deleted must beTrue

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.delete.call(7) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/delete?auth_test=true" should {
    "test success" in {
      val limit = 100L
      val remaining = 98L
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.delete.test must beSuccessfulTry.which {
        case (result, rate) =>
          result.code must_== 202
          result.authType must_== "Accepted"
          result.message must_== "This auth_token has access to use this method."

          rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.delete.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
