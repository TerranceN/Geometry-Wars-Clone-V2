package com.awesome.matricies

import scala.math._

import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

class Matrix4(var data:Array[Float]) {
  def this() = this(null:Array[Float])
  if (data == null || data.length != 16) {
    setIdentity()
  }

  def setIdentity() {
    data = new Array(16)
    for (i <- 0 until 4) {
      for (j <- 0 until 4) {
        data(4 * j + i) = if (i == j) 1 else 0
      }
    }
  }

  def copy():Matrix4 = {
    return new Matrix4(data)
  }

  def multiplyBy(other:Matrix4) {
    val result = this.multiply(other)
    data = result.data
  }

  def multiply(other:Matrix4):Matrix4 = {
    val result = new Matrix4()

    for (i <- 0 until 4) {
      for (j <- 0 until 4) {
        var sum:Float = 0
        for (k <- 0 until 4) {
          sum += data(4 * k + i) * other.data(4 * j + k)
        }
        result.data(4 * j + i) = sum
      }
    }

    return result
  }

  def inverse():Matrix4 = {
    var result = new Matrix4()

    result.data(0) =
      data(5)  * data(10) * data(15) - 
      data(5)  * data(11) * data(14) - 
      data(9)  * data(6)  * data(15) + 
      data(9)  * data(7)  * data(14) +
      data(13) * data(6)  * data(11) - 
      data(13) * data(7)  * data(10);

    result.data(4) =
      -data(4)  * data(10) * data(15) + 
      data(4)  * data(11) * data(14) + 
      data(8)  * data(6)  * data(15) - 
      data(8)  * data(7)  * data(14) - 
      data(12) * data(6)  * data(11) + 
      data(12) * data(7)  * data(10);

    result.data(8) =
      data(4)  * data(9) * data(15) - 
      data(4)  * data(11) * data(13) - 
      data(8)  * data(5) * data(15) + 
      data(8)  * data(7) * data(13) + 
      data(12) * data(5) * data(11) - 
      data(12) * data(7) * data(9);

    result.data(12) =
      -data(4)  * data(9) * data(14) + 
      data(4)  * data(10) * data(13) +
      data(8)  * data(5) * data(14) - 
      data(8)  * data(6) * data(13) - 
      data(12) * data(5) * data(10) + 
      data(12) * data(6) * data(9);

    result.data(1) =
      -data(1)  * data(10) * data(15) + 
      data(1)  * data(11) * data(14) + 
      data(9)  * data(2) * data(15) - 
      data(9)  * data(3) * data(14) - 
      data(13) * data(2) * data(11) + 
      data(13) * data(3) * data(10);

    result.data(5) =
      data(0)  * data(10) * data(15) - 
      data(0)  * data(11) * data(14) - 
      data(8)  * data(2) * data(15) + 
      data(8)  * data(3) * data(14) + 
      data(12) * data(2) * data(11) - 
      data(12) * data(3) * data(10);

    result.data(9) =
      -data(0)  * data(9) * data(15) + 
      data(0)  * data(11) * data(13) + 
      data(8)  * data(1) * data(15) - 
      data(8)  * data(3) * data(13) - 
      data(12) * data(1) * data(11) + 
      data(12) * data(3) * data(9);

    result.data(13) =
      data(0)  * data(9) * data(14) - 
      data(0)  * data(10) * data(13) - 
      data(8)  * data(1) * data(14) + 
      data(8)  * data(2) * data(13) + 
      data(12) * data(1) * data(10) - 
      data(12) * data(2) * data(9);

    result.data(2) =
      data(1)  * data(6) * data(15) - 
      data(1)  * data(7) * data(14) - 
      data(5)  * data(2) * data(15) + 
      data(5)  * data(3) * data(14) + 
      data(13) * data(2) * data(7) - 
      data(13) * data(3) * data(6);

    result.data(6) =
      -data(0)  * data(6) * data(15) + 
      data(0)  * data(7) * data(14) + 
      data(4)  * data(2) * data(15) - 
      data(4)  * data(3) * data(14) - 
      data(12) * data(2) * data(7) + 
      data(12) * data(3) * data(6);

    result.data(10) =
      data(0)  * data(5) * data(15) - 
      data(0)  * data(7) * data(13) - 
      data(4)  * data(1) * data(15) + 
      data(4)  * data(3) * data(13) + 
      data(12) * data(1) * data(7) - 
      data(12) * data(3) * data(5);

    result.data(14) =
      -data(0)  * data(5) * data(14) + 
      data(0)  * data(6) * data(13) + 
      data(4)  * data(1) * data(14) - 
      data(4)  * data(2) * data(13) - 
      data(12) * data(1) * data(6) + 
      data(12) * data(2) * data(5);

    result.data(3) =
      -data(1) * data(6) * data(11) + 
      data(1) * data(7) * data(10) + 
      data(5) * data(2) * data(11) - 
      data(5) * data(3) * data(10) - 
      data(9) * data(2) * data(7) + 
      data(9) * data(3) * data(6);

    result.data(7) =
      data(0) * data(6) * data(11) - 
      data(0) * data(7) * data(10) - 
      data(4) * data(2) * data(11) + 
      data(4) * data(3) * data(10) + 
      data(8) * data(2) * data(7) - 
      data(8) * data(3) * data(6);

    result.data(11) =
      -data(0) * data(5) * data(11) + 
      data(0) * data(7) * data(9) + 
      data(4) * data(1) * data(11) - 
      data(4) * data(3) * data(9) - 
      data(8) * data(1) * data(7) + 
      data(8) * data(3) * data(5);

    result.data(15) =
      data(0) * data(5) * data(10) - 
      data(0) * data(6) * data(9) - 
      data(4) * data(1) * data(10) + 
      data(4) * data(2) * data(9) + 
      data(8) * data(1) * data(6) - 
      data(8) * data(2) * data(5);

    var det = data(0) * result.data(0) + data(1) * result.data(4) + data(2) * result.data(8) + data(3) * result.data(12);

    if (det == 0)
      return new Matrix4()

    det = 1.0f / det

    for (i <- 0 until 16) {
      result.data(i) = result.data(i) * det
    }

    return result
  }

