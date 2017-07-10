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

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


/**
 *  
 * @author Thomas Diewald
 *
 */
public class DwMouseDragBodies implements DwInteractionEvent, QueryCallback{
  
  public World world;
  public DwViewportTransform vptransform;
  
  public boolean active = false;
  public AABB aabb = new AABB();
  public Vec2 point = new Vec2();
  public Fixture fixture = null;
  public MouseJoint mouse_joint;
  
  public final Vec2 mouse = new Vec2();
  public float mult_dragforce = 1000000f;
  public boolean enable_static_drag = false;
  
  public int button = PConstants.LEFT;
//  public int button = PConstants.CENTER;
//  public int button = PConstants.RIGHT;

  public DwMouseDragBodies(World world, DwViewportTransform vptransform){
    this.world = world;
    this.vptransform = vptransform;
  }

//  public boolean query(Vec2 world_xy){
//    fixture = null;
//    point.set(world_xy);
//    aabb.lowerBound.set(point.x - 0.001f, point.y - 0.001f);
//    aabb.upperBound.set(point.x + 0.001f, point.y + 0.001f);
//    world.queryAABB(this, aabb);
//    return fixture != null;
//  }
  
  public boolean query(float screen_x, float screen_y){
    vptransform.getScreen2box(screen_x, screen_y, mouse);
    fixture = null;
    point.set(mouse);
    aabb.lowerBound.set(point.x - 0.001f, point.y - 0.001f);
    aabb.upperBound.set(point.x + 0.001f, point.y + 0.001f);
    world.queryAABB(this, aabb);
    return fixture != null;
  }
  

  public void press(float screen_x, float screen_y) {
    if (query(screen_x, screen_y)) {
      Body body = fixture.getBody();
      MouseJointDef def = new MouseJointDef();
      def.bodyA = world.createBody(new BodyDef());
      def.bodyB = fixture.getBody();
      def.collideConnected = true;
      def.target.set(point);
      def.maxForce = mult_dragforce * body.getMass();
      mouse_joint = (MouseJoint) world.createJoint(def);
      body.setAwake(true);
    }
    active = (mouse_joint != null);
  }
  

  public void update(float screen_x, float screen_y) {
    if (active) {
      vptransform.getScreen2box(screen_x, screen_y, mouse);
      mouse_joint.setTarget(mouse);
    }
  }
  

  public void release(float screen_x, float screen_y) {
    if (active) {
      if(body_type_cpy != null){
        fixture.getBody().setType(body_type_cpy);
        body_type_cpy = null;
      }
      fixture = null;
      world.destroyJoint(mouse_joint);
      mouse_joint = null;
    }
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

  public BodyType body_type_cpy = null;
  
  @Override
  public boolean reportFixture(Fixture argFixture) {
    Body body = argFixture.getBody();
    
    // static bodies can be moved too, but need to set to dynamic for the
    // mousejoint to work.
    body_type_cpy = body.getType();
    if(enable_static_drag){
      body.setType(BodyType.DYNAMIC);
    }
    
    if (body.getType() == BodyType.DYNAMIC) 
    {
      if (argFixture.testPoint(point)) {
        fixture = argFixture;
        return false;
      }
    }
    return true;
  }


  int mouseX_off = 0;
  int mouseY_off = 0;
  int mouseX;
  int mouseY;
  
  @Override
  public void mouseEvent(MouseEvent event){
    if(!is_enabled){
      return;
    }

    mouseX = mouseX_off + event.getX();
    mouseY = mouseY_off + event.getY();
    if(event.getButton() == this.button){
      switch(event.getAction()){
        case MouseEvent.PRESS:   press  (mouseX, mouseY); break;
        case MouseEvent.RELEASE: release(mouseX, mouseY); break;
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

