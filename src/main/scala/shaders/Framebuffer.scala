package com.awesome.shaders

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.BufferUtils

import com.awesome.GLFrustum
import com.awesome.matricies._
import com.awesome.vectors._

import java.nio.FloatBuffer
import scala.collection.mutable.ArrayBuffer

class Framebuffer(val fboWidth:Int, val fboHeight:Int) {
  class TextureReference(val name:String, val id:Int, val attachmentId:Int) { }

  val textures:ArrayBuffer[TextureReference] = ArrayBuffer()
  var maxAttachmentId:Int = 0
  val id = glGenFramebuffers()

  var fboVBO:Int = glGenBuffers()

  initFBOVBO()

  def initFBOVBO() {
    val vertexBuffer = BufferUtils.createFloatBuffer(16)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(fboWidth); vertexBuffer.put(0.0f)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 1.0f)

    vertexBuffer.put(fboWidth); vertexBuffer.put(fboHeight)
    vertexBuffer.put( 1.0f); vertexBuffer.put( 0.0f)

    vertexBuffer.put( 0.0f); vertexBuffer.put(fboHeight)
    vertexBuffer.put( 0.0f); vertexBuffer.put( 0.0f)
    vertexBuffer.flip()

    glBindBuffer(GL_ARRAY_BUFFER, fboVBO)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
  }

  def delete: Unit = {
    var textureIds = textures map (_.id)
    var texIdBuffer = BufferUtils.createIntBuffer(textureIds.length)
    for (texId <- textureIds) {
      texIdBuffer.put(texId)
    }
    texIdBuffer.flip()
    glDeleteTextures(texIdBuffer)
    glDeleteFramebuffers(id)
  }

  def blankBuffer(numComponents:Int):FloatBuffer = {
    var buf = BufferUtils.createFloatBuffer(fboWidth * fboHeight * numComponents)
    BufferUtils.zeroBuffer(buf)
    return buf
  }

  def newTexture(name:String, internalType:Int, floatBuffer:FloatBuffer) {
    glBindFramebuffer(GL_FRAMEBUFFER, id)

    val tex = glGenTextures()
    textures += new TextureReference(name, tex, maxAttachmentId)

    var initData = floatBuffer
    if (initData == null) {
      initData = blankBuffer(4)
    }

    glBindTexture(GL_TEXTURE_2D, tex)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    glTexImage2D(GL_TEXTURE_2D, 0, internalType, fboWidth, fboHeight, 0, GL_RGBA, GL_FLOAT, initData)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + maxAttachmentId, GL_TEXTURE_2D, tex, 0)

    maxAttachmentId += 1
  }

  def getTexture(name:String):Int = getTextureOption(name).getOrElse(-1)

  def getTextureOption(name:String):Option[Int] = {
    for (tex <- textures) {
      if (tex.name == name) {
        return Some(tex.id)
      }
    }

    return None
  }

  def getAttachmentId(name:String):Option[Int] = {
    for (tex <- textures) {
      if (tex.name == name) {
        return Some(tex.attachmentId)
      }
    }

    return None
  }

  def drawFBOQuad() { drawFBOQuad(new Vector2(0, 0), new Vector2(fboWidth, fboHeight)) }

  def drawFBOQuad(lower:Vector2, upper:Vector2) {
    GLFrustum.pushProjection()
    GLFrustum.projectionMatrix = Matrix4.ortho(lower.x, upper.x, upper.y, lower.y, -1, 1)

    GLFrustum.pushModelview()
    GLFrustum.modelviewMatrix.setIdentity

    val program = ShaderProgram.getActiveShader()

    // if there's no shader program, there's nowhere to send the vertex data
    if (program != null) {
      GLFrustum.setMatricies()

      val aCoordLocation = glGetAttribLocation(program.id, "aCoord")
      val aTexCoordLocation = glGetAttribLocation(program.id, "aTexCoord")

      glEnableVertexAttribArray(aCoordLocation)
      if (aTexCoordLocation >= 0) {
        glEnableVertexAttribArray(aTexCoordLocation)
      }

      glBindBuffer(GL_ARRAY_BUFFER, fboVBO)
      glVertexAttribPointer(aCoordLocation, 3, GL_FLOAT, false, 4 * 4, 0)
      if (aTexCoordLocation >= 0) {
        glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, false, 4 * 4, 2 * 4)
      }

      glDrawArrays(GL_QUADS, 0, 4)

      glDisableVertexAttribArray(aCoordLocation)
      if (aTexCoordLocation >= 0) {
        glDisableVertexAttribArray(aTexCoordLocation)
      }
    }

    GLFrustum.popModelview()
    GLFrustum.popProjection()
  }

  def drawToTextures(textureNames:List[String])(f: => Unit) {
    // bind fbo and call glRenderBuffers
    glBindFramebuffer(GL_FRAMEBUFFER, id)
    glViewport(0, 0, fboWidth, fboHeight)

    var optionAttachments = textureNames map getAttachmentId
    var attachments = optionAttachments flatten

    if (attachments.length != textureNames.length) {
      var missing = ((optionAttachments zip textureNames) filter { case (None, _) => true case _ => false }).foldLeft("") { case (s2, (_, s)) => s + s2 }
      throw new IllegalArgumentException("Cannot find the following textures: " + missing)
    }

    var buffer = BufferUtils.createIntBuffer(attachments.length)
    for (i <- attachments) {
      buffer.put(GL_COLOR_ATTACHMENT0 + i)
    }
    buffer.flip

    glDrawBuffers(buffer)

    f

    // unbind fbo and set render target to GL_BACK
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    glDrawBuffer(GL_BACK)
  }
}
