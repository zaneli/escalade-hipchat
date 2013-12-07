package com.zaneli.escalade.hipchat.param

sealed abstract class Color(value: String) {
  override def toString = value
}

object Color {
  case object Yellow extends Color("yellow")
  case object Red extends Color("red")
  case object Green extends Color("green")
  case object Purple extends Color("purple")
  case object Gray extends Color("gray")
  case object Random extends Color("random")
}
