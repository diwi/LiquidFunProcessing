/**
 * 
 * LiquidFunProcessing | Copyright 2017 Thomas Diewald - www.thomasdiewald.com
 * 
 * https://github.com/diwi/LiquidFunProcessing.git
 * 
 * Box2d / LiquidFun Library for Processing.
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */


package com.thomasdiewald.liquidfun.java;

import processing.opengl.PGraphics2D;


/**
 * 
 * 
 * Render Interface
 * 
 * 
 * @author Thomas Diewald
 *
 */
public interface DwRender {

  public void update();
  public void release();
  public void display(PGraphics2D canvas);
  
}
