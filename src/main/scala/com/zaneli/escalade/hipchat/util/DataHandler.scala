package com.zaneli.escalade.hipchat.util

import org.scala_tools.time.Imports.DateTime
import scala.util.Try

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

  protected[this] def sec2DateTime(sec: Long): Option[DateTime] =
    if (sec == 0) None else Try(new DateTime(sec * 1000)).toOption
}
