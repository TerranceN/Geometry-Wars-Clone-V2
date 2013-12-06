package com.awesome

import scala.math._

import vectors._
import matricies._

abstract class Enemy(var position:Vector2) {
  var velocity = new Vector2(0)
  var acceleration = new Vector2(0)
  var isAlive = true

  def model():CollidableModel
  def color():Vector3
  def angle:Float
  def scale:Vector2

  def pushAwayFromEnemy(e:Enemy, strength:Float) {
    val diffDir = (position - e.position).normalized
    acceleration += diffDir * strength
  }

  def handleCollisionWithEnemy(e:Enemy, deltaTime:Double) {
    pushAwayFromEnemy(e, 5000)
  }

  def kill() {
    isAlive = false
  }

  def update(gamestate:GS_Game, deltaTime:Double):Unit

  def updateEnemy(gamestate:GS_Game, deltaTime:Double) {
    update(gamestate, deltaTime)

    model.modelTranslation = Matrix4.translate(position)
    model.modelScale = Matrix4.scale(scale.y, scale.x, 1)
    model.modelRotation = Matrix4.rotateZ(angle)
  }

  def draw() {
    model.draw(color)
  }
}
