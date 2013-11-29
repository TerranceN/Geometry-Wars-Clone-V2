package com.awesome

import scala.math._

import vectors._

// Axis-aligned
class BoundingBox(val lower:Vector2, val upper:Vector2) {
  def this() = this(new Vector2(0), new Vector2(0))
  def combine(other:BoundingBox):BoundingBox =
    new BoundingBox(
      new Vector2(min(lower.x, other.lower.x), min(lower.y, other.lower.y)),
      new Vector2(max(upper.x, other.upper.x), max(upper.y, other.upper.y)))
  def contains(point:Vector2):Boolean = {
    return (point.x >= lower.x && point.x <= upper.x
         && point.y >= lower.y && point.y <= upper.y)
  }
  def containsStrict(point:Vector2):Boolean = {
    return (point.x > lower.x && point.x < upper.x
         && point.y > lower.y && point.y < upper.y)
  }
  def isCollidingWith(other:BoundingBox):Boolean = {
    val collidingX = lower.x <= other.upper.x && upper.x >= other.lower.x
    val collidingY = lower.y <= other.upper.y && upper.y >= other.lower.y

    return collidingX && collidingY
  }
  def size():Vector2 = upper - lower
}

object BoundingBox {
  def fromPoints(p1:Vector2, p2:Vector2):BoundingBox = {
    return new BoundingBox(
      new Vector2(min(p1.x, p2.x), min(p1.y, p2.y)),
      new Vector2(max(p1.x, p2.x), max(p1.y, p2.y)))
  }
}
