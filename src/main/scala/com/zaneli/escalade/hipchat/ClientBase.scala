package com.zaneli.escalade.hipchat

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.zaneli.escalade.hipchat.model.{ RateLimit, TestResult }
import com.zaneli.escalade.hipchat.util.DataHandler
import org.json4s.native.JsonMethods.parse
import scalaj.http.Http
import scala.util.{ Failure, Success, Try }

private[hipchat] abstract sealed class ClientBase(
  private[this] val callApi: ((String, Map[String, String]) => (Int, Map[String, Seq[String]], String)),
  private[this] val apiMethod: String,
  private[this] val version: String,
  private[this] val category: String) extends DataHandler with LazyLogging {

  protected[this] def execute(params: Map[String, Any]): (String, RateLimit) = {
    val apiParams = value2String(params)
    logger.debug(s"params  = ${apiParams}")

    val (code, headers, body) = callApi(s"${version}/${category}/${apiMethod}", apiParams)

    logger.debug(s"code    = ${code}")
    logger.debug(s"headers = ${headers}")
    logger.debug(s"body    = ${body}")

    (body, RateLimit(headers))
  }
}

private[hipchat] abstract class AuthClientBase(
  private[this] val callApi: ((String, Map[String, String]) => (Int, Map[String, Seq[String]], String)),
  private[this] val apiMethod: String,
  private[this] val version: String,
  private[this] val category: String,
  private[this] val token: String) extends ClientBase(callApi, apiMethod, version, category) {

  override protected[this] def execute(params: Map[String, Any] = Map()): (String, RateLimit) =
    super.execute(params.updated("auth_token", token))

  def test: Try[(TestResult, RateLimit)] = {
    try {
      val (body, rateLimit) = execute(Map("auth_test" -> "true"))
      val result = parse(body).children.map { TestResult.apply }.head
      Success(result, rateLimit)
    } catch {
      case e: HipChatAuthException => Failure(e)
      case t: Throwable => throw t
    }
  }
}

private[hipchat] trait HttpExecutor {
  private[this] val host = "api.hipchat.com"

  protected[this] def httpExecute(method: String)(path: String, params: Map[String, String]): (Int, Map[String, Seq[String]], String) = {
    val req = Http(s"http://${host}/${path}").params(params).method(method).timeout(connTimeoutMs = 5000, readTimeoutMs = 5000)
    val res = req.asString
    if (res.isError) {
      throw HipChatException(res.code, res.body)
    }
    (res.code, res.headers.toMap, res.body)
  }
}
