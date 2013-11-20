package com.awesome

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._

import shaders._
import vectors._

class SparkParticleSystem(pageSize:Int, numPages:Int) extends ParticleSystem(pageSize, numPages) {
  val vbo = glGenBuffers()

  var vertexBuffer = BufferUtils.createFloatBuffer(pageSize * numPages * 3 * 2)
  for (j <- 0 until numPages) {
    for (i <- 0 until pageSize) {
      for (k <- 0 until 2) {
        vertexBuffer.put(i + 0.001f); vertexBuffer.put(j + 0.001f); vertexBuffer.put(k + 0.001f)
      }
    }
  }
  vertexBuffer.flip()

  glBindBuffer(GL_ARRAY_BUFFER, vbo)
  glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

  val updateShader = new ShaderProgram(
    new VertexShader("shaders/sparkParticleUpdate.vert"),
    new FragmentShader("shaders/sparkParticleUpdate.frag")
  )

  val drawShader = new ShaderProgram(
    new VertexShader("shaders/sparkParticleDraw.vert"),
    new FragmentShader("shaders/sparkParticleDraw.frag")
  )

  val setShader = new ShaderProgram(
    new VertexShader("shaders/sparkParticleSet.vert"),
    new FragmentShader("shaders/sparkParticleSet.frag")
  )

  def updatePage(page:Int, position:Vector2) {
    fbo.drawToTextures(List("positions", "velocities")) {
      setShader.bind()
        setShader.setUniform2f("uPosition", position.x, GLFrustum.screenHeight - position.y)
          fbo.drawFBOQuad(new Vector2(0, page), new Vector2(pageSize, page + 1))
      setShader.unbind()
    }
  }

  override def update(deltaTime:Double) {
    fbo.drawToTextures(List("positions", "velocities")) {
      updateShader.bind()
        val program = ShaderProgram.getActiveShader()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, fbo.getTexture("positions"))
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, fbo.getTexture("velocities"))
        program.setUniform1i("uPositionSampler", 0)
        program.setUniform1i("uVelocitySampler", 1)
        program.setUniform1f("uDeltaTime", deltaTime.toFloat)

        fbo.drawFBOQuad()

      updateShader.unbind()
    }
  }

  override def draw() {
    drawShader.bind()
      val program = ShaderProgram.getActiveShader()

      GLFrustum.setMatricies()

      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, fbo.getTexture("positions"))
      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, fbo.getTexture("velocities"))
      program.setUniform1i("uPositionSampler", 0)
      program.setUniform1i("uVelocitySampler", 1)

      val aCoordLocation = glGetAttribLocation(program.id, "aCoord")

      glEnableVertexAttribArray(aCoordLocation)

      glBindBuffer(GL_ARRAY_BUFFER, vbo)
      glVertexAttribPointer(aCoordLocation, 3, GL_FLOAT, false, 3 * 4, 0)
      glDrawArrays(GL_LINES, 0, pageSize * numPages * 2)

      glDisableVertexAttribArray(aCoordLocation)
    drawShader.unbind()
  }
}
