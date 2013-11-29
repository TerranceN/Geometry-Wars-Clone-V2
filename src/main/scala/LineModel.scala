package com.awesome

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.BufferUtils

import vectors._
import matricies._
import shaders._

class LineModel(points:List[Vector2]) {
  val numPoints = points.length
  val vbo = glGenBuffers()
  initVBO()

  val primitiveShader = new ShaderProgram(
    new VertexShader("shaders/primitiveWithColor.vert"),
    new FragmentShader("shaders/primitiveWithColor.frag")
  )

  def initVBO() {
    val buffer = BufferUtils.createFloatBuffer(numPoints * 2)
    for (p <- points) {
      buffer.put(p.x)
      buffer.put(p.y)
    }
    buffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
  }

  def draw(color:Vector3) {
    primitiveShader.bind()
      GLFrustum.setMatricies()
      val program = ShaderProgram.getActiveShader()

      val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
      program.setUniform4f("uColor", color.x, color.y, color.z, 1)

      glEnableVertexAttribArray(aCoordLocation)

      glBindBuffer(GL_ARRAY_BUFFER, vbo)
      glVertexAttribPointer(aCoordLocation, 2, GL_FLOAT, false, 2 * 4, 0)

      glDrawArrays(GL_LINES, 0, numPoints)

      glDisableVertexAttribArray(aCoordLocation)
    primitiveShader.unbind()
  }
}
object LineModel {
  def lineLoop(points:List[Vector2]):List[Vector2] = {
    val newPoints = points flatMap { x => List(x, x) }
    return newPoints.tail ::: List(newPoints.head)
  }
}
