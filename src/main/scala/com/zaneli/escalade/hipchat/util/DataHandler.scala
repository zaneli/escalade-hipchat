package com.zaneli.escalade.hipchat.util

import com.github.nscala_time.time.Imports.DateTime

trait DataHandler {

  protected[this] def value2String(map: Map[String, Any]): Map[String, String] = {
    map.collect {
      case (k, Some(v)) => (k, v.toString)
      case (k, v) if v != None => (k, v.toString)
    }
  }

  protected[this] def bool2Int(b: Boolean): Int = if (b) 1 else 0

  protected[this] def int2bool(i: Int): Boolean = (i == 1)

  protected[this] def empty2None(org: Option[String]): Option[String] = org match {
    case Some("") => None
    case x => x
  }

  protected[this] def sec2DateTime(sec: Long): Option[DateTime] = {
    PartialFunction.condOpt(sec) {
      case s if s != 0 => new DateTime(sec * 1000)
    }
  }
}
