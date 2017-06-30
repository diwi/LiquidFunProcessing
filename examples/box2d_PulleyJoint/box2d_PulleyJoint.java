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



package box2d_PulleyJoint;


import com.thomasdiewald.liquidfun.java.DwWorld;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.dynamics.joints.PulleyJointDef;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_PulleyJoint extends PApplet {

  //
  // This examples shows how to use PulleyJoints.
  //
  // Controls:
  //
  // LMB         ... drag bodies
  // LMB + SHIFT ... shoot bullet
  // MMB         ... add particles
  // RMB         ... remove particles
  // 'r'         ... reset
  // 't'         ... update/pause physics
  // 'f'         ... toggle debug draw
  //
  
  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    reset();
    frameRate(120);
  }
  
  
  public void release(){
    if(world != null) world.release(); world = null;
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 20);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){

    if(UPDATE_PHYSICS){
      world.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    canvas.background(32);
    canvas.pushMatrix();
    world.applyTransform(canvas);
    world.drawBulletSpawnTrack(canvas);
    if(USE_DEBUG_DRAW){
      world.displayDebugDraw(canvas);
      // DwDebugDraw.display(canvas, world);
    } else {
      world.display(canvas);
    }
    canvas.popMatrix();
    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////
  public void keyReleased(){
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Pulleys.java
  public void initScene() {
    
    float y = 16.0f;
    float L = 12.0f;
    float a = 1.0f;
    float b = 2.0f;
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
      ground.createFixture(shape, 0.0f);

      CircleShape circle = new CircleShape();
      circle.m_radius = 2.0f;

      circle.m_p.set(-10.0f, y + b + L);
      ground.createFixture(circle, 0.0f);

      circle.m_p.set(10.0f, y + b + L);
      ground.createFixture(circle, 0.0f);
      
      world.bodies.add(ground, true, color(220, 128), true, color(255), 1f);
    }

    {

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(a, b);

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;

      bd.position.set(-10.0f, y);
      Body body1 = world.createBody(bd);
      body1.createFixture(shape, 5.0f);

      bd.position.set(10.0f, y);
      Body body2 = world.createBody(bd);
      body2.createFixture(shape, 5.0f);
      
      world.bodies.add(body1, true, color(0,128,255), true, color(0), 1f);
      world.bodies.add(body2, true, color(0,128,255), true, color(0), 1f);

      PulleyJointDef pulleyDef = new PulleyJointDef();
      Vec2 anchor1 = new Vec2(-10.0f, y + b);
      Vec2 anchor2 = new Vec2(10.0f, y + b);
      Vec2 groundAnchor1 = new Vec2(-10.0f, y + b + L);
      Vec2 groundAnchor2 = new Vec2(10.0f, y + b + L);
      pulleyDef.initialize(body1, body2, groundAnchor1, groundAnchor2, anchor1, anchor2, 2.0f);

      Joint m_joint1 = (PulleyJoint) world.createJoint(pulleyDef);
      
      world.bodies.add(m_joint1, false, color(0), true, color(255,128,64), 1f);
    }
    

  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_PulleyJoint.class.getName() });
  }
  
}