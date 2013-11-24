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
  val sparkSystem = new SparkParticleSystem(250, 50)
  var gameSize = new Vector2(1500, 1500)
  //class SparkEmitter(val center:Vector2, val radius:Float, val maxLife:Float) {
  //  def update(deltaTime:Double) {
  //  }

  //  def draw() {
  //  }
  //}
  class Bullet(var position:Vector2, var velocity:Vector2) {
    var timeAlive:Float = 0
    var pushStrength:Float = 0
    var isAlive = true
    val timeToMax = 0.25f
    def update(deltaTime:Double) {
      if (isAlive) {
        if (timeAlive < timeToMax) {
          pushStrength = timeAlive / timeToMax
        } else {
          pushStrength = 1
        }
        pushStrength = pushStrength * (velocity.length / 600) * 0.55f
        timeAlive += deltaTime.toFloat
        position += velocity * deltaTime.toFloat

        if (position.x < 0 || position.y < 0 || position.x > gameSize.x || position.y > gameSize.y) {
          isAlive = false
          var pages = sparkSystem.allocate(sparkSystem.pageSize)
          pages map (x => sparkSystem.updatePage(x, position, velocity))
          sparkSystem.deallocate(pages)
        }
      }
    }
  }
  var camera = new Camera()
  class Boundary(val padding:Float) {
    camera.setBoundaries(new Vector2(-padding), gameSize - new Vector2(GLFrustum.screenWidth, GLFrustum.screenHeight) + new Vector2(padding))

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
      val buffer = BufferUtils.createFloatBuffer(4 * 4 * 2)
      buffer.put(-padding); buffer.put(-padding)
      buffer.put(0); buffer.put(0)
      buffer.put(gameSize.x); buffer.put(0)
      buffer.put(gameSize.x + padding); buffer.put(-padding)

      buffer.put(gameSize.x + padding); buffer.put(-padding)
      buffer.put(gameSize.x); buffer.put(0)
      buffer.put(gameSize.x); buffer.put(gameSize.y)
      buffer.put(gameSize.x + padding); buffer.put(gameSize.y + padding)

      buffer.put(gameSize.x + padding); buffer.put(gameSize.y + padding)
      buffer.put(gameSize.x); buffer.put(gameSize.y)
      buffer.put(0); buffer.put(gameSize.y)
      buffer.put(-padding); buffer.put(gameSize.y + padding)

      buffer.put(-padding); buffer.put(gameSize.y + padding)
      buffer.put(0); buffer.put(gameSize.y)
      buffer.put(0); buffer.put(0)
      buffer.put(-padding); buffer.put(-padding)

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

  val random = new Random

  var screenFBO:Framebuffer = null
  var bloomFBO:Framebuffer = null

  var wasMouse0Down = false
  var wasMouse1Down = false
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

  var boundary = new Boundary(20)

  var angle:Double = 0

  var bulletList:List[Bullet] = Nil


  var gbuf = new GBuffer()
  //gbuf.setup(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt, 241, 141)
  //gbuf.setup(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt, 121, 71)
  //gbuf.setup(2560, 2560, 481, 481)
  gbuf.setup(gameSize.x.toInt, gameSize.y.toInt, 121, 121)

  def init() = {
    screenFBO = new Framebuffer(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt)
    screenFBO.newTexture("scene", GL_RGBA32F, null)

    bloomFBO = new Framebuffer(screenFBO.fboWidth, screenFBO.fboHeight)
    bloomFBO.newTexture("bloom", GL_RGBA, null)
    bloomFBO.newTexture("bloom_halfblur", GL_RGBA, null)
  }

  def update(dt:Double):Unit = {
    var deltaTime = dt
    if (Keyboard.isKeyDown(Keyboard.KEY_P)) return
    var mouse = new Vector2(Mouse.getX, GLFrustum.screenHeight - Mouse.getY)
    var transformedMouse = mouse.transform(camera.getTransforms.inverse)

    angle += 5 * deltaTime.toFloat

    if (!wasKeyBDown && Keyboard.isKeyDown(Keyboard.KEY_B)) {
      bloomEnabled = !bloomEnabled
    }

    if (!wasMouse0Down && Mouse.isButtonDown(0)) {
      var middle = gameSize * 0.5f
      var mouse = transformedMouse
      var diff = mouse - middle
      var angle = atan2(diff.y, diff.x)
      var angles = List(-1, 0, 1)
      for (i <- angles) {
        var newAngle = angle + Pi / 90 * 2 * i
        bulletList = new Bullet(middle, new Vector2(cos(newAngle).toFloat, sin(newAngle).toFloat) * 1000) :: bulletList
      }
    }

    if ((!wasMouse1Down && Mouse.isButtonDown(1)) || Mouse.isButtonDown(2)) {
      var pages = sparkSystem.allocate(sparkSystem.pageSize)
      pages map (x => sparkSystem.updatePage(x, transformedMouse))
      sparkSystem.deallocate(pages)
    }

    for (b <- bulletList) {
      b.update(deltaTime)
    }

    gbuf.accelerationPass {
      accelerationShader.bind()
        bulletList.zipWithIndex.foreach{ case (b, i) =>
          accelerationShader.setUniform2f("uPushPositions[" + (i) + "]", b.position.x, b.position.y)
          accelerationShader.setUniform2f("uPushVelocity[" + (i) + "]", b.velocity.x, b.velocity.y)
          accelerationShader.setUniform1f("uPushStrength[" + (i) + "]", b.pushStrength)
          accelerationShader.setUniform1f("uPushSize[" + (i) + "]", 50f)
        }
        accelerationShader.setUniform2f("uPushPositions[" + bulletList.size + "]", transformedMouse.x, transformedMouse.y)
        //accelerationShader.setUniform2f("uPushPositions[" + bulletList.size + "]", Mouse.getX, Mouse.getY)
        accelerationShader.setUniform2f("uPushVelocity[" + bulletList.size + "]", 0, 0)
        accelerationShader.setUniform1f(
          "uPushStrength[" + bulletList.size + "]",
          -(new Vector2(Mouse.getDX(), Mouse.getDY()).length / 5) / (deltaTime.toFloat * 300)
        )
        accelerationShader.setUniform1f("uPushSize[" + bulletList.size + "]", 350f)
        accelerationShader.setUniform1i("uNumPositions", bulletList.size + 1);
        accelerationShader.setUniform2f("uScreenSize", GLFrustum.screenWidth, GLFrustum.screenHeight)
        gbuf.accelerationFBO.drawFBOQuad()
      accelerationShader.unbind()
    }

    gbuf.update(deltaTime)

    sparkSystem.update(deltaTime, gameSize)

    bulletList = bulletList filter (_.isAlive)

    wasMouse0Down = Mouse.isButtonDown(0)
    wasMouse1Down = Mouse.isButtonDown(1)
    wasKeyBDown = Keyboard.isKeyDown(Keyboard.KEY_B)

    var lookAt = new Vector2(
      mouse.x / GLFrustum.screenWidth * gameSize.x,
      mouse.y / GLFrustum.screenHeight * gameSize.y
    )
    camera.moveTowardsCenter(lookAt, (0.25f / deltaTime).toFloat)
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

    glEnable(GL_BLEND)
    glBlendFunc(GL_ONE, GL_ONE)

    screenFBO.drawToTextures(List("scene")) {
      glClear(GL_COLOR_BUFFER_BIT)
      gbuf.draw()
      sparkSystem.draw()
      boundary.draw()
    }

    glDisable(GL_BLEND)

    if (bloomEnabled) {
      var input = screenFBO.getTexture("scene")
      for (i <- 0 until 4) {
        blurShader.bind()
          blurShader.setUniform1f("uBlurSize", 1.0f);
          bloomFBO.drawToTextures(List("bloom_halfblur")) {
            glClear(GL_COLOR_BUFFER_BIT)
            blurShader.setUniform1f("horizontal", 1.0f);
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, input)
            blurShader.setUniform1i("uSampler", 0)
            bloomFBO.drawFBOQuad()
          }

          bloomFBO.drawToTextures(List("bloom")) {
            glClear(GL_COLOR_BUFFER_BIT)
            blurShader.setUniform1f("horizontal", 0.0f);
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, bloomFBO.getTexture("bloom_halfblur"))
            blurShader.setUniform1i("uSampler", 0)
            bloomFBO.drawFBOQuad()
          }
        blurShader.unbind()
        input = bloomFBO.getTexture("bloom")
      }

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
      //glBindTexture(GL_TEXTURE_2D, gbuf.accelerationFBO.getTexture("accelerations"))
      //glBindTexture(GL_TEXTURE_2D, sparkSystem.fbo.getTexture("velocities"))
      mainSceneShader.setUniform1i("uSampler", 0)
      screenFBO.drawFBOQuad()
      //gbuf.accelerationFBO.drawFBOQuad()
      //sparkSystem.fbo.drawFBOQuad()
    mainSceneShader.unbind()


    checkError
  }
}
