package com.awesome

import org.lwjgl.input._

import scala.math._

import vectors._
import matricies._

class Player(var position:Vector2) {
  var velocity = new Vector2(0, 0)
  val color = new Vector3(1, 1, 1)
  var angle = 0f

  def update(controller:Controller, deltaTime:Double) {
    val stick = new Vector2(controller.getXAxisValue, controller.getYAxisValue)
    var acceleration = new Vector2(0, 0)
    if (stick.length > 0.5) {
      acceleration = stick.normalized * stick.length * 2000
    }

    val oldPosition = position.copy
    val oldVelocity = velocity.copy

    position = oldPosition + oldVelocity * deltaTime.toFloat
    velocity = oldVelocity + acceleration * deltaTime.toFloat - oldVelocity * 5 * deltaTime.toFloat

    angle = atan2(velocity.y, velocity.x).toFloat
  }

  def draw() {
    GLFrustum.pushModelview()
      GLFrustum.modelviewMatrix.multiplyBy(Matrix4.translate(position) * Matrix4.scale(12, 12, 1) * Matrix4.rotateZ(angle))
      Player.model.draw(color)
    GLFrustum.popModelview()
  }
}

object Player {
  val model = new LineModel(List(
    new Vector2(1, 0),
    new Vector2(-1, 0.75f),

    new Vector2(-1, 0.75f),
    new Vector2(-0.5f, 0),

    new Vector2(-0.5f, 0),
    new Vector2(-1, -0.75f),

    new Vector2(-1, -0.75f),
    new Vector2(1, 0)
  ))
}
