package com.awesome

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.BufferUtils

import shaders._
import vectors._

class Boundary(val camera:Camera, val gameSize:Vector2, val padding:Float) {
  camera.setBoundaries(new Vector2(-padding), gameSize + new Vector2(padding))

  val primitiveShader = new ShaderProgram(
    new VertexShader("shaders/primitiveWithColor.vert"),
    new FragmentShader("shaders/primitiveWithColor.frag")
  )

  val borderVBO = glGenBuffers()
  initBorderVBO()

  val blackedOutAreasVBO = glGenBuffers()
  initBlackedOutAreasVBO()

  def initBorderVBO() {
    val buffer = BufferUtils.createFloatBuffer(5 * 2)
    buffer.put(0); buffer.put(0)
    buffer.put(gameSize.x); buffer.put(0)
    buffer.put(gameSize.x); buffer.put(gameSize.y)
    buffer.put(0); buffer.put(gameSize.y)
    buffer.put(0); buffer.put(0)
    buffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, borderVBO)
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
  }

  def initBlackedOutAreasVBO() {
    var blackPadding = gameSize.length()
    val buffer = BufferUtils.createFloatBuffer(4 * 4 * 2)
    buffer.put(-blackPadding); buffer.put(-blackPadding)
    buffer.put(0); buffer.put(0)
    buffer.put(gameSize.x); buffer.put(0)
    buffer.put(gameSize.x + blackPadding); buffer.put(-blackPadding)

    buffer.put(gameSize.x + blackPadding); buffer.put(-blackPadding)
    buffer.put(gameSize.x); buffer.put(0)
    buffer.put(gameSize.x); buffer.put(gameSize.y)
    buffer.put(gameSize.x + blackPadding); buffer.put(gameSize.y + blackPadding)

    buffer.put(gameSize.x + blackPadding); buffer.put(gameSize.y + blackPadding)
    buffer.put(gameSize.x); buffer.put(gameSize.y)
    buffer.put(0); buffer.put(gameSize.y)
    buffer.put(-blackPadding); buffer.put(gameSize.y + blackPadding)

    buffer.put(-blackPadding); buffer.put(gameSize.y + blackPadding)
    buffer.put(0); buffer.put(gameSize.y)
    buffer.put(0); buffer.put(0)
    buffer.put(-blackPadding); buffer.put(-blackPadding)

    buffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, blackedOutAreasVBO)
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
  }

  def drawBorder() {
    val program = ShaderProgram.getActiveShader()

    val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
    program.setUniform4f("uColor", 1, 1, 1, 1)

    glEnableVertexAttribArray(aCoordLocation)

    glBindBuffer(GL_ARRAY_BUFFER, borderVBO)
    glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 2 * 4, 0)

    glDrawArrays(GL_LINE_STRIP, 0, 5)

    glDisableVertexAttribArray(aCoordLocation)
  }

  def drawBlackedOutAreas() {
    val program = ShaderProgram.getActiveShader()

    val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
    program.setUniform4f("uColor", 0, 0, 0, 1)

    glEnableVertexAttribArray(aCoordLocation)

    glBindBuffer(GL_ARRAY_BUFFER, blackedOutAreasVBO)
    glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 2 * 4, 0)

    glDrawArrays(GL_QUADS, 0, 4 * 4)

    glDisableVertexAttribArray(aCoordLocation)
  }

  def draw() {
    primitiveShader.bind()
      GLFrustum.setMatricies()
      glDisable(GL_BLEND)
      // black out edges
      drawBlackedOutAreas()
      // add white border
      drawBorder()
      glEnable(GL_BLEND)
    primitiveShader.unbind()
  }
}
