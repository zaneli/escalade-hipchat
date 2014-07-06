package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersUpdateSpec extends Specification with TestUtil {

  "users/update" should {
    "call (min params)" in {
      val (_, users) = mockUsers("User", (100, 99, DateTime.now.getMillis / 1000))
      users.update.call(5) must throwA[HipChatException]("""\QInvalid params: Set a item at least.\E""")
    }

    "call (full params)" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (holder, users) = mockUsers("User", (limit, remaining, reset.getMillis / 1000))

      val (user, rate) = users.update.call(
        5,
        Some("garret@hipchat.com"), Some(("Garret", "Heaton")), Some("garret"), Some("Co-founder"), Some(true), Some("password"), Some("US/Central")
      )

      holder.method must_== "post"
      holder.path must_== "v1/users/update"
      holder.params must_== Map(
        "user_id" -> "5", "email" -> "garret@hipchat.com", "name" -> "Garret Heaton", "mention_name" -> "garret", "title" -> "Co-founder",
        "is_group_admin" -> "1", "password" -> "password", "timezone" -> "US/Central", "auth_token" -> "token")

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

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.update.call(5, name = Some("first", "last")) must throwA[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }

  "users/update?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 97
      val reset = new DateTime(2013, 12, 1, 12, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.update.test must beSuccessfulTry.which { case (result, rate) =>
        result.code must_== 202
        result.authType must_== "Accepted"
        result.message must_== "This auth_token has access to use this method."

        rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.update.test must beFailedTry.withThrowable[HipChatException]("""\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E""")
    }
  }
}
