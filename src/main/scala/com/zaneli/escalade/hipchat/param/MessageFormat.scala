package com.zaneli.escalade.hipchat.param

sealed abstract class MessageFormat(value: String) {
  override def toString = value
}

object MessageFormat {
  case object Html extends MessageFormat("html")
  case object Text extends MessageFormat("text")
}
