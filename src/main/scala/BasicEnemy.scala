package com.awesome

import scala.math._

import vectors._
import matricies._

class BasicEnemy(pos:Vector2) extends Enemy(pos) {
  val __model = new CollidableModel(BasicEnemy.lineModel)
  var __angle:Float = 0f

  def model() = __model
  def angle = __angle
  def angle_=(a:Float) { __angle = a }
  def scale = new Vector2(10)
  def color() = new Vector3(0, 1, 0)

  def update(gamestate:GS_Game, deltaTime:Double) {
    if (isAlive) {
      val diffToPlayer = gamestate.player.position - position
      var accDir = diffToPlayer
      if (diffToPlayer.dot(gamestate.player.velocity) > 0) {
        val weight = min(1f, gamestate.player.velocity.length / velocity.length)
        accDir = gamestate.player.velocity * weight + accDir * (1 - weight)
      }
      acceleration += accDir.normalized * 2000

      val oldPosition = position.copy
      val oldVelocity = velocity.copy

      position = oldPosition + oldVelocity * deltaTime.toFloat
      velocity = oldVelocity + acceleration * deltaTime.toFloat - oldVelocity * 5 * deltaTime.toFloat
      acceleration = new Vector2(0)

      angle = atan2(velocity.y, velocity.x).toFloat

      if (gamestate.player.model.isCollidingWith(model)) {
        gamestate.player.isAlive = false
        for (e <- gamestate.enemyList) {
          e.isAlive = false
        }
        for (b <- gamestate.bulletList) {
          b.isAlive = false
        }
      }
    }
  }
}
object BasicEnemy {
  val lineModel = new LineModel(LineModel.lineLoop(List(
    new Vector2(-1, -1),
    new Vector2( 1, -0.5f),
    new Vector2( 1,  0.5f),
    new Vector2(-1,  1)
  )))
}
