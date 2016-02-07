package com.zaneli.escalade.hipchat

import java.net.MalformedURLException

import org.specs2.mutable.Specification

class HostSpecifySpec extends Specification with TestUtil {

  "Rooms" should {
    "set valid http host" in {
      val (holder, rooms) = mockRooms("http://example.com/")
      rooms.list.call
      holder.url must_== "http://example.com/v1/rooms/list"
    }
    "set valid https host" in {
      val (holder, rooms) = mockRooms("https://example.com/")
      rooms.list.call
      holder.url must_== "https://example.com/v1/rooms/list"
    }
    "set valid http host and port" in {
      val (holder, rooms) = mockRooms("http://example.com:8080/")
      rooms.list.call
      holder.url must_== "http://example.com:8080/v1/rooms/list"
    }
    "set invalid host" in {
      mockRooms("invalid") must throwA(new MalformedURLException("no protocol: invalid"))
    }
  }

  "Users" should {
    "set valid http host" in {
      val (holder, users) = mockUsers("http://example.com/")
      users.list.call()
      holder.url must_== "http://example.com/v1/users/list"
    }
    "set valid https host" in {
      val (holder, users) = mockUsers("https://example.com/")
      users.list.call()
      holder.url must_== "https://example.com/v1/users/list"
    }
    "set valid http host and port" in {
      val (holder, users) = mockUsers("http://example.com:8080/")
      users.list.call()
      holder.url must_== "http://example.com:8080/v1/users/list"
    }
    "set invalid host" in {
      mockUsers("invalid") must throwA(new MalformedURLException("no protocol: invalid"))
    }
  }

  private[this] def mockRooms(host: String): (InputDataHolder, Rooms) = {
    val holder = new InputDataHolder
    val rooms = new Rooms(host, "token") {
      override def httpExecute(method: String)(url: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) = {
        holder.method = method
        holder.url = url
        holder.params = params

        dummyResponse
      }
    }
    (holder, rooms)
  }

  private[this] def mockUsers(host: String): (InputDataHolder, Users) = {
    val holder = new InputDataHolder
    val users = new Users(host, "token") {
      override def httpExecute(method: String)(url: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) = {
        holder.method = method
        holder.url = url
        holder.params = params

        dummyResponse
      }
    }
    (holder, users)
  }

  private[this] val dummyResponse = {
    (
      200,
      Map(
        "X-RateLimit-Limit" -> Seq("1"),
        "X-RateLimit-Remaining" -> Seq("1"),
        "X-RateLimit-Reset" -> Seq("1")),
      "")
  }
}
