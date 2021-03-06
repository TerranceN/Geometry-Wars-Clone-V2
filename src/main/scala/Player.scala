package com.awesome

import org.lwjgl.input._

import scala.math._
import scala.util.Random

import vectors._
import matricies._

class Player(var position:Vector2, val input:Input) {
  val model = new CollidableModel(Player.lineModel)
  var velocity = new Vector2(0, 0)
  val color = new Vector3(1, 1, 1)
  var angle = -Pi / 2
  var isAlive = true

  var lastFiringTime:Long = System.currentTimeMillis
  var firingDelay = 100

  var lastSparkTime:Long = System.currentTimeMillis
  var sparkDelay = 5
  var sparkPages:List[Int] = null
  var numSparksAllocated = 1000
  var sparkIndex:Int = 0
  var random = new Random()

  def readyToFire:Boolean = {
    val timeDiff = System.currentTimeMillis - lastFiringTime
    if (timeDiff > firingDelay) {
      lastFiringTime = System.currentTimeMillis
      return true
    } else {
      return false
    }
  }

  def readyToFireExhaust:Boolean = {
    val timeDiff = System.currentTimeMillis - lastSparkTime
    if (timeDiff > sparkDelay) {
      lastSparkTime = System.currentTimeMillis
      return true
    } else {
      return false
    }
  }

  def update(gamestate:GS_Game, deltaTime:Double) {
    if (sparkPages == null) {
      sparkPages = gamestate.sparkSystem.allocate(numSparksAllocated)
    }
    if (readyToFireExhaust) {
      for (i <- 0 until 14) {
        val pageIndex = sparkIndex / gamestate.sparkSystem.pageSize
        val sparkModPageSize = sparkIndex % gamestate.sparkSystem.pageSize
        var angle = atan2(-velocity.y, -velocity.x)
        val variance = 1
        angle += -(variance.toFloat / 2) + random.nextFloat() * variance
        val newVel = new Vector2(cos(angle).toFloat, sin(angle).toFloat) * velocity.length / 2 * random.nextFloat()
        gamestate.sparkSystem.updatePage(sparkPages(pageIndex), position, newVel, sparkModPageSize, sparkModPageSize + 1, false)
        sparkIndex = (sparkIndex + 1) % numSparksAllocated
      }
    }
    def fireBullets(angle:Double) {
      var middle = position
      var angles = List(-1, 0, 1)
      for (i <- angles) {
        var newAngle = angle + Pi / 90 * 2 * i
        gamestate.addBullet(new Bullet(middle, new Vector2(cos(newAngle).toFloat, sin(newAngle).toFloat) * 1000, newAngle.toFloat))
      }
    }

    var boost = 1.0f
    if (input.getAxis("specials") > 0.25) {
      boost = 2.0f * input.getAxis("specials")
    }

    var mouse = new Vector2(Mouse.getX, GLFrustum.screenHeight - Mouse.getY)
    var transformedMouse = mouse.transform(gamestate.camera.getTransforms.inverse)
    val rightStick = new Vector2(input.getAxis("aimingX"), input.getAxis("aimingY"))
    if (readyToFire && boost <= 1) {
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
      acceleration = stick.normalized * min(stick.length, 1).toFloat * 2000 * boost
    }

    val oldPosition = position.copy
    val oldVelocity = velocity.copy

    position = oldPosition + oldVelocity * deltaTime.toFloat
    velocity = oldVelocity + acceleration * deltaTime.toFloat - oldVelocity * 5 * deltaTime.toFloat

    if (velocity.length > 0) {
      angle = atan2(velocity.y, velocity.x).toFloat
    }

    model.modelTranslation = Matrix4.translate(position) 
    model.modelScale = Matrix4.scale(12, 12, 1)
    model.modelRotation = Matrix4.rotateZ(angle.toFloat)
  }

  def draw() {
    model.draw(color)
  }
}

object Player {
  val lineModel = new LineModel(LineModel.lineLoop(List(
    new Vector2(1, 0),
    new Vector2(-1, 0.75f),
    new Vector2(-0.5f, 0),
    new Vector2(-1, -0.75f)
  )))
}
