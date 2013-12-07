package com.zaneli.escalade.hipchat

import scala.io.Source

trait TestUtil {

  protected[this] def mockRooms(file: String, rateLimit: (Long, Long, Long)): (InputDataHolder, Rooms) = {
    val holder = new InputDataHolder
    val rooms = new Rooms("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, List[String]], String) = {
        holder.method = method
        holder.path = path
        holder.params = params
        dummyExecute(file, rateLimit)
      }
    }
    (holder, rooms)
  }

  protected[this] def mockUsers(file: String, rateLimit: (Long, Long, Long)): (InputDataHolder, Users) = {
    val holder = new InputDataHolder
    val users = new Users("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, List[String]], String) = {
        holder.method = method
        holder.path = path
        holder.params = params
        dummyExecute(file, rateLimit)
      }
    }
    (holder, users)
  }

  protected[this] def mockUnauthorizedRooms(): Rooms = {
    new Rooms("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, List[String]], String) =
        dummyUnauthorizedExecute
    }
  }

  protected[this] def mockUnauthorizedUsers(): Users = {
    new Users("token") {
      override def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, List[String]], String) =
        dummyUnauthorizedExecute
    }
  }

  protected[this] class InputDataHolder {
    var method: String = _
    var path: String = _
    var params: Map[String, String] = _
  }

  private[this] def dummyExecute(file: String, rateLimit: (Long, Long, Long)): (Int, Map[String, List[String]], String) = {
    val (limit, remaining, reset) = rateLimit
    (
      200,
      Map(
        "X-RateLimit-Limit" -> List(limit.toString),
        "X-RateLimit-Remaining" -> List(remaining.toString),
        "X-RateLimit-Reset" -> List(reset.toString)),
        Source.fromInputStream(classOf[TestUtil].getResourceAsStream(file + ".json")).mkString
    )
  }

  private[this] def dummyUnauthorizedExecute = {
    throw new scalaj.http.HttpException(
      401,
      "Unauthorized",
      """{"error":{"code":401,"type":"Unauthorized","message":"Auth token invalid. Please see: https:\/\/www.hipchat.com\/docs\/api\/auth"}}""",
      new java.io.IOException()
    )
  }
}
