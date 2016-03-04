package com.zaneli.escalade.hipchat.model

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.util.DataHandler

case class RateLimit(limit: Long, remaining: Long, reset: DateTime) {
  override def toString = s"RateLimit(limit=$limit, remaining=$remaining, reset=$reset)"
}

object RateLimit extends DataHandler {
  def apply(map: Map[String, Seq[String]]): RateLimit = {
    val limit = map.getOrElse("X-RateLimit-Limit", Nil).headOption.map(_.toLong).get
    val remaining = map.getOrElse("X-RateLimit-Remaining", Nil).headOption.map(_.toLong).get
    val reset = map.getOrElse("X-RateLimit-Reset", Nil).headOption.flatMap(x => sec2DateTime(x.toLong)).get
    RateLimit(limit, remaining, reset)
  }
}
