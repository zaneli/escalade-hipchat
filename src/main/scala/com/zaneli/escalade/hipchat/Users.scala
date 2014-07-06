package com.zaneli.escalade.hipchat

import com.zaneli.escalade.hipchat.model.{ RateLimit, User }
import com.zaneli.escalade.hipchat.util.DataHandler
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

class Users(private[this] val token: String) extends HttpExecutor with DataHandler {

  private[this] val version = "v1"
  private[this] val category = "users"

  object create extends AuthClientBase(httpExecute("post") _, "create", version, category, token) {
    /**
     * Create a new user in your group.
     *
     * @param email User's email.
     * @param name User's full name. Set (firstName, lastName) into pair.
     * @param mentionName User's mention name.
     * @param title User's title.
     * @param isGroupAdmin Whether or not this user is an admin.
     * @param password User's password.
     * @param timezone User's timezone. Must be a supported timezone(https://www.hipchat.com/docs/api/timezones).
     * @return User info and Rate Limiting info
     */
    def call(
        email: String, name: (String, String), mentionName: Option[String] = None, title: Option[String] = None,
        isGroupAdmin: Boolean = false, password: Option[String] = None, timezone: Option[String] = None): (User, RateLimit) = {
      val (res, rateLimit) = execute(Map(
        "email" -> email, "name" -> s"${name._1} ${name._2}", "mention_name" -> mentionName, "title" -> title,
        "is_group_admin" -> bool2Int(isGroupAdmin), "password" -> password, "timezone" -> timezone
      ))
      val user = parse(res).children.map { User.apply }.head
      (user, rateLimit)
    }
  }

  object delete extends AuthClientBase(httpExecute("post") _, "delete", version, category, token) {
    /**
     * Delete a user.
     *
     * @param userId ID of the user.
     * @return Deleted result and Rate Limiting info
     */
    def call(userId: Int): (Boolean, RateLimit) = call(userId.toString)

    /**
     * Delete a user.
     *
     * @param email Email address of the user.
     * @return Deleted result and Rate Limiting info
     */
    def call(email: String): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("user_id" -> email))
      val deleted = (parse(res) \ "deleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object list extends AuthClientBase(httpExecute("get") _, "list", version, category, token) {
    /**
     * List all users in the group.
     *
     * @param includeDeleted Include deleted users in response.
     * @return Users info and Rate Limiting info
     */
    def call(includeDeleted: Boolean = false): (List[User], RateLimit) = {
      val (res, rateLimit) = execute(Map("include_deleted" -> bool2Int(includeDeleted)))
      val users = (parse(res) \ "users").children.map { User.apply }
      (users, rateLimit)
    }
  }

  object show extends AuthClientBase(httpExecute("get") _, "show", version, category, token) {
    /**
     * Get a user's details.
     *
     * @param userId ID of the user.
     * @return User info and Rate Limiting info
     */
    def call(userId: Int): (User, RateLimit) = call(userId.toString)

    /**
     * Get a user's details.
     *
     * @param email Email address of the user.
     * @return User info and Rate Limiting info
     */
    def call(email: String): (User, RateLimit) = {
      val (res, rateLimit) = execute(Map("user_id" -> email))
      val user = parse(res).children.map { User.apply }.head
      (user, rateLimit)
    }
  }

  object undelete extends AuthClientBase(httpExecute("post") _, "undelete", version, category, token) {
    /**
     * Undelete a user.
     *
     * @param userId ID of the user.
     * @return Undeleted result and Rate Limiting info
     */
    def call(userId: Int): (Boolean, RateLimit) = call(userId.toString)

    /**
     * Undelete a user.
     *
     * @param email Email address of the user.
     * @return Undeleted result and Rate Limiting info
     */
    def call(email: String): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("user_id" -> email))
      val deleted = (parse(res) \ "undeleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object update extends AuthClientBase(httpExecute("post") _, "update", version, category, token) {
    /**
     * Update a user.
     *
     * @param userId ID of the user.
     * @param email User's email.
     * @param name User's full name. Set (firstName, lastName) into pair.
     * @param mentionName User's mention name.
     * @param title User's title.
     * @param isGroupAdmin Whether or not this user is an admin.
     * @param password User's password.
     * @param timezone User's timezone. Must be a supported timezone(https://www.hipchat.com/docs/api/timezones).
     * @return Undeleted result and Rate Limiting info
     */
    def call(
      userId: Int, email: Option[String] = None, name: Option[(String, String)] = None, mentionName: Option[String] = None, title: Option[String] = None,
      isGroupAdmin: Option[Boolean] = None, password: Option[String] = None, timezone: Option[String] = None): (User, RateLimit) = {
      if (Seq(email, name, mentionName, title, isGroupAdmin, password, timezone).forall(_.isEmpty)) {
        throw HipChatException("Invalid params: Set a item at least.")
      }

      val (res, rateLimit) = execute(Map(
        "user_id" -> userId, "email" -> email, "name" -> name.map{ case (f, l) => s"${f} ${l}" }, "mention_name" -> mentionName, "title" -> title,
        "is_group_admin" -> isGroupAdmin.map(bool2Int), "password" -> password, "timezone" -> timezone
      ))
      val user = parse(res).children.map { User.apply }.head
      (user, rateLimit)
    }
  }
}
