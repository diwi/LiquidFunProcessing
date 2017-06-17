package com.thomasdiewald.liquidfun.java;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import processing.core.PApplet;
import processing.core.PGraphics;

public class DwWorld {
  
  public PApplet papplet;
  public World world;
  public DwViewportTransform transform;
  public DwDebugDraw debug_draw;
  
  public DwBodyRenderP5 body_render;
  
  
  public DwWorld(PApplet papplet){
    this.papplet = papplet;
    
    world = new World(new Vec2(0, -10f));
    
    transform = new DwViewportTransform(papplet);
    
    debug_draw = new DwDebugDraw(papplet, world, transform);

    
    body_render = new DwBodyRenderP5(papplet, world, transform);
  }
  

  public void setBodyRender(DwBodyRenderP5 body_render){
    this.body_render = body_render;
  }
  
  public void update(){
    update(1/60f, 8, 4);
  }
  
  
  public void update(float timestep, int iter_velocity, int iter_position){
    world.step(timestep, iter_velocity, iter_position);
    
    if(body_render != null){
      body_render.update();
    }
  }
  

  public void displayDebugDraw(PGraphics canvas){
    debug_draw.setCanvas(canvas);
    world.drawDebugData();
  }
  
  
  
}
