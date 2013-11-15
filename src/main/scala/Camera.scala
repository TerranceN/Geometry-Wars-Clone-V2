package com.awesome

import matricies._
import vectors._

class Camera {
  var translationMatrix:Matrix4 = new Matrix4()

  def moveTowardsCenter(center:Vector2, divisor:Float) {
    // get current position from matrix
    var currentTranslation = new Vector2().transform(translationMatrix) * -1
    // find difference
    var screenCenter = new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight) / 2
    var diff = center - (screenCenter + currentTranslation)
    // make new translation matrix that's incremented by the difference / divisor
    translationMatrix = Matrix4.translate((currentTranslation + diff / divisor) * -1)
  }

  def getTransforms():Matrix4 = {
    return translationMatrix;
  }
}
