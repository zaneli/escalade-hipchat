package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersShowSpec extends Specification with TestUtil {

  "users/show" should {
    "call (set userId)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("User", (limit, remaining, reset.getMillis / 1000))

      val (user, rate) = users.show.call(5)

      holder.method must_== "get"
      holder.path must_== "v1/users/show"
      holder.params must_== Map("user_id" -> "5", "auth_token" -> "token")

      user.userId must_== 5
      user.name must_== "Garret Heaton"
      user.mentionName must_== "garret"
      user.email must_== "garret@hipchat.com"
      user.title must_== "Co-founder"
      user.password must beNone
      user.photoUrl must_== "https://www.hipchat.com/img/silhouette_125.png"
      user.lastActive must beSome(new DateTime(1360031425L * 1000))
      user.created must beSome(new DateTime(1315711352L * 1000))
      user.status must_== "available"
      user.statusMessage must_== "Come see what I'm working on!"
      user.timezone must_== "US/Central"
      user.isGroupAdmin must beTrue
      user.isDeleted must beFalse

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (set email)" in {
      val (holder, users) = mockUsers("User", (100, 98, DateTime.now.getMillis / 1000))

      users.show.call("pete@hipchat.com")

      holder.method must_== "get"
      holder.path must_== "v1/users/show"
      holder.params must_== Map("user_id" -> "pete@hipchat.com", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.show.call(5) must throwA[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }

  "users/show?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.show.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.show.test must beFailedTry.withThrowable[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }
}
