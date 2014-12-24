package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersUndeleteSpec extends Specification with TestUtil {

  "users/undelete" should {
    "call (set userId)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("Undeleted", (limit, remaining, reset.getMillis / 1000))

      val (undeleted, rate) = users.undelete.call(12345)

      holder.method must_== "post"
      holder.path must_== "v1/users/undelete"
      holder.params must_== Map("user_id" -> "12345", "auth_token" -> "token")

      undeleted must beTrue

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (set email)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("Undeleted", (limit, remaining, reset.getMillis / 1000))

      users.undelete.call("garret@hipchat.com")

      holder.method must_== "post"
      holder.path must_== "v1/users/undelete"
      holder.params must_== Map("user_id" -> "garret@hipchat.com", "auth_token" -> "token")
    }

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.undelete.call(12345) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "users/undelete?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.undelete.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.undelete.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
