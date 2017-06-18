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

import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

public class DwMouseDragParticles implements ParticleQueryCallback {
  
  public World world;
  public DwViewportTransform vptransform;
  
  public boolean active = false;


  public Transform xf = new Transform();
  public AABB aabb = new AABB();
  public CircleShape query_shape = new CircleShape();

  public float dt = 1 / 60f;
  public Vec2 pos = new Vec2(0, 0);
  public Vec2 vel = new Vec2(0, 0);
  public Vec2 acc = new Vec2(0, 0);
  
  public final Vec2 mouse = new Vec2();
  
  public DwMouseDragParticles(World world, DwViewportTransform vptransform){
    this.world = world;
    this.vptransform = vptransform;
    xf.setIdentity();
    query_shape.m_radius = 2;
  }
  

  public void begin(float screen_x, float screen_y) {
    vptransform.getScreen2box(screen_x, screen_y, mouse);
    this.vel.set(0, 0);
    this.pos.set(mouse);
    this.active = true;
  }

  public void update(float screen_x, float screen_y) {
    if (active) {
      vptransform.getScreen2box(screen_x, screen_y, mouse);
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

  public void end(float screen_x, float screen_y) {
    active = false;
  }
  
  public boolean isActive(){
    return active;
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

}
