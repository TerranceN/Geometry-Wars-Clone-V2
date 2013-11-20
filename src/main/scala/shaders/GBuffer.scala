package com.awesome.shaders

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._

import scala.util.Random
import java.nio._
import scala.collection.mutable.ArrayBuffer

import com.awesome.GLFrustum
import com.awesome.vectors._
import com.awesome.matricies._

class GBuffer {
  var isSetup = false

  var numParticlesWidth = 50
  var numParticlesHeight = 50

  var vbo = glGenBuffers()
  var drawHorizontalLinesVBO = glGenBuffers()
  var drawVerticalLinesVBO = glGenBuffers()

  val random = new Random()

  var particleUpdateShader:ShaderProgram = null
  var particleDrawShader:ShaderProgram = null

  var fbo:Framebuffer = null
  var accelerationFBO:Framebuffer = null

  var screenWidth = 0
  var screenHeight = 0

  def setup(screenWidth:Int, screenHeight:Int, particlesWidth:Int, particlesHeight:Int) {
    this.screenWidth = screenWidth
    this.screenHeight = screenHeight
    numParticlesWidth = particlesWidth
    numParticlesHeight = particlesHeight

    if (!isSetup) {
      loadShaders()
      isSetup = true
    }

    setupScreenVBO()
    setupDrawVBO()
    setupFBO()
    setupAccelerationFBO()
  }

  def setupDrawVBO() {
    def offset(i:Int, j:Int, max:Int) = (j % 2) match {
      case 0 => i
      case 1 => (max - i) - 1
    }

    var vertexBuffer = BufferUtils.createFloatBuffer(numParticlesWidth * numParticlesHeight * 2)
    for (j <- 0 until numParticlesHeight) {
      for (i <- 0 until numParticlesWidth) {
        vertexBuffer.put(offset(i, j, numParticlesWidth) + 0.001f); vertexBuffer.put(j + 0.001f)
      }
    }
    vertexBuffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, drawHorizontalLinesVBO)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

