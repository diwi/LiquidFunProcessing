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

/**
 * 
 * TODO: will be removed i guess
 * 
 * @author Thomas
 *
 */
public class DwMouseDragBodies implements QueryCallback {
  
  public World world;
  public DwViewportTransform vptransform;
  
  public boolean active = false;
  public AABB aabb = new AABB();
  public Vec2 point = new Vec2();
  public Fixture fixture = null;
  public MouseJoint mouse_joint;
  
  public final Vec2 mouse = new Vec2();
  

  public DwMouseDragBodies(World world, DwViewportTransform vptransform){
    this.world = world;
    this.vptransform = vptransform;
  }

  public boolean query(Vec2 world_xy){
    fixture = null;
    point.set(world_xy);
    aabb.lowerBound.set(point.x - 0.001f, point.y - 0.001f);
    aabb.upperBound.set(point.x + 0.001f, point.y + 0.001f);
    world.queryAABB(this, aabb);
    return fixture != null;
  }
  
  public void begin(float screen_x, float screen_y) {
    vptransform.getScreen2box(screen_x, screen_y, mouse);
    if (query(mouse)) {
      Body body = fixture.getBody();
      MouseJointDef def = new MouseJointDef();
      def.bodyA = world.createBody(new BodyDef());
      def.bodyB = fixture.getBody();
      def.collideConnected = true;
      def.target.set(point);
      def.maxForce = 1000f * body.getMass();
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

  public void end(float screen_x, float screen_y) {
    if (active) {
      world.destroyJoint(mouse_joint);
      mouse_joint = null;
    }
    active = false;
  }
  
  public boolean isActive(){
    return active;
  }

  @Override
  public boolean reportFixture(Fixture argFixture) {
    Body body = argFixture.getBody();
    if (body.getType() == BodyType.DYNAMIC) {
      if (argFixture.testPoint(point)) {
        fixture = argFixture;
        return false;
      }
    }
    return true;
  }
  
  
}