  def getFloatBuffer():FloatBuffer = {
    val buffer = BufferUtils.createFloatBuffer(16)
    buffer.put(data)
    buffer.flip
    return buffer
  }

  def print() {
    for (j <- 0 until 4) {
      var str = ""
      for (i <- 0 until 4) {
        if (str != "") {
          str += ", "
        }
        str += data(i + 4 * j);
      }

      Console.println(str)
    }
  }
}

object Matrix4 {
  def scale(sx:Float, sy:Float, sz:Float):Matrix4 = {
    val result = new Matrix4()
    result.data(0) = sx
    result.data(5) = sy
    result.data(10) = sz
    return result
  }

  def translate(tx:Float, ty:Float, tz:Float):Matrix4 = {
    val result = new Matrix4()
    result.data(12) = tx
    result.data(13) = ty
    result.data(14) = tz
    return result
  }

  def rotateZ(angle:Float):Matrix4 = {
    val c = cos(angle).toFloat
    val s = sin(angle).toFloat
    val result = new Matrix4()
    result.data(0) = c
    result.data(1) = s
    result.data(4) = -s
    result.data(5) = c
    return result
  }

  def rotateX(angle:Float):Matrix4 = {
    val c = cos(angle).toFloat
    val s = sin(angle).toFloat
    val result = new Matrix4()
    result.data(5) = c
    result.data(6) = -s
    result.data(9) = s
    result.data(10) = c
    return result
  }

  def rotateY(angle:Float):Matrix4 = {
    val c = cos(angle).toFloat
    val s = sin(angle).toFloat
    val result = new Matrix4()
    result.data(0) = c
    result.data(2) = -s
    result.data(8) = s
    result.data(10) = c
    return result
  }

  def perspective(fovy:Double, aspect:Double, near:Double, far:Double):Matrix4 = {
    val f = 1.0 / tan(fovy / 2)
    val diff = near - far
    val result = new Matrix4()
    result.data(0) = (f / aspect).toFloat
    result.data(5) = f.toFloat
    result.data(10) = ((far + near) / diff).toFloat
    result.data(11) = -1
    result.data(14) = ((2 * far * near) / diff).toFloat
    result.data(15) = 0
    return result
  }

  def ortho(left:Float, right:Float, bottom:Float, top:Float, near:Float, far:Float):Matrix4 = {
    val result = new Matrix4()
    result.data(0) = 2 / (right - left)
    result.data(5) = 2 / (top - bottom)
    result.data(10) = -2 / (far-near)
    result.data(12) = -(right + left) / (right - left)
    result.data(13) = -(top + bottom) / (top - bottom)
    result.data(14) = -(far + near) / (far - near)
    result.data(15) = 1

    return result
  }
}
