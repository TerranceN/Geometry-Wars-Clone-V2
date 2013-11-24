package com.awesome

import org.lwjgl.input._
import scala.math._

object ButtonState extends Enumeration {
  type ButtonState = Value
  val Pressed, Released = Value
}

import ButtonState._

trait InputButton {
  def getButton():ButtonState
}

class KeyboardButton(val key:Int) extends InputButton {
  def getButton():ButtonState = {
    if (Keyboard.isKeyDown(key)) {
      return Pressed
    } else {
      return Released
    }
  }
}

class MouseButton(val button:Int) extends InputButton {
  def getButton():ButtonState = {
    if (Mouse.isButtonDown(button)) {
      return Pressed
    } else {
      return Released
    }
  }
}

class ControllerButton(val controllerIndex:Int, val button:Int) extends InputButton {
  def getButton():ButtonState = {
    if (Controllers.getController(controllerIndex).isButtonPressed(button)) {
      return Pressed
    } else {
      return Released
    }
  }
}

trait InputAxis {
  def getAxis():Float
}

class KeyboardAxis(lowKey:Int, highKey:Int) extends InputAxis {
  def getAxis():Float = {
    var total = 0.0f
    if (Keyboard.isKeyDown(lowKey)) {
      total -= 1
    }
    if (Keyboard.isKeyDown(highKey)) {
      total += 1
    }
    return total
  }
}

class ControllerAxis(val controllerIndex:Int, val axis:Int) extends InputAxis {
  def getAxis():Float = {
    return (Controllers.getController(controllerIndex).getAxisValue(axis))
  }
}

class CombinationAxis(val axis1:InputAxis, val axis2:InputAxis) extends InputAxis {
  def clamp(x:Float, lower:Float, upper:Float):Float = {
    return max(lower, min(upper, x)).toFloat
  }

  def getAxis():Float = {
    return clamp(axis1.getAxis() + axis2.getAxis(), -1, 1)
  }
}

class Input(var buttonDict:Map[String, InputButton], var axisDict:Map[String, InputAxis]) {
  import ButtonState._

  var initialButtonValues:Map[String, ButtonState] = Map()
  for ((k, v) <- buttonDict) {
    initialButtonValues += k -> v.getButton()
  }

  var initialAxisValues:Map[String, Float] = Map()
  for ((k, v) <- axisDict) {
    initialAxisValues += k -> v.getAxis()
  }

  var controllerEnabled:Map[Int, Boolean] = Map()

  def getAxis(name:String):Float = {
    axisDict(name) match {
      case axis:ControllerAxis => {
        val value = axis.getAxis()
        if (!(controllerEnabled contains axis.controllerIndex)) {
          if (value != initialAxisValues(name)) {
            controllerEnabled += axis.controllerIndex -> true
            return value
          } else {
            return 0.0f
          }
        } else {
          return value
        }
      }
      case other:InputAxis => other.getAxis()
    }
    return axisDict(name).getAxis()
  }

  def getButton(name:String):ButtonState = {
    return buttonDict(name).getButton
  }
}
