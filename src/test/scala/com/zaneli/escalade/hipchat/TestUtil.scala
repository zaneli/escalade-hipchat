package com.zaneli.escalade.hipchat

import scala.io.Source

trait TestUtil {

  protected[this] val unauthorizedMessage =
    """\Q401: Unauthorized (Auth token invalid. Please see: https://www.hipchat.com/docs/api/auth)\E"""

  protected[this] def mockRooms(file: String, rateLimit: (Long, Long, Long)): (InputDataHolder, Rooms) = {
    val holder = new InputDataHolder
    val rooms = new Rooms("token") {
      override def httpExecute(method: String)(url: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) = {
        holder.method = method
        holder.url = url
        holder.params = params
        dummyExecute(file, rateLimit)
      }
    }
    (holder, rooms)
  }

  protected[this] def mockUsers(file: String, rateLimit: (Long, Long, Long)): (InputDataHolder, Users) = {
    val holder = new InputDataHolder
    val users = new Users("token") {
      override def httpExecute(method: String)(url: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) = {
        holder.method = method
        holder.url = url
        holder.params = params
        dummyExecute(file, rateLimit)
      }
    }
    (holder, users)
  }

  protected[this] def mockUnauthorizedRooms(): Rooms = {
    new Rooms("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) =
        dummyUnauthorizedExecute
    }
  }

  protected[this] def mockUnauthorizedUsers(): Users = {
    new Users("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) =
        dummyUnauthorizedExecute
    }
  }

  protected[this] class InputDataHolder {
    var method: String = _
    var url: String = _
    var params: Map[String, String] = _
  }

  private[this] def dummyExecute(file: String, rateLimit: (Long, Long, Long)): (Int, Map[String, Seq[String]], String) = {
    val (limit, remaining, reset) = rateLimit
    (
      200,
      Map(
        "X-RateLimit-Limit" -> Seq(limit.toString),
        "X-RateLimit-Remaining" -> Seq(remaining.toString),
        "X-RateLimit-Reset" -> Seq(reset.toString)),
        Source.fromInputStream(classOf[TestUtil].getResourceAsStream(file + ".json")).mkString)
  }

  private[this] def dummyUnauthorizedExecute = {
    throw HipChatException(
      401,
      """{"error":{"code":401,"type":"Unauthorized","message":"Auth token invalid. Please see: https:\/\/www.hipchat.com\/docs\/api\/auth"}}""")
  }
}
