package com.zaneli.escalade.hipchat.model

import com.github.nscala_time.time.Imports.DateTime
import org.json4s.{ DefaultFormats, JValue }

case class Message private (date: DateTime, from: UserIdentifier, message: String, file: Option[File]) {
  override def toString = s"Message(date=${date}, from=${from}, message=${message}, file=${file})"
}

object Message {
  private[this] case class DataHolder(date: String, from: UserIdentifier.DataHolder, message: String, file: Option[File])

  def apply(value: JValue): Message = {
    implicit val formats = DefaultFormats + UserIdentifier.UserIdSerializer
    val holder = value.extract[DataHolder]
    Message(new DateTime(holder.date), UserIdentifier(holder.from), holder.message, holder.file)
  }
}

case class File(name: String, size: Int, url: String) {
  override def toString = s"File(name=${name}, size=${size}, url=${url})"
}

object File {
  def apply(value: JValue): File = {
    implicit val formats = DefaultFormats
    value.extract[File]
  }
}
