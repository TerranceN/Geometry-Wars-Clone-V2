package com.awesome.textures

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL21._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.EXTTextureCompressionS3TC._
import org.lwjgl.opengl.EXTTextureSRGB._
import org.lwjgl.BufferUtils

import de.matthiasmann.twl.utils.PNGDecoder;

import com.awesome.GLFrustum
import com.awesome.shaders._

import java.io._

class Texture(fileName:String) {
  glEnable(GL_TEXTURE_2D)

  val id = glGenTextures
  glActiveTexture(GL_TEXTURE0)
  glBindTexture(GL_TEXTURE_2D, id)

  val internalType = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT

  var in:InputStream = new FileInputStream(fileName)
  try {
    var decoder = new PNGDecoder(in)

    val buf = BufferUtils.createByteBuffer(4 * decoder.getWidth * decoder.getHeight)
    decoder.decode(buf, decoder.getWidth * 4, PNGDecoder.Format.RGBA)
    buf.flip

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth, decoder.getHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)

    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR); // Linear Filtering
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR); // Linear Filtering
  } finally {
  }

  glDisable(GL_TEXTURE_2D)

  def bind() = {
    glEnable(GL_TEXTURE_2D)
    glBindTexture(GL_TEXTURE_2D, id)
  }

  def unbind() = {
    glDisable(GL_TEXTURE_2D)
  }
}
object Texture {
  val vbo = glGenBuffers()
  initVBO()

  def initVBO() {
    val buffer = BufferUtils.createFloatBuffer(16)
    buffer.put(0); buffer.put(0)
    buffer.put(0); buffer.put(0)

    buffer.put(1); buffer.put(0)
    buffer.put(1); buffer.put(0)

    buffer.put(1); buffer.put(1)
    buffer.put(1); buffer.put(1)

    buffer.put(0); buffer.put(1)
    buffer.put(0); buffer.put(1)

    buffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
  }

  def drawUnitQuad() {
    GLFrustum.setMatricies()
    val program = ShaderProgram.getActiveShader()

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
  }
}
