package com.awesome

import vectors._
import matricies._

class Bullet(var position:Vector2, var velocity:Vector2, val angle:Float) {
  var timeAlive:Float = 0
  var pushStrength:Float = 0
  var isAlive = true
  val timeToMax = 0.25f
  val color = new Vector3(1, 1, 0)
  def update(gamestate:GS_Game, deltaTime:Double) {
    if (isAlive) {
      if (timeAlive < timeToMax) {
        pushStrength = timeAlive / timeToMax
      } else {
        pushStrength = 1
      }
      pushStrength = pushStrength * (velocity.length / 800) * 1f
      timeAlive += deltaTime.toFloat
      position += velocity * deltaTime.toFloat

      if (position.x < 0 || position.y < 0 || position.x > gamestate.gameSize.x || position.y > gamestate.gameSize.y) {
        isAlive = false
        var pages = gamestate.sparkSystem.allocate(gamestate.sparkSystem.pageSize)
        pages map (x => gamestate.sparkSystem.updatePage(x, position, velocity))
        gamestate.sparkSystem.deallocate(pages)
      }
    }
  }
  def draw() {
    GLFrustum.pushModelview()
      GLFrustum.modelviewMatrix.multiplyBy(Matrix4.translate(position) * Matrix4.scale(5, 5, 1) * Matrix4.rotateZ(angle))
      Bullet.model.draw(color)
    GLFrustum.popModelview()
  }
}
object Bullet {
  val model = new LineModel(List(
    new Vector2(1, 0),
    new Vector2(-1, 0.75f),

    new Vector2(-1, 0.75f),
    new Vector2(-1, -0.75f),

    new Vector2(-1, -0.75f),
    new Vector2(1, 0)
  ))
}
