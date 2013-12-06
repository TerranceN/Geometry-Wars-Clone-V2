package com.awesome

import scala.math._

import vectors._
import matricies._

class SnakeEnemySegment(pos:Vector2, var follow:Option[SnakeEnemySegment], val segmentNum:Int, var stillSpawning:Boolean) extends Enemy(pos) {
  def this(pos:Vector2) = this(pos, None, 0, true)
  def this(pos:Vector2, follow:SnakeEnemySegment, stillSpawning:Boolean) = this(pos, Some(follow), follow.segmentNum + 1, stillSpawning)

  val __model = new CollidableModel(SnakeEnemySegment.lineModel)
  var __angle = 0f

  var followedBy:Option[SnakeEnemySegment] = None

  var movementAngle = 0f

  val turnSpeed = 1.5f
  val maxSegments = 10
  val timeToSpawn = 1000
  var endSpawnTimer = System.currentTimeMillis

  def model = __model
  def angle = __angle
  def angle_=(a:Float) { __angle = a }
  def scale = new Vector2(40, 25)
  def color:Vector3 = {
    if (follow == None) {
      return new Vector3(1, 0, 0)
    } else if (followedBy == None && !stillSpawning) {
      return new Vector3(1, 1, 0)
    } else {
      return new Vector3(1, 0, 1)
    }
  }

  def findRoot(e:SnakeEnemySegment):SnakeEnemySegment = {
    e.follow match {
      case Some(seg) => { return findRoot(seg) }
      case None => { return e }
    }
  }

  override def handleCollisionWithEnemy(e:Enemy, deltaTime:Double) {
    follow match {
      case Some(seg) => {
        if (seg == e) {
          pushAwayFromEnemy(e, 1000)
        }
      }
      case None => {
        e match {
          case sSeg:SnakeEnemySegment => {
            if (findRoot(sSeg) == this) {
              return
            }
          }
          case _ => {}
        }
        if (velocity.length > 0 && e.velocity.length > 0) {
          val diff = e.position - position
          val dot = velocity.dot(diff)
          if (dot > 0) {
            var bounceOff = e.velocity
            if (dot > 0.8) {
              bounceOff = new Vector2(-diff.y, diff.x)
            }
            var targetVel = velocity - velocity.perp(bounceOff) * 2
            movementAngle = atan2(targetVel.y, targetVel.x).toFloat
          }
        }
      }
    }
  }

  def segmentMapUp(f: SnakeEnemySegment => Unit)(seg:Option[SnakeEnemySegment]) {
    def innerMap(s:Option[SnakeEnemySegment]):Unit = s map { seg =>
      f(seg)
      innerMap(seg.follow)
    }
    innerMap(seg)
  }

  def segmentMapDown(f: SnakeEnemySegment => Unit)(seg:Option[SnakeEnemySegment]) {
    def innerMap(s:Option[SnakeEnemySegment]):Unit = s map { seg =>
      f(seg)
      innerMap(seg.followedBy)
    }
    innerMap(seg)
  }

  override def kill() {
    if (!stillSpawning) {
      if (followedBy == None) {
        isAlive = false
        follow map { section =>
          section.endSpawnTimer = System.currentTimeMillis
          section.followedBy = None
        }
      }
    }
  }

  def update(gamestate:GS_Game, deltaTime:Double) {
    follow = follow match {
      case None => None
      case Some(seg) => {
        if (!seg.isAlive) {
          seg.follow
        } else {
          Some(seg)
        }
      }
    }

    var spawnDelay = timeToSpawn
    
    if (stillSpawning) {
      spawnDelay = timeToSpawn / 5
    }
    if (segmentNum < maxSegments && followedBy == None && System.currentTimeMillis - endSpawnTimer >= spawnDelay) {
      val nextStillSpawning = stillSpawning && (segmentNum <= maxSegments / 2)
      val newSegment = new SnakeEnemySegment(position - velocity.normalized * 10, this, nextStillSpawning)
      gamestate.enemyList = newSegment :: gamestate.enemyList
      followedBy = Some(newSegment)

      stillSpawning = false
    }

    var speed = 4000

    follow match {
      case None => {
        val diffToPlayer = gamestate.player.position - position
        val targetAngle = atan2(diffToPlayer.y, diffToPlayer.x).toFloat
        val turnDir = (movementAngle - targetAngle + (2 * Pi)) % (2 * Pi) > Pi
        if (turnDir) {
          movementAngle += turnSpeed * deltaTime.toFloat
          movementAngle = movementAngle % (2 * Pi).toFloat
        } else {
          movementAngle -= turnSpeed * deltaTime.toFloat
          movementAngle = movementAngle % (2 * Pi).toFloat
        }
        acceleration += new Vector2(cos(movementAngle).toFloat, sin(movementAngle).toFloat) * speed
      }
      case Some(seg) => {
        val diffToSegment = seg.position - position
        acceleration += diffToSegment.normalized * speed * max(1f, diffToSegment.length / 50)
      }
    }

    val oldPosition = position.copy
    val oldVelocity = velocity.copy

    position = oldPosition + oldVelocity * deltaTime.toFloat
    velocity = oldVelocity + acceleration * deltaTime.toFloat - oldVelocity * 10 * deltaTime.toFloat
    acceleration = new Vector2(0)

    angle = atan2(velocity.y, velocity.x).toFloat
    follow match {
      case None => {}
      case Some(seg) => {
        movementAngle = atan2(velocity.y, velocity.x).toFloat
      }
    }
  }
}
object SnakeEnemySegment {
  val lineModel = new LineModel(LineModel.lineLoop(List(
    new Vector2(1, 0),
    new Vector2(-1, 0.75f),
    new Vector2(-0.25f, 0),
    new Vector2(-1, -0.75f)
  )))
}
