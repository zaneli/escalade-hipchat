package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersDeleteSpec extends Specification with TestUtil {

  "users/delete" should {
    "call (set userId)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("Deleted", (limit, remaining, reset.getMillis / 1000))

      val (deleted, rate) = users.delete.call(12345)

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/users/delete"
      holder.params must_== Map("user_id" -> "12345", "auth_token" -> "token")

      deleted must beTrue

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (set email)" in {
      val (holder, users) = mockUsers("Deleted", (100, 98, DateTime.now.getMillis / 1000))

      users.delete.call("chris@hipchat.com")

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/users/delete"
      holder.params must_== Map("user_id" -> "chris@hipchat.com", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.delete.call(12345) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "users/delete?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.delete.test must beSuccessfulTry.which {
        case (result, rate) =>
          result.code must_== 202
          result.authType must_== "Accepted"
          result.message must_== "This auth_token has access to use this method."

          rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.delete.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
