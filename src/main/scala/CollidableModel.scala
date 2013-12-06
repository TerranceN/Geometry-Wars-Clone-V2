package com.awesome

import vectors._
import matricies._

class CollidableModel(val model:LineModel) {
  var __modelTranslation = new Matrix4()
  var __modelScale = new Matrix4()
  var modelRotation = new Matrix4()
  var __boundingBox = new BoundingBox()
  var __boundingBoxInvalid = true

  def modelTranslation = __modelTranslation
  def modelTranslation_=(m:Matrix4) = {
    __modelTranslation = m
    __boundingBoxInvalid = true
  }

  def modelScale = __modelScale
  def modelScale_=(m:Matrix4) = {
    __modelScale = m
    __boundingBoxInvalid = true
  }

  def remakeBoundingBox() {
    __boundingBox = new BoundingBox(
      new Vector2(-1).transform(__modelTranslation * __modelScale),
      new Vector2(1).transform(__modelTranslation * __modelScale)
    )
    __boundingBoxInvalid = false
  }

  def boundingBox:BoundingBox = {
    if (__boundingBoxInvalid) { remakeBoundingBox() }
    return __boundingBox
  }

  def lineCollision(other:CollidableModel):Boolean = {
    val transformedPoints1 = model.points.map (_.transform(transformations))
    val transformedPoints2 = other.model.points.map (_.transform(other.transformations))
    transformedPoints1.grouped(2) foreach { pointLst =>
      val line1 = new LineSegment(pointLst(0), pointLst(1))

      transformedPoints2.grouped(2) foreach { pointLst =>
        val line2 = new LineSegment(pointLst(0), pointLst(1))

        line1.intersectionWith(line2) match {
          case Some(_) => return true
          case None => {}
        }
      }
    }
    return false
  }

  def isCollidingWith(other:CollidableModel):Boolean = {
    if (boundingBox.isCollidingWith(other.boundingBox)) {
      // TODO: check for collision line by line
      return lineCollision(other)
    }

    return false
  }

  def transformations = modelTranslation * modelRotation * modelScale

  def draw(color:Vector3) {
    GLFrustum.pushModelview()
      GLFrustum.modelviewMatrix.multiplyBy(transformations)
      model.draw(color)
    GLFrustum.popModelview()
  }
}
