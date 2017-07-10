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

import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


/**
 *  
 * @author Thomas Diewald
 *
 */
public class DwMouseDragParticles implements DwInteractionEvent, ParticleQueryCallback {
  
  public World world;
  public DwViewportTransform transform;
  
  protected DwMouseDragBodies body_dragger;
  
  public boolean active = false;
  

  public int button = PConstants.LEFT;
//public int button = PConstants.CENTER;
//public int button = PConstants.RIGHT;


  public Transform xf = new Transform();
  public AABB aabb = new AABB();
  public CircleShape query_shape = new CircleShape();

  public float dt = 1 / 60f;
  public Vec2 pos = new Vec2(0, 0);
  public Vec2 vel = new Vec2(0, 0);
  public Vec2 acc = new Vec2(0, 0);
  
  public final Vec2 mouse = new Vec2();
  
  public DwMouseDragParticles(World world, DwViewportTransform transform){
    this.world = world;
    this.transform = transform;
    xf.setIdentity();
    query_shape.m_radius = 2;
    
    body_dragger = new DwMouseDragBodies(world, transform);
  }
  
  
  public void press(float screen_x, float screen_y) {
    // bodies have priority
    if(body_dragger.query(screen_x, screen_y)){
      return;
    }

    transform.getScreen2box(screen_x, screen_y, mouse);
    this.vel.set(0, 0);
    this.pos.set(mouse);
    query_shape.m_p.set(pos);
    query_shape.computeAABB(aabb, xf, 0);
    world.queryAABB(this, aabb);
    active = true;
  }
  
  
  public void update(float screen_x, float screen_y) {
    if (active) {
      transform.getScreen2box(screen_x, screen_y, mouse);
      float delay = 0.1f;
      acc.x = 2f / delay * ((mouse.x - pos.x) / delay - vel.x);
      acc.y = 2f / delay * ((mouse.y - pos.y) / delay - vel.y);
      vel.x += dt * acc.x;
      vel.y += dt * acc.y;
      pos.x += dt * vel.x;
      pos.y += dt * vel.y;
      query_shape.m_p.set(pos);
      query_shape.computeAABB(aabb, xf, 0);
      world.queryAABB(this, aabb);
    }
  }
  
  public void release(float screen_x, float screen_y) {
    active = false;
  }
  
  
  public boolean is_enabled = true;
  
  @Override
  public void enable(boolean enable) {
    is_enabled = enable;
  }

  @Override
  public boolean isEnabled() {
    return is_enabled;
  }
  
  @Override
  public boolean isActive(){
    return active;
  }
  
  @Override
  public void setMouseButton(int button){
    this.button = button;
  }  
  
  @Override
  public int getMouseButton(){
    return button;
  }

  @Override
  public boolean reportParticle(int index) {
    Vec2 particle_pos = world.getParticlePositionBuffer()[index];
    if (query_shape.testPoint(xf, particle_pos)) {
      Vec2 particle_vel = world.getParticleVelocityBuffer()[index];
      particle_vel.set(vel);
    }
    return true;
  }



  
  
  
  int mouseX;
  int mouseY;
  int mouseX_off = 0;
  int mouseY_off = 0;
  
  @Override
  public void mouseEvent(MouseEvent event){
    if(!is_enabled){
      return;
    }
    mouseX = mouseX_off + event.getX();
    mouseY = mouseY_off + event.getY();
    if(event.getButton() == this.button){
      switch(event.getAction()){
        case MouseEvent.PRESS:   
          if(key_combi_enabled == key_combi_active){
            press(mouseX, mouseY);
          }
          break;
        case MouseEvent.RELEASE: 
          release(mouseX, mouseY); 
          break;
      }
    }
  }
  
  public int     key_combi = 0;
  public boolean key_combi_active = true;
  public boolean key_combi_enabled = true;

  @Override
  public void keyEvent(KeyEvent event){
    switch(event.getAction()){
      case KeyEvent.PRESS: 
        key_combi_active = false;
        break;
      case KeyEvent.RELEASE:
        key_combi_active = true;
        break;
    }
  }
  
  @Override
  public void updateEvent(){
    update(mouseX, mouseY);
  }
  
  
  @Override
  public void setMouseOffset(int mouseX_off, int mouseY_off){
    this.mouseX_off = mouseX_off;
    this.mouseY_off = mouseY_off;
  }

}
