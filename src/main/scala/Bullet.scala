package com.awesome

import vectors._
import matricies._

class Bullet(var position:Vector2, var velocity:Vector2, val angle:Float) {
  var model = new CollidableModel(Bullet.lineModel)
  var timeAlive:Float = 0
  var pushStrength:Float = 0
  var isAlive = true
  val timeToMax = 0.25f
  val color = new Vector3(1, 1, 0)
  model.modelRotation = Matrix4.rotateZ(angle)
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
      }

      for (enemy <- gamestate.enemyList) {
        if (enemy.model.isCollidingWith(model)) {
          isAlive = false
          enemy.kill()
          if (!enemy.isAlive) {
            enemy.velocity += velocity / 2
          }
        }
      }
    }
    model.modelTranslation = Matrix4.translate(position)
    model.modelScale = Matrix4.scale(5, 5, 1)
  }
  def draw() {
    model.draw(color)
  }
}
object Bullet {
  val lineModel = new LineModel(LineModel.lineLoop(List(
    new Vector2(1, 0),
    new Vector2(-1, 0.75f),
    new Vector2(-1, -0.75f)
  )))
  //val lineModel = new LineModel(LineModel.lineLoop(List(
  //  new Vector2(-1, -1),
  //  new Vector2( 1, -1),
  //  new Vector2( 1,  1),
  //  new Vector2(-1,  1)
  //)))
}
