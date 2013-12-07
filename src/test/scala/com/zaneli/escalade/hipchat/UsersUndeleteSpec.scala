package com.zaneli.escalade.hipchat

import com.zaneli.escalade.hipchat.model.RateLimit
import org.scala_tools.time.Imports.DateTime
import org.specs2.mutable.Specification

class UsersUndeleteSpec extends Specification with TestUtil {

  "users/undelete" should {
    "call" in {
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

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.undelete.call(12345) must throwA[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
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
      users.undelete.test must beFailedTry.withThrowable[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }
}
