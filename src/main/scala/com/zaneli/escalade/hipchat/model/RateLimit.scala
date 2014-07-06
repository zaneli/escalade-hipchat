package com.zaneli.escalade.hipchat.model

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.util.DataHandler

case class RateLimit (limit: Long, remaining: Long, reset: DateTime) {
  override def toString = s"RateLimit(limit=${limit}, remaining=${remaining}, reset=${reset})"
}

object RateLimit extends DataHandler {
  def apply(map: Map[String, List[String]]): RateLimit = {
    val limit = map.get("X-RateLimit-Limit").map(_.head.toInt).get
    val remaining = map.get("X-RateLimit-Remaining").map(_.head.toInt).get
    val reset = map.get("X-RateLimit-Reset").map(x => sec2DateTime(x.head.toLong)).flatten.get
    RateLimit(limit, remaining, reset)
  }
}
