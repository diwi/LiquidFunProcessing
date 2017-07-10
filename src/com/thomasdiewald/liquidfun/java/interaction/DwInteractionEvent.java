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

package com.thomasdiewald.liquidfun.java.interaction;

import processing.event.KeyEvent;
import processing.event.MouseEvent;


/**
 * Interface for mouse interactions, mainly used by DwWorld to have some
 * default interaction handlers.
 * 
 * @author Thomas  Diewald
 *
 */
public interface DwInteractionEvent {
  
  public void enable(boolean enable);
  public boolean isEnabled();
  
  public boolean isActive();
  
  public void setMouseButton(int button);
  public int getMouseButton();
  
  public void mouseEvent(MouseEvent event);
  public void keyEvent(KeyEvent event);
  public void updateEvent();
  
  public void setMouseOffset(int mouseX_off, int mouseY_off);
}
