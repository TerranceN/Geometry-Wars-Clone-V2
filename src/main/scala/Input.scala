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
  def controllerPluggedIn = Controllers.getControllerCount() > controllerIndex

  def getButton():ButtonState = {
    if (controllerPluggedIn) {
      if (Controllers.getController(controllerIndex).isButtonPressed(button)) {
        return Pressed
      } else {
        return Released
      }
    } else {
      return Released
    }
  }
}

class CombinationButton(val button1:InputButton, val button2:InputButton) extends InputButton {
  def getButton():ButtonState = {
    return button1.getButton match {
      case Pressed => Pressed
      case Released => button2.getButton
    }
  }
}

trait InputAxis {
  var enabled:Boolean = false
  val initialValue = getAxisValue()

  def checkInitialValue(value:Float):Float = {
    if (value != initialValue) {
      enabled = true
      return value
    } else {
      return 0
    }
  }
  def getAxis():Float = {
    if (enabled) {
      return getAxisValue()
    } else {
      return checkInitialValue(getAxisValue())
    }
  }
  def getAxisValue():Float
}

class KeyboardAxis(lowKey:Int, highKey:Int) extends InputAxis {
  def getAxisValue():Float = {
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
  def controllerPluggedIn = Controllers.getControllerCount() > controllerIndex

  def getAxisValue():Float = {
    if (controllerPluggedIn) {
      return (Controllers.getController(controllerIndex).getAxisValue(axis))
    } else {
      return 0
    }
  }
}

class CombinationAxis(val axis1:InputAxis, val axis2:InputAxis) extends InputAxis {
  def clamp(x:Float, lower:Float, upper:Float):Float = {
    return max(lower, min(upper, x)).toFloat
  }

  def getAxisValue():Float = {
    return clamp(axis1.getAxis() + axis2.getAxis(), -1, 1)
  }
}

class Input(var buttonDict:Map[String, InputButton], var axisDict:Map[String, InputAxis]) {
  import ButtonState._

  def getAxis(name:String):Float = {
    axisDict(name).getAxis()
  }

  def getButton(name:String):ButtonState = {
    return buttonDict(name).getButton
  }

  def ifPressed(name:String)(f: => Unit) {
    getButton(name) match {
      case Pressed => f
      case Released => {}
    }
  }
}
