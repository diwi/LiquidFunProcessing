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


import java.util.Stack;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;



/**
 *  
 * @author Thomas Diewald
 *
 */
public class DwMouseShootBullet implements DwInteractionEvent {

  public World world;
  public DwViewportTransform transform;

  public boolean is_active = false;
  public int button = PConstants.LEFT;

  public Vec2 bomb_spawn_start = new Vec2();
  public Vec2 bomb_spawn_end   = new Vec2();
  public Vec2 bomb_velocity    = new Vec2();
  public float velocity_mult   = 2f;
  public float density_mult    = 1f;
  

  public BodyDef body_def = new BodyDef();
  public CircleShape cirlce_shape = new CircleShape();
  public FixtureDef fixture_def = new FixtureDef();
  
  protected DwMouseDragBodies body_dragger;
  
  protected Stack<Body> bullets = new Stack<>();
  

  public DwMouseShootBullet(World world, DwViewportTransform transform){
    this.world = world;
    this.transform = transform;
    
    // bullet specific settings
    cirlce_shape.m_radius = 0.3f;
    
    fixture_def.shape = cirlce_shape;
    fixture_def.density = 200f;
    fixture_def.restitution = 0.1f;
    
    body_def.type = BodyType.DYNAMIC;
    body_def.bullet = true;
    
    body_dragger = new DwMouseDragBodies(world, transform);
  }

  public void beginSpawn(float screen_x, float screen_y){
    // bodies have priority
    if(body_dragger.query(screen_x, screen_y)){
      return;
    }
    
    transform.getScreen2box(screen_x, screen_y, bomb_spawn_start);
    bomb_spawn_end.set(bomb_spawn_start);
    is_active = true;
  }

  public void updateSpawn(float screen_x, float screen_y){
    if(is_active){
      transform.getScreen2box(screen_x, screen_y, bomb_spawn_end);
    }
  }

  public void endSpawn(float screen_x, float screen_y){
    if(is_active){
      transform.getScreen2box(screen_x, screen_y, bomb_spawn_end);
      bomb_velocity = bomb_spawn_end.sub(bomb_spawn_start);
      
      // velocity and density
      float vel_len = 1f + bomb_velocity.length();
      
      float mult = (1f + vel_len * vel_len) * velocity_mult;
      
      fixture_def.density = (1 + vel_len * vel_len) * density_mult;

      bomb_velocity.normalize();
      bomb_velocity.mulLocal(mult);
      
      shoot(bomb_spawn_start, bomb_velocity);
      is_active = false;
    }
  }

  public void drawSpawnTrack(PGraphics pg){
    if(is_active){
      pg.strokeWeight(1f / transform.screen_scale);
      pg.stroke(200,128,128, 128);
      pg.line(bomb_spawn_start.x, bomb_spawn_start.y, bomb_spawn_end.x, bomb_spawn_end.y);
    }
  }

  public void shoot(Vec2 position, Vec2 velocity) {
    body_def.position.set(position);
    Body bullet = world.createBody(body_def);
    bullet.setLinearVelocity(velocity);
    bullet.createFixture(fixture_def);
    
    bullets.push(bullet);
  }
  
  
  public Body popBullet(){
    if(bullets.isEmpty()){
      return null;
    }
    return bullets.pop();
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
  public boolean isActive() {
    return is_active;
  }

  @Override
  public void setMouseButton(int button) {
    this.button = button;
  }

  @Override
  public int getMouseButton() {
    return button;
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
          beginSpawn(mouseX, mouseY);
        }
        break;
      case MouseEvent.RELEASE: 
        endSpawn(mouseX, mouseY); 
        break;
      }
    }
  }

  public int     key_combi = KeyEvent.SHIFT;
  public boolean key_combi_active = false;
  public boolean key_combi_enabled = true;

  @Override
  public void keyEvent(KeyEvent event){
    switch(event.getAction()){
      case KeyEvent.PRESS: 
        key_combi_active = ((key_combi ^ event.getModifiers()) == 0);
        break;
      case KeyEvent.RELEASE:
        key_combi_active = false;
        break;
    }
   
  }
  

  @Override
  public void updateEvent() {
    updateSpawn(mouseX, mouseY);
  }
  
  @Override
  public void setMouseOffset(int mouseX_off, int mouseY_off){
    this.mouseX_off = mouseX_off;
    this.mouseY_off = mouseY_off;
  }
  
  
}
