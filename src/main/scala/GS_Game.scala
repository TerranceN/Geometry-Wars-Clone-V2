package com.awesome 

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
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
  class Bullet(var position:Vector2, var velocity:Vector2) {
    var timeAlive:Float = 0
    var pushStrength:Float = 0
    var isAlive = true
    val timeToMax = 0.25f
    def update(deltaTime:Double) {
      if (timeAlive < timeToMax) {
        pushStrength = timeAlive / timeToMax
      } else {
        pushStrength = 1
      }
      pushStrength = pushStrength * (velocity.length / 600)
      timeAlive += deltaTime.toFloat
      position += velocity * deltaTime.toFloat

      if (position.x < 0 || position.y < 0 || position.x > GLFrustum.screenWidth || position.y > GLFrustum.screenHeight) {
        isAlive = false
      }
    }
  }

  val random = new Random

  var screenFBO:Framebuffer = null
  var bloomFBO:Framebuffer = null

  var camera = new Camera()

  var wasMouse0Down = false
  var wasKeyBDown = false

  var bloomEnabled = true

  val accelerationShader = new ShaderProgram(
    new VertexShader("shaders/test.vert"),
    new FragmentShader("shaders/test.frag")
  )

  val mainSceneShader = new ShaderProgram(
    new VertexShader("shaders/mainScene.vert"),
    new FragmentShader("shaders/mainScene.frag")
  )

  val blurShader = new ShaderProgram(
    new VertexShader("shaders/blur.vert"),
    new FragmentShader("shaders/blur.frag")
  )

  val additiveShader = new ShaderProgram(
    new VertexShader("shaders/mainScene.vert"),
    new FragmentShader("shaders/additive.frag")
  )

  var angle:Double = 0

  var bulletList:List[Bullet] = Nil

  var gbuf = new GBuffer()
  //gbuf.setup(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt, 241, 141)
  gbuf.setup(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt, 121, 71)

  def init() = {
    screenFBO = new Framebuffer(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt)
    screenFBO.newTexture("scene", GL_RGBA32F, null)

    bloomFBO = new Framebuffer(screenFBO.fboWidth, screenFBO.fboHeight)
    bloomFBO.newTexture("bloom", GL_RGBA, null)
    bloomFBO.newTexture("bloom_halfblur", GL_RGBA, null)
  }

  def update(deltaTime:Double) = {
    angle += 5 * deltaTime.toFloat


    if (!wasKeyBDown && Keyboard.isKeyDown(Keyboard.KEY_B)) {
      bloomEnabled = !bloomEnabled
    }

    if (!wasMouse0Down && Mouse.isButtonDown(0)) {
      var middle = new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight) * 0.5f
      var mouse = new Vector2(Mouse.getX, Mouse.getY)
      var diff = mouse - middle
      var angle = atan2(diff.y, diff.x)
      for (i <- -1 to 1) {
        var newAngle = angle + Pi / 20 * i
        bulletList = new Bullet(middle, new Vector2(cos(newAngle).toFloat, sin(newAngle).toFloat) * 700) :: bulletList
      }
    }

    for (b <- bulletList) {
      b.update(deltaTime)
    }

    gbuf.accelerationPass {
      accelerationShader.bind()
        var mouse = new Vector2(Mouse.getX, GLFrustum.screenHeight - Mouse.getY)
        var transformedMouse = mouse.transform(camera.getTransforms.inverse)
        bulletList.zipWithIndex.foreach{ case (b, i) =>
          accelerationShader.setUniform2f("uPushPositions[" + (i) + "]", b.position.x, b.position.y)
          accelerationShader.setUniform2f("uPushVelocity[" + (i) + "]", b.velocity.x, b.velocity.y)
          accelerationShader.setUniform1f("uPushStrength[" + (i) + "]", b.pushStrength)
          accelerationShader.setUniform1f("uPushSize[" + (i) + "]", 25f)
        }
        accelerationShader.setUniform2f("uPushPositions[" + bulletList.size + "]", transformedMouse.x, GLFrustum.screenHeight - transformedMouse.y)
        accelerationShader.setUniform2f("uPushVelocity[" + bulletList.size + "]", 0, 0)
        accelerationShader.setUniform1f("uPushStrength[" + bulletList.size + "]", -0.5f)
        accelerationShader.setUniform1f("uPushSize[" + bulletList.size + "]", 150f)
        accelerationShader.setUniform1i("uNumPositions", bulletList.size + 1);
        gbuf.accelerationFBO.drawFBOQuad()
      accelerationShader.unbind()
    }

    gbuf.update(deltaTime)

    var temp:List[Bullet] = List()
    for (b <- bulletList) {
      if (b.isAlive) {
        temp = b :: temp
      }
    }
    bulletList = temp

    wasMouse0Down = Mouse.isButtonDown(0)
    wasKeyBDown = Keyboard.isKeyDown(Keyboard.KEY_B)

    camera.moveTowardsCenter(new Vector2(Mouse.getX(), GLFrustum.screenHeight - Mouse.getY()), (0.5f / deltaTime).toFloat)
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
    GLFrustum.modelviewMatrix = camera.getTransforms()
    glClear(GL_COLOR_BUFFER_BIT)

    screenFBO.drawToTextures(List("scene")) {
      glClear(GL_COLOR_BUFFER_BIT)
      gbuf.draw()
    }

    if (bloomEnabled) {
      blurShader.bind()
        blurShader.setUniform1f("uBlurSize", 1.0f);
        bloomFBO.drawToTextures(List("bloom_halfblur")) {
          blurShader.setUniform1f("horizontal", 1.0f);
          glActiveTexture(GL_TEXTURE0)
          glBindTexture(GL_TEXTURE_2D, screenFBO.getTexture("scene"))
          blurShader.setUniform1i("uSampler", 0)
          bloomFBO.drawFBOQuad()
        }

        bloomFBO.drawToTextures(List("bloom")) {
          blurShader.setUniform1f("horizontal", 0.0f);
          glActiveTexture(GL_TEXTURE0)
          glBindTexture(GL_TEXTURE_2D, bloomFBO.getTexture("bloom_halfblur"))
          blurShader.setUniform1i("uSampler", 0)
          bloomFBO.drawFBOQuad()
        }
      blurShader.unbind()

      screenFBO.drawToTextures(List("scene")) {
        additiveShader.bind()
          glActiveTexture(GL_TEXTURE0)
          glBindTexture(GL_TEXTURE_2D, bloomFBO.getTexture("bloom"))
          glActiveTexture(GL_TEXTURE1)
          glBindTexture(GL_TEXTURE_2D, screenFBO.getTexture("scene"))
          additiveShader.setUniform1i("uSampler", 0)
          additiveShader.setUniform1i("uExistingSampler", 1)
          bloomFBO.drawFBOQuad()
        additiveShader.unbind()
      }
    }

    mainSceneShader.bind()
      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, screenFBO.getTexture("scene"))
      mainSceneShader.setUniform1i("uSampler", 0)
      screenFBO.drawFBOQuad()
    mainSceneShader.unbind()

    checkError
  }
}
