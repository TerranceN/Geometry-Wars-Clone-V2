package com.awesome

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._

import scala.collection.mutable.ArrayBuffer

import vectors._
import matricies._
import shaders._

class ParticleSystem(val pageSize:Int, val numPages:Int) {
  var fbo = new Framebuffer(pageSize, numPages)

  fbo.newTexture("positions", GL_RGBA32F, null)
  fbo.newTexture("velocities", GL_RGBA32F, null)

  var maxUsedPage = -1

  var inUse:ArrayBuffer[Boolean] = new ArrayBuffer(numPages)

  for (i <- 0 until numPages) {
    inUse += false
  }

  // create vbo where there are 2 * pageSize * numPages verticies representing indicies, doublling each one so that each one becomes a line

  def allocate(numParticles:Int):List[Int] = {
    var particlesLeft = numParticles
    var page = (maxUsedPage + 1) % numPages
    var allocatedPages:List[Int] = List()

    while (particlesLeft > 0 && page != maxUsedPage) {
      if (!inUse(page)) {
        particlesLeft -= pageSize
        allocatedPages = page :: allocatedPages
        maxUsedPage = page
      }
      page = (page + 1) % numPages
    }

    if (particlesLeft > 0) {
      Console.println("WARN: ParticleSystem::allocate: No free pages")
      return List()
    }

    for (p <- allocatedPages) {
      inUse(p) = true
    }
    return allocatedPages
  }

  def deallocate(pages:List[Int]) { pages map deallocate }
  def deallocate(page:Int) {
    inUse(page) = false
    if (numPages == 1) maxUsedPage = -1
  }

  def updatePage(page:Int) {
    fbo.drawFBOQuad(new Vector2(0, page), new Vector2(pageSize, page + 1))
  }

  def update(deltaTime:Double) {
  }

  def draw() {
  }
}
