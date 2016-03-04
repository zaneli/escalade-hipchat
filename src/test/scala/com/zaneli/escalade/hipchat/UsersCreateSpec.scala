package com.zaneli.escalade.hipchat

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.model.RateLimit
import org.specs2.mutable.Specification

class UsersCreateSpec extends Specification with TestUtil {

  "users/create" should {
    "call (min params)" in {
      val limit = 100
      val remaining = 99
      val reset = new DateTime(2013, 12, 1, 10, 5, 0)
      val (holder, users) = mockUsers("CreatedUser", (limit, remaining, reset.getMillis / 1000))

      val (user, rate) = users.create.call("test@test.com", ("first_name", "last_name"))

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/users/create"
      holder.params must_== Map("email" -> "test@test.com", "name" -> "first_name last_name", "is_group_admin" -> "0", "auth_token" -> "token")

      user.userId must_== 3
      user.name must_== "New Guy"
      user.mentionName must_== "NewGuy"
      user.email must_== "new@company.com"
      user.title must_== "Intern"
      user.password must beSome("d294H58zlE")
      user.photoUrl must_== "https://www.hipchat.com/img/silhouette_125.png"
      user.lastActive must beNone
      user.created must beNone
      user.status must_== "offline"
      user.statusMessage must beEmpty
      user.timezone must_== "UTC"
      user.isGroupAdmin must beFalse
      user.isDeleted must beFalse

      rate must_== RateLimit(limit, remaining, reset)
    }

    "call (full params)" in {
      val (holder, users) = mockUsers("CreatedUser", (100, 98, DateTime.now.getMillis / 1000))

      users.create.call("test@test.com", ("first_name", "last_name"), Some("mention"), Some("title"), true, Some("password"), Some("UTC"))

      holder.method must_== "post"
      holder.url must_== "http://api.hipchat.com/v1/users/create"
      holder.params must_== Map(
        "email" -> "test@test.com", "name" -> "first_name last_name", "mention_name" -> "mention", "title" -> "title",
        "is_group_admin" -> "1", "password" -> "password", "timezone" -> "UTC", "auth_token" -> "token"
      )
    }

    "call (Unauthorized token)" in {
      val users = mockUnauthorizedUsers
      users.create.call("test@test.com", ("first_name", "last_name")) must throwA[HipChatAuthException](unauthorizedMessage)
    }
  }

  "users/create?auth_test=true" should {
    "test success" in {
      val limit = 100
      val remaining = 98
      val reset = new DateTime(2013, 12, 1, 11, 5, 0)
      val (_, users) = mockUsers("TestResult", (limit, remaining, reset.getMillis / 1000))

      users.create.test must beSuccessfulTry.which {
        case (result, rate) =>
          result.code must_== 202
          result.authType must_== "Accepted"
          result.message must_== "This auth_token has access to use this method."

          rate must_== RateLimit(limit, remaining, reset)
      }
    }

    "test failure" in {
      val users = mockUnauthorizedUsers
      users.create.test must beFailedTry.withThrowable[HipChatAuthException](unauthorizedMessage)
    }
  }
}
