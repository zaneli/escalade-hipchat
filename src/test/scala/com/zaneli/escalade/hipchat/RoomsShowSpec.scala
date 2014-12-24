package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.{ RateLimit, UserIdentifier }
import org.specs2.mutable.Specification

class RoomsShowSpec extends Specification with TestUtil {

  "rooms/show" should {
    "call" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("Room", (limit, remaining, reset.getMillis / 1000))

      val (room, rate) = rooms.show.call(5)

      holder.method must_== "get"
      holder.path must_== "v1/rooms/show"
      holder.params must_== Map("room_id" -> "5", "auth_token" -> "token")

      room.roomId must_== 7
      room.name must_== "Ops"
      room.topic must_== "Chef is so awesome."
      room.lastActive must beSome(new DateTime(1269020400L * 1000))
      room.created must_== new DateTime(1269010311L * 1000)
      room.ownerUserId must_== 5
      room.isArchived must beFalse
      room.isPrivate must beTrue
      room.privacy must beSome("private")
      room.xmppJid must_== "7_ops@conf.hipchat.com"
      room.memberUserIds must_== List(1, 2, 3, 4, 5)
      room.participants must_== List(UserIdentifier(Right(5), "Garret Heaton"), UserIdentifier(Right(1), "Chris Rivers"))
      room.guestAccessUrl must beNone

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.show.call(5) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/show?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.show.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.show.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
