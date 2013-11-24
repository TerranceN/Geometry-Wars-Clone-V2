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
import scala.math._
import org.lwjgl.util.glu.GLU._

import textures._
import shaders._
import vectors._
import matricies._

class GS_Game extends GameState {
  val sparkSystem = new SparkParticleSystem(250, 50)
  var gameSize = new Vector2(2000, 2000)
  var camera = new Camera()

  var screenFBO:Framebuffer = null
  var bloomFBO:Framebuffer = null

  var wasKeyBDown = false

  var input = new Input(
    Map(
    ),
    Map(
      "movementY" -> new ControllerAxis(0, 0),
      "movementX" -> new ControllerAxis(0, 1),
      //"movementY" -> new KeyboardAxis(Keyboard.KEY_W, Keyboard.KEY_S),
      //"movementX" -> new KeyboardAxis(Keyboard.KEY_A, Keyboard.KEY_D),
      "aimingY" -> new ControllerAxis(0, 2),
      "aimingX" -> new ControllerAxis(0, 3)
    )
  )
  var player = new Player(gameSize / 2, input)

  var pushTexture = new Texture("assets/push.png")

  var bloomEnabled = true

  var controller:Controller = null;

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

  var boundary = new Boundary(camera, gameSize, 20)

  var bulletList:List[Bullet] = Nil

  var gbuf = new GBuffer()
  gbuf.setup(gameSize.x.toInt, gameSize.y.toInt, 161, 161)

  def init() = {
    screenFBO = new Framebuffer(GLFrustum.screenWidth.toInt, GLFrustum.screenHeight.toInt)
    screenFBO.newTexture("scene", GL_RGBA32F, null)

    bloomFBO = new Framebuffer(screenFBO.fboWidth, screenFBO.fboHeight)
    bloomFBO.newTexture("bloom", GL_RGBA, null)
    bloomFBO.newTexture("bloom_halfblur", GL_RGBA, null)
  }

  def addBullet(b:Bullet) {
    bulletList = b :: bulletList
  }

  def drawPush(position:Vector2, strength:Float, size:Float) {
    GLFrustum.pushModelview()
      var drawPos = position - new Vector2(size)
      accelerationShader.setUniform1f("uPushStrength", strength)
      GLFrustum.modelviewMatrix = Matrix4.translate(drawPos) * Matrix4.scale(size * 2, size * 2, 1)
      Texture.drawUnitQuad()
    GLFrustum.popModelview()
  }

  def update(dt:Double):Unit = {
    var deltaTime = dt
    if (!wasKeyBDown && Keyboard.isKeyDown(Keyboard.KEY_B)) {
      bloomEnabled = !bloomEnabled
    }

    if (!Keyboard.isKeyDown(Keyboard.KEY_P)) {
      player.update(this, deltaTime)

      for (b <- bulletList) {
        b.update(this, deltaTime)
      }

      gbuf.accelerationPass {
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)
        accelerationShader.bind()
          glActiveTexture(GL_TEXTURE0)
          pushTexture.bind()
          accelerationShader.setUniform1i("uPushSampler", 0)

          bulletList.zipWithIndex.foreach{ case (b, i) =>
            val aheadPos = b.position + b.velocity * 0.08f
            drawPush(aheadPos, b.pushStrength, 50f)
          }
        accelerationShader.unbind()
        glDisable(GL_BLEND)
      }

      gbuf.update(deltaTime)

      sparkSystem.update(deltaTime, gameSize)

      bulletList = bulletList filter (_.isAlive)
    }

    wasKeyBDown = Keyboard.isKeyDown(Keyboard.KEY_B)

    var lookAt = player.position
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
      player.draw()
      bulletList map (_.draw())
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
