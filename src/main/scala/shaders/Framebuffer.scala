package com.awesome.shaders

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.BufferUtils

import java.nio.FloatBuffer
import scala.collection.mutable.ArrayBuffer

class Framebuffer(val fboWidth:Int, val fboHeight:Int) {
  class TextureReference(val name:String, val id:Int, val attachmentId:Int) { }

  val textures:ArrayBuffer[TextureReference] = ArrayBuffer()
  var maxAttachmentId:Int = 0
  val id = glGenFramebuffers()

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
