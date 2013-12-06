package com.awesome

import matricies._
import vectors._

class Camera {
  var translationMatrix:Matrix4 = new Matrix4()
  var scaleMatrix:Matrix4 = Matrix4.scale(0.75f, 0.75f, 1)

  var minTranslation:Option[Vector2] = None
  var maxTranslation:Option[Vector2] = None

  def setBoundaries(min:Vector2, max:Vector2) {
    minTranslation = Some(min)
    maxTranslation = Some(max)
  }

  def moveTowardsCenter(center:Vector2, divisor:Float) {
    var currentTranslation = toWorldCoordinates(new Vector2(0))
    var screenCenter = toWorldCoordinates(new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight) / 2)
    var diff = center - screenCenter
    var newTranslation = lockTranslationToBoundaries(currentTranslation + diff / divisor)

    // make new translation matrix that's incremented by the difference / divisor
    translationMatrix = Matrix4.translate(newTranslation * -1)
  }

  def lockTranslationToBoundaries(translation:Vector2):Vector2 = {
    var newTranslation = translation

    minTranslation match {
      case None => {}
      case Some(t) => {
        val scaled = t.transform(scaleMatrix)
        if (newTranslation.x < t.x) {
          newTranslation.x = t.x
        }
        if (newTranslation.y < t.y) {
          newTranslation.y = t.y
        }
      }
    }

    maxTranslation match {
      case None => {}
      case Some(t) => {
        val screenSize = new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight).transformAsVector(getTransforms().inverse)
        newTranslation += screenSize
        if (newTranslation.x > t.x) {
          newTranslation.x = t.x
        }
        if (newTranslation.y > t.y) {
          newTranslation.y = t.y
        }
        newTranslation -= screenSize
      }
    }

    return newTranslation
  }

  def toScreenCoordinates(world:Vector2):Vector2 = world.transform(getTransforms())
  def toWorldCoordinates(screen:Vector2):Vector2 = screen.transform(getTransforms().inverse())

  def getTransforms():Matrix4 = {
    return scaleMatrix * translationMatrix;
  }
}
