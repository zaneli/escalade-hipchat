package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class RoomsListSpec extends Specification with TestUtil {

  "rooms/list" should {
    "call" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, rooms) = mockRooms("Rooms", (limit, remaining, reset.getMillis / 1000))

      val (res, rate) = rooms.list.call

      holder.method must_== "get"
      holder.path must_== "v1/rooms/list"
      holder.params must_== Map("auth_token" -> "token")

      res must have size 2

      {
        val room = res(0)
        room.roomId must_== 7
        room.name must_== "Development"
        room.topic must_== "Make sure to document your API functions well!"
        room.lastActive must beSome(new DateTime(1269020400L * 1000))
        room.created must_== new DateTime(1269010311L * 1000)
        room.ownerUserId must_== 1
        room.isArchived must beTrue
        room.isPrivate must beFalse
        room.xmppJid must_== "7_development@conf.hipchat.com"
        room.guestAccessUrl must beNone
      }
      {
        val room = res(1)
        room.roomId must_== 10
        room.name must_== "Ops"
        room.topic must beEmpty
        room.lastActive must beSome(new DateTime(1269010500L * 1000))
        room.created must_== new DateTime(1269010211L * 1000)
        room.ownerUserId must_== 5
        room.isArchived must beFalse
        room.isPrivate must beTrue
        room.xmppJid must_== "10_ops@conf.hipchat.com"
        room.guestAccessUrl must beSome("https://www.hipchat.com/XXXguest")
      }

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (Unauthorized token)" in {
      val rooms = mockUnauthorizedRooms
      rooms.list.call must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "rooms/list?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, rooms) = mockRooms("TestResult", (limit, remaining, reset.getMillis / 1000))

      rooms.list.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val rooms = mockUnauthorizedRooms
      rooms.list.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
