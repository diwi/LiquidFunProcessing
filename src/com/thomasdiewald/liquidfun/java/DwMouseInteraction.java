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

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import processing.core.PApplet;
import processing.event.MouseEvent;



/**
 * 
 * Class that handles mouse-interaction with Box2D bodies.
 * 
 * Picking, Dragging, Destroying ...
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwMouseInteraction {

  PApplet papplet;
  DwViewportTransform transform;
  World world;
  
  DwMouseDragParticles drag_particles;
  DwMouseDragBodies    drag_bodies;
  
  int offx = 0;
  int offy = 0;
  
  Vec2 mouseP5 = new Vec2(); // mouse coordinates, processing
  Vec2 mouseB2 = new Vec2(); // mouse coordinates, Box2d
  
  boolean MOUSE_PRESSED = false;
  
  public DwMouseInteraction(PApplet papplet, World world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;
    
    this.drag_particles = new DwMouseDragParticles(world, transform);
    this.drag_bodies    = new DwMouseDragBodies(world, transform);
    

    this.papplet.registerMethod("post"      , this);
    this.papplet.registerMethod("mouseEvent", this);
  }
  
  public void setOffset(int offx, int offy){
    this.offx = offx;
    this.offy = offy;
  }

  public void post(){
    drag_particles.update(mouseP5.x, mouseP5.y);
  }

  public void mouseEvent(MouseEvent me) {
    mouseP5.x = me.getX() - offx;
    mouseP5.y = me.getY() - offy;


    if(MouseEvent.PRESS == me.getAction()){
      MOUSE_PRESSED = true;
      drag_bodies   .begin(mouseP5.x, mouseP5.y);
      if(!drag_bodies.active){
        drag_particles.begin(mouseP5.x, mouseP5.y);
      }
    }
    if(MouseEvent.DRAG == me.getAction()){
      drag_bodies.update(mouseP5.x, mouseP5.y);
//      drag_particles.update(mouseP5.x, mouseP5.y);
    }
    if(MouseEvent.RELEASE == me.getAction()){
      MOUSE_PRESSED = false;
      drag_bodies   .end(mouseP5.x, mouseP5.y);
      drag_particles.end(mouseP5.x, mouseP5.y);
    } 
  }


}
