package com.awesome

import matricies._
import vectors._

class Camera {
  var translationMatrix:Matrix4 = new Matrix4()

  var minTranslation:Option[Vector2] = None
  var maxTranslation:Option[Vector2] = None

  def setBoundaries(min:Vector2, max:Vector2) {
    minTranslation = Some(min)
    maxTranslation = Some(max)
  }

  def moveTowardsCenter(center:Vector2, divisor:Float) {
    // get current position from matrix
    var currentTranslation = new Vector2().transform(translationMatrix) * -1
    // find difference
    var screenCenter = new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight) / 2
    var diff = center - (screenCenter + currentTranslation)
    var newTranslation = lockTranslationToBoundaries(currentTranslation + diff / divisor)

    // make new translation matrix that's incremented by the difference / divisor
    translationMatrix = Matrix4.translate(newTranslation * -1)
  }

  def lockTranslationToBoundaries(translation:Vector2):Vector2 = {
    var newTranslation = translation

    minTranslation match {
      case None => {}
      case Some(t) => {
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
        if (newTranslation.x > t.x) {
          newTranslation.x = t.x
        }
        if (newTranslation.y > t.y) {
          newTranslation.y = t.y
        }
      }
    }

    return newTranslation
  }

  def getTransforms():Matrix4 = {
    return translationMatrix;
  }
}
