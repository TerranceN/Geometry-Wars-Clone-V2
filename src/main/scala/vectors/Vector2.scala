package com.awesome.vectors

import com.awesome.matricies._
import scala.math._

class Vector2(var x:Float, var y:Float) {
  def this(x:Float) = this(x, x)
  def this() = this(0)
  def copy():Vector2 = new Vector2(x, y)
  def +(other:Vector2):Vector2 = new Vector2(x + other.x, y + other.y)
  def -(other:Vector2):Vector2 = new Vector2(x - other.x, y - other.y)
  def *(scale:Float):Vector2 = new Vector2(x * scale, y * scale)
  def *(other:Vector2):Vector2 = new Vector2(x * other.x, y * other.y)
  def /(scale:Float):Vector2 = this * (1 / scale)
  def dot(other:Vector2):Float = x*other.x + y*other.y
  def length():Float = sqrt(x*x + y*y).toFloat
  def normalized():Vector2 = {
    val l = length()
    if (l > 0) {
      return new Vector2(x/l, y/l)
    } else {
      return new Vector2(0)
    }
  }
  def lerp(other:Vector2, t:Float):Vector2 = {
    val diff = other - this
    return this + diff * t
  }
  def transform(m:Matrix4):Vector2 = {
    return new Vector4(x, y, 0, 1).transform(m).xy
  }
}
