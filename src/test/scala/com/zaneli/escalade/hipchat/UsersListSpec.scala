package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersListSpec extends Specification with TestUtil {

  "users/list" should {
    "call (min params)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("Users", (limit, remaining, reset.getMillis / 1000))

      val (res, rate) = users.list.call()

      holder.method must_== "get"
      holder.path must_== "v1/users/list"
      holder.params must_== Map("include_deleted" -> "0", "auth_token" -> "token")

      res must have size 3

      {
        val user = res(0)
        user.userId must_== 1
        user.name must_== "Chris Rivers"
        user.mentionName must_== "chris"
        user.email must_== "chris@hipchat.com"
        user.title must_== "Developer"
        user.password must beNone
        user.photoUrl must_== "https://www.hipchat.com/chris.png"
        user.lastActive must beSome(new DateTime(1360031425L * 1000))
        user.created must beSome(new DateTime(1315711352L * 1000))
        user.status must_== "away"
        user.statusMessage must_== "gym, bbl"
        user.timezone must_== "UTC"
        user.isGroupAdmin must beTrue
        user.isDeleted must beFalse
      }
      {
        val user = res(1)
        user.userId must_== 3
        user.name must_== "Peter Curley"
        user.mentionName must_== "pete"
        user.email must_== "pete@hipchat.com"
        user.title must_== "Designer"
        user.password must beNone
        user.photoUrl must_== "https://www.hipchat.com/pete.png"
        user.lastActive must beNone
        user.created must beSome(new DateTime(1315711352L * 1000))
        user.status must_== "offline"
        user.statusMessage must beEmpty
        user.timezone must_== "Asia/Tokyo"
        user.isGroupAdmin must beFalse
        user.isDeleted must beFalse
      }
      {
        val user = res(2)
        user.userId must_== 5
        user.name must_== "Garret Heaton"
        user.mentionName must_== "garret"
        user.email must_== "garret@hipchat.com"
        user.title must_== "Co-founder"
        user.password must beNone
        user.photoUrl must_== "https://www.hipchat.com/garret.png"
        user.lastActive must beSome(new DateTime(1360031425L * 1000))
        user.created must beSome(new DateTime(1315711352L * 1000))
        user.status must_== "available"
        user.statusMessage must_== "Come see what I'm working on!"
        user.timezone must_== "US/Central"
        user.isGroupAdmin must beFalse
        user.isDeleted must beTrue
      }

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, users) = mockUsers("Users", (100, 98, DateTime.now.getMillis / 1000))

      users.list.call(true)

      holder.method must_== "get"
      holder.path must_== "v1/users/list"
      holder.params must_== Map("include_deleted" -> "1", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.list.call() must throwA[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }

  "users/list?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 97
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.list.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.list.test must beFailedTry.withThrowable[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }
}
