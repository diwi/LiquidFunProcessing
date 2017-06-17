package com.thomasdiewald.liquidfun.java;

import org.jbox2d.common.Vec2;

public interface DwMouseHandler {

  public void begin(Vec2 pos_world);

  public void update(Vec2 pos_world);

  public void end(Vec2 pos_world);
  
}
