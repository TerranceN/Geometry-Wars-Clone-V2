package com.awesome

import org.lwjgl.input._

import scala.math._

import vectors._
import matricies._

class Player(var position:Vector2, val input:Input) {
  var velocity = new Vector2(0, 0)
  val color = new Vector3(1, 1, 1)
  var angle = 0f

  var lastFiringTime:Long = 0
  var firingDelay = 100

  def readyToFire:Boolean = {
    if (System.currentTimeMillis - lastFiringTime > firingDelay) {
      lastFiringTime = System.currentTimeMillis
      return true
    } else {
      return false
    }
  }

  def update(gamestate:GS_Game, deltaTime:Double) {
    def fireBullets(angle:Double) {
      var middle = position
      var angles = List(-1, 0, 1)
      for (i <- angles) {
        var newAngle = angle + Pi / 90 * 2 * i
        gamestate.addBullet(new Bullet(middle, new Vector2(cos(newAngle).toFloat, sin(newAngle).toFloat) * 750, newAngle.toFloat))
      }
    }

    var mouse = new Vector2(Mouse.getX, GLFrustum.screenHeight - Mouse.getY)
    var transformedMouse = mouse.transform(gamestate.camera.getTransforms.inverse)
    val rightStick = new Vector2(input.getAxis("aimingX"), input.getAxis("aimingY"))
    if (readyToFire) {
      if (rightStick.length > 0.4) {
        fireBullets(atan2(rightStick.y, rightStick.x))
      }

      if (Mouse.isButtonDown(0)) {
        var middle = position
        var mouse = transformedMouse
        var diff = mouse - middle
        fireBullets(atan2(diff.y, diff.x))
      }
    }

    val stick = new Vector2(input.getAxis("movementX"), input.getAxis("movementY"))
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
