package com.zaneli.escalade.hipchat.model

import org.json4s.{ DefaultFormats, JValue }

case class TestResult(code: Int, authType: String, message: String) {
  override def toString = s"TestResult(code=${code}, authType=${authType}, message=${message})"
}

object TestResult {
  private[this] case class DataHolder(code: Int, `type`: String, message: String)

  def apply(value: JValue): TestResult = {
    implicit val formats = DefaultFormats
    val holder = value.extract[DataHolder]
    TestResult(holder.code, holder.`type`, holder.message)
  }
}
