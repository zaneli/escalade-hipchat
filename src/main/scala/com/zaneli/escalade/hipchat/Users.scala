package com.zaneli.escalade.hipchat

import com.zaneli.escalade.hipchat.model.{ RateLimit, User }
import com.zaneli.escalade.hipchat.util.DataHandler
import net.liftweb.json.{ DefaultFormats, parse }

class Users(private[this] val token: String) extends HttpExecutor with DataHandler {

  private[this] val version = "v1"
  private[this] val category = "users"

  object create extends AuthClientBase(httpExecute("post") _, "create", version, category, token) {
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
    def call(userId: Int): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("user_id" -> userId))
      val deleted = (parse(res) \ "deleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object list extends AuthClientBase(httpExecute("get") _, "list", version, category, token) {
    def call(includeDeleted: Boolean = false): (List[User], RateLimit) = {
      val (res, rateLimit) = execute(Map("include_deleted" -> bool2Int(includeDeleted)))
      val users = (parse(res) \ "users").children.map { User.apply }
      (users, rateLimit)
    }
  }

  object show extends AuthClientBase(httpExecute("get") _, "show", version, category, token) {
    def call(userId: Int): (User, RateLimit) = {
      val (res, rateLimit) = execute(Map("user_id" -> userId))
      val user = parse(res).children.map { User.apply }.head
      (user, rateLimit)
    }
  }

  object undelete extends AuthClientBase(httpExecute("post") _, "undelete", version, category, token) {
    def call(userId: Int): (Boolean, RateLimit) = {
      implicit val formats = DefaultFormats

      val (res, rateLimit) = execute(Map("user_id" -> userId))
      val deleted = (parse(res) \ "undeleted").extract[Boolean]
      (deleted, rateLimit)
    }
  }

  object update extends AuthClientBase(httpExecute("post") _, "update", version, category, token) {
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
