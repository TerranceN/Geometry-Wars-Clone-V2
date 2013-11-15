package com.awesome.vectors

import com.awesome.matricies._

class Vector4(var x:Float, var y:Float, var z:Float, var w:Float) {
  def this(x:Float) = this(x, x, x, x)
  def this() = this(0)
  def xy():Vector2 = new Vector2(x, y)
  def transform(m:Matrix4):Vector4 = {
    var result = new Vector4()
    result.x = x * m.data(0) + y * m.data(4) + z * m.data( 8) + w * m.data(12)
    result.y = x * m.data(1) + y * m.data(5) + z * m.data( 9) + w * m.data(13)
    result.z = x * m.data(2) + y * m.data(6) + z * m.data(10) + w * m.data(14)
    result.w = x * m.data(3) + y * m.data(7) + z * m.data(11) + w * m.data(15)
    return result
  }
  def plus(other:Vector4):Vector4 = new Vector4(x + other.x, y + other.y, z + other.z, w + other.w)
}
