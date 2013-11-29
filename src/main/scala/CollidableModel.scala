package com.awesome

import vectors._
import matricies._

class CollidableModel(val model:LineModel) {
  var __modelTranslationAndScale = new Matrix4()
  var modelRotation = new Matrix4()
  var __boundingBox = new BoundingBox()

  def modelTranslationAndScale = __modelTranslationAndScale
  def modelTranslationAndScale_=(m:Matrix4) = {
    __modelTranslationAndScale = m
    __boundingBox = new BoundingBox(
      new Vector2(-1).transform(__modelTranslationAndScale),
      new Vector2(1).transform(__modelTranslationAndScale)
    )
  }

  def isCollidingWith(other:CollidableModel):Boolean = {
    if (__boundingBox.isCollidingWith(other.__boundingBox)) {
      // TODO: check for collision line by line
      return true
    }

    return false
  }

  def draw(color:Vector3) {
    GLFrustum.pushModelview()
      GLFrustum.modelviewMatrix.multiplyBy(modelTranslationAndScale * modelRotation)
      model.draw(color)
    GLFrustum.popModelview()
  }
}
