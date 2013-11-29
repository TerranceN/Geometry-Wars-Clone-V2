package com.awesome

import vectors._
import matricies._

class Enemy(var position:Vector2) {
  var velocity = new Vector2(0)
  val model = new CollidableModel(Enemy.lineModel)
  var isAlive = true

  val color = new Vector3(0, 1, 0)

  def update(gamestate:GS_Game, deltaTime:Double) {
    if (isAlive) {
      val diffToPlayer = gamestate.player.position - position
      var acceleration = diffToPlayer.normalized * 1000

      val oldPosition = position.copy
      val oldVelocity = velocity.copy

      position = oldPosition + oldVelocity * deltaTime.toFloat
      velocity = oldVelocity + acceleration * deltaTime.toFloat - oldVelocity * 5 * deltaTime.toFloat
    }
    model.modelTranslationAndScale = Matrix4.translate(position) * Matrix4.scale(10, 10, 1)
  }

  def draw() {
    model.draw(color)
  }
}
object Enemy {
  val lineModel = new LineModel(LineModel.lineLoop(List(
    new Vector2(-1, -1),
    new Vector2( 1, -1),
    new Vector2( 1,  1),
    new Vector2(-1,  1)
  )))
}
