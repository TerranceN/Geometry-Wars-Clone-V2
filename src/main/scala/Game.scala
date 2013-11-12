package com.awesome 

// What GL version you plan on using
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.{
  Display, 
  DisplayMode
}
import org.lwjgl.input.Keyboard
import Keyboard._
import scala.collection.mutable.Stack

object Game extends App {
  Console.println(System.getProperty("java.library.path"))
  val updateFPSCap = 500
  val drawFPSCap = 80
  var gameStates = new Stack[GameState]
  gameStates.push(new GS_Init)

  var threadsExit = false
  var shouldDraw = true

  class GameRunner extends Thread {
    override def run() = {
      gameStates.synchronized {
        // the first thread will make an opengl context, so it needs to be initialized here
        gameStates.head.init
      }
      var exit = false
      var lastFrameTime:Long = 0
      var fpsCountStart:Long = System.nanoTime
      var fpsFrameCount:Int = 0
      while (!exit) {
        var startTime:Long = 0
        gameStates.synchronized {
          threadsExit = Display.isCloseRequested && !gameStates.isEmpty
          exit = threadsExit

          startTime = System.nanoTime
          val currentState = gameStates.head
          // update the current state
          currentState.update(lastFrameTime / 1000000000d)

          // if that update caused the state to die, remove to from the stack
          // otherwise, draw the state
          if (!currentState.isAlive) {
            gameStates.pop
          }

          // since updating includes opengl calls and runs much faster, draw is run from here
          if (shouldDraw) {
            currentState.draw
            Display.update
            shouldDraw = false
          }

          // check if there is a next state
          if (currentState.hasNextState) {
            gameStates.push(currentState.takeNextState)
            gameStates.head.init
          }
        } // nothing else depends on shared data, so exit the synchronized section

        // calculate the time taken and delay the game if enabled
        val endTime = System.nanoTime
        val delayTime = (1000d / updateFPSCap.toDouble) - ((endTime - startTime) / 1000000)
        if (delayTime > 0) Thread.sleep(delayTime.toInt)
        fpsFrameCount += 1
        val fpsTimeDiff = System.nanoTime - fpsCountStart
        if (fpsTimeDiff >= 1000000000) {
          Console.println("fps: " + (1000000000d / (fpsTimeDiff / fpsFrameCount)))
          fpsFrameCount = 0
          fpsCountStart = System.nanoTime
        }
        lastFrameTime = System.nanoTime - startTime
      }
      Display.destroy()
    }
  }

  class DrawScheduler extends Thread {
    override def run() {
      var exit = false
      var fpsCountStart:Long = System.nanoTime
      var fpsFrameCount:Int = 0
      while (!exit) {
        val startTime = System.nanoTime
        gameStates.synchronized {
          exit = threadsExit

          shouldDraw = true
        } // nothing else depends on shared data, so exit the synchronized section
        val endTime = System.nanoTime
        val delayTime = (1000d / drawFPSCap.toDouble) - ((endTime - startTime) / 1000000)
        if (delayTime > 0) Thread.sleep(delayTime.toInt)
        fpsFrameCount += 1
        val fpsTimeDiff = System.nanoTime - fpsCountStart
        if (fpsTimeDiff >= 1000000000) {
          Console.println("fps: " + (1000000000d / (fpsTimeDiff / fpsFrameCount)))
          fpsFrameCount = 0
          fpsCountStart = System.nanoTime
        }
      }
    }
  }

  new Thread(new GameRunner()).start
  new DrawScheduler().run // call act here to it must finish before the future finishes

  sys.exit(0)
}