    vertexBuffer = BufferUtils.createFloatBuffer(numParticlesWidth * numParticlesHeight * 2)
    for (i <- 0 until numParticlesWidth) {
      for (j <- 0 until numParticlesHeight) {
        vertexBuffer.put(i + 0.001f); vertexBuffer.put(offset(j, i, numParticlesHeight) + 0.001f)
      }
    }
    vertexBuffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, drawVerticalLinesVBO)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
  }

  def setupScreenVBO() {
    val vertexBuffer = BufferUtils.createFloatBuffer(16)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(numParticlesWidth); vertexBuffer.put(0.0f)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(numParticlesWidth); vertexBuffer.put(numParticlesHeight)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 0.0f)

    vertexBuffer.put( 0.0f); vertexBuffer.put(numParticlesHeight)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
  }

  def loadShaders() {
    particleUpdateShader = new ShaderProgram("shaders/gridParticleUpdate.vert", "shaders/gridParticleUpdate.frag")
    particleDrawShader = new ShaderProgram("shaders/gridParticleDraw.vert", "shaders/gridParticleDraw.frag")
  }

  def randRange(min:Float, max:Float):Float = {
    return min + random.nextFloat() * (max - min)
  }

  def setupFBO():Boolean = {
    if (fbo != null) {
      fbo.delete
    }

    fbo = new Framebuffer(numParticlesWidth, numParticlesHeight)

    var initPositions = BufferUtils.createFloatBuffer(numParticlesWidth * numParticlesHeight * 4)
    var xSize:Double = screenWidth.toFloat / (numParticlesWidth + 1)
    var ySize:Double = screenHeight.toFloat / (numParticlesHeight + 1)
    for (j <- 0 until numParticlesHeight) {
      for (i <- 0 until numParticlesWidth) {
        var x:Float = ((i + 1) * xSize).toFloat
        var y:Float = ((j + 1) * ySize).toFloat
        initPositions.put(x)
        initPositions.put(y)
        initPositions.put(0)
        initPositions.put(1)
      }
    }
    initPositions.flip()

    fbo.newTexture("positions", GL_RGBA32F, initPositions)
    fbo.newTexture("offsets", GL_RGBA32F, null)
    fbo.newTexture("velocities", GL_RGBA32F, null)

    // check status
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)

    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glBindTexture(GL_TEXTURE_2D, 0)

    if (status != GL_FRAMEBUFFER_COMPLETE) {
      Console.println("GBuffer::setupFbo()" + "Could not create framebuffer")
      return false
    }

    return true
  }

  def setupAccelerationFBO():Boolean = {
    if (accelerationFBO != null) {
      accelerationFBO.delete
    }

    accelerationFBO = new Framebuffer(screenWidth, screenHeight)
    accelerationFBO.newTexture("accelerations", GL_RGBA32F, null)

    // check status
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)

    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glBindTexture(GL_TEXTURE_2D, 0)

    if (status != GL_FRAMEBUFFER_COMPLETE) {
      Console.println("GBuffer::setupAccelerationFBO()" + "Could not create framebuffer")
      return false
    }

    return true
  }

  def accelerationPass(f: => Unit) {
    accelerationFBO.drawToTextures(List("accelerations")) {
      glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
      glClear(GL_COLOR_BUFFER_BIT)

      f
    }
  }

  def update(deltaTime:Double) {
    fbo.drawToTextures(List("offsets", "velocities")) {
      particleUpdateShader.bind()
      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, fbo.getTexture("positions"))
      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, fbo.getTexture("offsets"))
      glActiveTexture(GL_TEXTURE2)
      glBindTexture(GL_TEXTURE_2D, fbo.getTexture("velocities"))
      glActiveTexture(GL_TEXTURE3)
      glBindTexture(GL_TEXTURE_2D, accelerationFBO.getTexture("accelerations"))
      particleUpdateShader.setUniform1i("uPositionSampler", 0)
      particleUpdateShader.setUniform1i("uOffsetSampler", 1)
      particleUpdateShader.setUniform1i("uVelocitySampler", 2)
      particleUpdateShader.setUniform1i("uAccelerationSampler", 3)
      particleUpdateShader.setUniform1f("uDeltaTime", deltaTime.toFloat)

      val program = ShaderProgram.getActiveShader()

      var projection = Matrix4.ortho(0, numParticlesWidth, numParticlesHeight, 0, -1, 1)
      program.setUniformMatrix4("uProjectionMatrix", projection.getFloatBuffer)

      val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
      val aTexCoordLocation = glGetAttribLocation(program.id, "aTexCoord")

      glEnableVertexAttribArray(aCoordLocation)
      glEnableVertexAttribArray(aTexCoordLocation)

      glBindBuffer(GL_ARRAY_BUFFER, vbo)
      glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 4 * 4, 0)

      glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, false, 4 * 4, 2 * 4)

      glDrawArrays(GL_QUADS, 0, 4)

      glDisableVertexAttribArray(aCoordLocation)
      glDisableVertexAttribArray(aTexCoordLocation)

      particleUpdateShader.unbind()
    }
  }

  def draw() {
    particleDrawShader.bind()

    val program = ShaderProgram.getActiveShader()

    GLFrustum.setMatricies()

    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, fbo.getTexture("positions"))
    glActiveTexture(GL_TEXTURE1)
    glBindTexture(GL_TEXTURE_2D, fbo.getTexture("offsets"))
    glActiveTexture(GL_TEXTURE2)
    glBindTexture(GL_TEXTURE_2D, fbo.getTexture("velocities"))
    program.setUniform1i("uPositionSampler", 0)
    program.setUniform1i("uOffsetSampler", 1)
    program.setUniform1i("uVelocitySampler", 2)

    val aCoordLocation = glGetAttribLocation(program.id, "aCoord")

    glEnableVertexAttribArray(aCoordLocation)

    glBindBuffer(GL_ARRAY_BUFFER, drawHorizontalLinesVBO)
    glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 2 * 4, 0)
    glDrawArrays(GL_LINE_STRIP, 0, numParticlesWidth * numParticlesHeight)

    glBindBuffer(GL_ARRAY_BUFFER, drawVerticalLinesVBO)
    glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 2 * 4, 0)
    glDrawArrays(GL_LINE_STRIP, 0, numParticlesWidth * numParticlesHeight)

    glDisableVertexAttribArray(aCoordLocation)

    particleDrawShader.unbind()
  }
}
