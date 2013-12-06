package com.awesome

import vectors._

class LineSegment(p1:Vector2, p2:Vector2) {

  val collisionBounds = BoundingBox.fromPoints(p1, p2)
  val line = new LineSegment.Line(p1, p2)

  def intersectionWith(other:LineSegment):Option[Vector2] = {
    line.intersectionWith(other.line) match {
      case Some(i) => if (collisionBounds.contains(i)) Some(i) else None
      case None => None
    }
  }
}
object LineSegment {
  object LineType extends Enumeration {
    type LineType = Value
    val Normal, Vertical, Horizontal = Value
  }

  class Line(p1:Vector2, p2:Vector2) {

    import LineType._

    var lineType:LineType = Horizontal
    var slope:Float = 0
    var intercept:Float = 0
    initLine()

    def initLine() {
      val diff = p2 - p1
      if (diff.x == 0) {
        lineType = Vertical
        intercept = p1.x
      } else if (diff.y == 0) {
        lineType = Horizontal
        intercept = p1.y
      } else {
        lineType = Normal
        slope = diff.y / diff.x
        intercept = p1.y - slope * p1.x
      }
    }

    def intersectionWith(other:Line):Option[Vector2] = {
      lineType match {
        case Normal => other.lineType match {
          case Normal => {
            val commonX = (other.intercept - intercept) / (slope - other.slope)
            Some(new Vector2(commonX, slope * commonX + intercept))
          }
          case Horizontal => Some(new Vector2((other.intercept - intercept) / slope, other.intercept))
          case Vertical => Some(new Vector2(other.intercept, slope * other.intercept + intercept))
        }
        case Horizontal => other.lineType match {
          case Normal => other.intersectionWith(this)
          case Horizontal => None
          case Vertical => Some(new Vector2(other.intercept, intercept))
        }
        case Vertical => other.lineType match {
          case Normal => other.intersectionWith(this)
          case Horizontal => other.intersectionWith(this)
          case Vertical => None
        }
      }
    }
  }
}
