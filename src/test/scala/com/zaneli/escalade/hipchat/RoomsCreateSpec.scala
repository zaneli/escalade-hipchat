package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class RoomsCreateSpec extends Specification with TestUtil {

  "rooms/create" should {
    "call (min params)" in {
      val limit = 100L
      val remaining = 99L
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("CreatedRoom", (limit, remaining, reset.getMillis / 1000))

      val (room, rate) = rooms.create.call("Development", 5)

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/rooms/create"
      holder.params must_== Map("name" -> "Development", "owner_user_id" -> "5", "privacy" -> "public", "guest_access" -> "0", "auth_token" -> "token")

      room.roomId must_== 7
      room.name must_== "Development"
      room.topic must beEmpty
      room.lastActive must beNone
      room.created must_== new DateTime(1269010311L * 1000)
      room.ownerUserId must_== 5
      room.isArchived must beFalse
      room.isPrivate must beFalse
      room.privacy must beNone
      room.xmppJid must_== "7_development@conf.hipchat.com"
      room.memberUserIds must beEmpty
      room.participants must beEmpty
      room.guestAccessUrl must beNone

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, rooms) = mockRooms("CreatedRoom", (100, 98, DateTime.now().getMillis / 1000))

      rooms.create.call("Development", 5, true, Some("topic"), true)

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/rooms/create"
      holder.params must_== Map(
        "name" -> "Development", "owner_user_id" -> "5", "privacy" -> "private", "topic" -> "topic", "guest_access" -> "1", "auth_token" -> "token"
      )
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.create.call("Development", 5) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/create?auth_test=true" should {
    "test success" in {
      val limit = 100L
      val remaining = 97L
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.create.test must beSuccessfulTry.which {
        case (result, rate) =>
          result.code must_== 202
          result.authType must_== "Accepted"
          result.message must_== "This auth_token has access to use this method."

          rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.create.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
