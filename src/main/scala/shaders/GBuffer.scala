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

object TextureType extends Enumeration {
  val GBUFFER_TEXTURE_TYPE_POSITIONS = 0
  val GBUFFER_TEXTURE_TYPE_VELOCITIES = 1
  val GBUFFER_TEXTURE_TYPE_ACCELERATIONS = 2
  val GBUFFER_NUM_TEXTURES = 3
}

class GBuffer {
  import TextureType._

  var isSetup = false

  var numParticlesWidth = 50
  var numParticlesHeight = 50

  var vbo = glGenBuffers()
  var drawVBO = glGenBuffers()

  val random = new Random()

  var particleUpdateShader:ShaderProgram = null
  var particleDrawShader:ShaderProgram = null

  var textures = BufferUtils.createIntBuffer(GBUFFER_NUM_TEXTURES)
  var fbo = 0
  var renderBuffer = 0

  var screenWidth = 0
  var screenHeight = 0

  def setup(screenWidth:Int, screenHeight:Int, numParticles:Int) {
    this.screenWidth = screenWidth
    this.screenHeight = screenHeight
    numParticlesWidth = numParticles
    numParticlesHeight = numParticles

    if (!isSetup) {
      loadShaders()
      isSetup = true
    }

    setupScreenVBO()
    setupDrawVBO()
    setupFBO()
  }

  def setupDrawVBO() {
    val vertexBuffer = BufferUtils.createFloatBuffer(numParticlesWidth * numParticlesHeight * 2)
    for (i <- 0 until numParticlesWidth) {
      for (j <- 0 until numParticlesHeight) {
        vertexBuffer.put(i); vertexBuffer.put(j)
      }
    }
    vertexBuffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, drawVBO)
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
    particleUpdateShader = new ShaderProgram("shaders/particleUpdate.vert", "shaders/particleUpdate.frag")
    particleDrawShader = new ShaderProgram("shaders/particleDraw.vert", "shaders/particleDraw.frag")
  }

  def randRange(min:Float, max:Float):Float = {
    return min + random.nextFloat() * (max - min)
  }

  def setupFBO():Boolean = {
    // delete objects in case screen size changed
    glDeleteTextures(textures)
    glDeleteRenderbuffers(renderBuffer)
    glDeleteFramebuffers(fbo)

    // create an fbo
    fbo = glGenFramebuffers()
    glBindFramebuffer(GL_FRAMEBUFFER, fbo)

    var initPositions = BufferUtils.createFloatBuffer(numParticlesWidth * numParticlesHeight * 4)
    for (i <- 0 until numParticlesWidth * numParticlesHeight) {
      initPositions.put(randRange(0, screenWidth))
      initPositions.put(randRange(0, screenHeight))
      initPositions.put(0)
      initPositions.put(1)
    }
    initPositions.flip()

    // create all gbuffer textures
    glGenTextures(textures)

    // albedo/diffuse (16-bit channel rgba)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_POSITIONS))
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, numParticlesWidth, numParticlesHeight, 0, GL_RGBA, GL_FLOAT, initPositions)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_POSITIONS, GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_POSITIONS), 0)

    // albedo/diffuse (16-bit channel rgba)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_VELOCITIES))
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, numParticlesWidth, numParticlesHeight, 0, GL_RGBA, GL_FLOAT, null:FloatBuffer)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_VELOCITIES, GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_VELOCITIES), 0)

    // albedo/diffuse (16-bit channel rgba)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_ACCELERATIONS))
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, screenWidth, screenHeight, 0, GL_RGBA, GL_FLOAT, null:FloatBuffer)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_ACCELERATIONS, GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_ACCELERATIONS), 0)

    // check status
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)

    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glBindRenderbuffer(GL_RENDERBUFFER, 0)
    glBindTexture(GL_TEXTURE_2D, 0)

    if (status != GL_FRAMEBUFFER_COMPLETE) {
      Console.println("GBuffer::setupFbo()" + "Could not create framebuffer")
      return false
    }

    return true
  }

  def getTexture(texType:Int):Int = {
    return textures.get(texType);
  }

  def accelerationPass() {
    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    glViewport(0, 0, screenWidth, screenHeight)

    val buffer = BufferUtils.createIntBuffer(1)
    buffer.put(GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_ACCELERATIONS)
    buffer.flip()

    glDrawBuffers(buffer)

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)
  }

  def endAccelerationPass() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glDrawBuffer(GL_BACK)
  }

  def update(deltaTime:Double) {
    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    glViewport(0, 0, numParticlesWidth, numParticlesHeight)

    val buffer = BufferUtils.createIntBuffer(2)
    buffer.put(GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_POSITIONS)
    buffer.put(GL_COLOR_ATTACHMENT0 + GBUFFER_TEXTURE_TYPE_VELOCITIES)
    buffer.flip()

    glDrawBuffers(buffer)

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)

    particleUpdateShader.bind()
    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_POSITIONS))
    glActiveTexture(GL_TEXTURE1)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_VELOCITIES))
    glActiveTexture(GL_TEXTURE2)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_ACCELERATIONS))
    particleUpdateShader.setUniform1i("uPositionSampler", 0)
    particleUpdateShader.setUniform1i("uVelocitySampler", 1)
    particleUpdateShader.setUniform1i("uAccelerationSampler", 2)
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
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glDrawBuffer(GL_BACK)
  }

  def draw() {
    particleDrawShader.bind()

    val program = ShaderProgram.getActiveShader()

    GLFrustum.setMatricies()

    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_POSITIONS))
    glActiveTexture(GL_TEXTURE1)
    glBindTexture(GL_TEXTURE_2D, textures.get(GBUFFER_TEXTURE_TYPE_VELOCITIES))
    program.setUniform1i("uPositionSampler", 0)
    program.setUniform1i("uVelocitySampler", 1)

    val aCoordLocation = glGetAttribLocation(program.id, "aCoord")

    glEnableVertexAttribArray(aCoordLocation)

    glBindBuffer(GL_ARRAY_BUFFER, drawVBO)
    glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 4 * 4, 0)

    glDrawArrays(GL_POINTS, 0, numParticlesWidth * numParticlesHeight)

    glDisableVertexAttribArray(aCoordLocation)

    particleDrawShader.unbind()
  }
}
