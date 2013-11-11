package com.awesome 

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.{
  Display,
  DisplayMode
}
import org.lwjgl.BufferUtils
import org.lwjgl.input._
import scala.util.Random
import scala.math._
import org.lwjgl.util.glu.GLU._

import textures._
import shaders._
import models._
import vectors._
import lighting._
import matricies._

class GS_Game extends GameState {
  val random = new Random
  val screenVBO = glGenBuffers()

  val accelerationShader = new ShaderProgram(
    new VertexShader("shaders/test.vert"),
    new FragmentShader("shaders/test.frag")
  )

  val mainSceneShader = new ShaderProgram(
    new VertexShader("shaders/mainScene.vert"),
    new FragmentShader("shaders/mainScene.frag")
  )

  var y:Double = 0
  var angle:Double = 0

  var gbuf = new GBuffer()
  gbuf.setup(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt, 1000)

  def init() = {
    setupScreenVBO()
  }

  def setupScreenVBO() {
    val vertexBuffer = BufferUtils.createFloatBuffer(16)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(GLFrustum.screenWidth); vertexBuffer.put(0.0f)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(GLFrustum.screenWidth); vertexBuffer.put(GLFrustum.screenHeight)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 0.0f)

    vertexBuffer.put( 0.0f); vertexBuffer.put(GLFrustum.screenHeight)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.flip()

    Console.println(GLFrustum.screenWidth + ", " + GLFrustum.screenHeight)

    glBindBuffer(GL_ARRAY_BUFFER, screenVBO)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
  }

  def drawScreenVBO() {
    GLFrustum.pushProjection()
    //GLFrustum.projectionMatrix.setIdentity

    GLFrustum.pushModelview()
    GLFrustum.modelviewMatrix.setIdentity

    setMatricies()

    val program = ShaderProgram.getActiveShader()

    val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
    val aTexCoordLocation = glGetAttribLocation(program.id, "aTexCoord")

    glEnableVertexAttribArray(aCoordLocation)
    glEnableVertexAttribArray(aTexCoordLocation)

    glBindBuffer(GL_ARRAY_BUFFER, screenVBO)
    glVertexAttribPointer(aCoordLocation, 3, GL_FLOAT, false, 4 * 4, 0)

    glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, false, 4 * 4, 2 * 4)

    glDrawArrays(GL_QUADS, 0, 4)

    glDisableVertexAttribArray(aCoordLocation)
    glDisableVertexAttribArray(aTexCoordLocation)

    GLFrustum.popModelview()
    GLFrustum.popProjection()
  }

  def update(deltaTime:Double) = {
    gbuf.accelerationPass()
      accelerationShader.bind()
        accelerationShader.setUniform2f("uMousePosition", Mouse.getX.toFloat, Mouse.getY.toFloat)
        drawScreenVBO()
      accelerationShader.unbind()
    gbuf.endAccelerationPass()

    gbuf.update(deltaTime)
  }

  def checkError() {
    val error = glGetError
    if (error != GL_NO_ERROR) {
      Console.println("OpenGL Error: " + error)
      Console.println(gluErrorString(error))
    }
  }

  def drawGeometry() {
  }

  def setMatricies() {
    ShaderProgram.activeShader.setUniformMatrix4("uProjectionMatrix", GLFrustum.projectionMatrix.getFloatBuffer)
    ShaderProgram.activeShader.setUniformMatrix4("uModelViewMatrix", GLFrustum.modelviewMatrix.getFloatBuffer)
  }

  def draw() = {
    glViewport(0, 0, GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt)
    GLFrustum.modelviewMatrix.setIdentity()
    glClear(GL_COLOR_BUFFER_BIT)
    glClear(GL_DEPTH_BUFFER_BIT)


    gbuf.draw()

    //mainSceneShader.bind()
    //  glActiveTexture(GL_TEXTURE0)
    //  glBindTexture(GL_TEXTURE_2D, gbuf.getTexture(TextureType.GBUFFER_TEXTURE_TYPE_ACCELERATIONS))
    //  mainSceneShader.setUniform1i("uSampler", 0)
    //  drawScreenVBO()
    //mainSceneShader.unbind()

    //checkError
  }
}
