package com.awesome 

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.{
  Display, 
  DisplayMode
}

class GS_Init extends GameState {
  def init() = {
    // screen size
    val screenWidth = 1280
    val screenHeight = 720
    val displayMode = new DisplayMode(screenWidth, screenHeight)

    // create a new window
    Display.setTitle("LWJGL Test")
    Display.setDisplayMode(displayMode)
    Display.create()

    // opengl settings
    glClearColor(0f, 0f, 0f, 1f)
    GLFrustum.setOrtho(0, screenWidth, screenHeight, 0, -1, 1)

    glDisable(GL_DEPTH_TEST)
    glShadeModel(GL_SMOOTH);   // Enable smooth shading

    // next next state and kill this state
    setNextState(new GS_Game)
    killState
  }

  def update(deltaTime:Double) = {}
  def draw() = {}
}
