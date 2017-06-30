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



package box2d_Tumbler;


import com.thomasdiewald.liquidfun.java.DwWorld;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Tumbler extends PApplet {
  
  //
  // A square tumbler is animated using a RevoluteJoint (motor).
  // Small boxes are dynamically added.
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
    
    world = new DwWorld(this, 22);
    world.transform.setScreen(width, height, 22, width/2, height/2);
   
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    
    if(UPDATE_PHYSICS){
      if(frameCount % 4 == 0){
        addBodies();
      }
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
  int MAX_NUM = 800;
  int m_count = 0;
  RevoluteJoint m_joint;

  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Tumbler.java
  public void initScene() {
    

    {
      BodyDef bd = new BodyDef();
      Body groundbody = world.createBody(bd);
      
      bd.type = BodyType.DYNAMIC;
      bd.allowSleep = false;
      bd.position.set(0.0f, 0.0f);
      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(1, 11.0f, new Vec2(10.0f, 0.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(1, 11.0f, new Vec2(-10.0f, 0.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(11.0f, 1, new Vec2(0.0f, 10.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(11.0f, 1, new Vec2(0.0f, -10.0f), 0.0f);
      body.createFixture(shape, 15.0f);

      
      CircleShape obstacle = new CircleShape(); 
      obstacle.m_radius = 1; 
      obstacle.m_p.set(-2, -6.5f);
      body.createFixture(obstacle, 115.0f);
      obstacle.m_p.set(2, +6.5f);
      body.createFixture(obstacle, 115.0f);
//      obstacle.m_p.set(-7.5f, 0);
//      body.createFixture(obstacle, 15.0f);
//      obstacle.m_p.set(+7.5f, 0);
//      body.createFixture(obstacle, 15.0f);


      world.bodies.add(body, true, color(224), true, color(0), 1f);

      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = groundbody;
      jd.bodyB = body;
      jd.localAnchorA.set(0.0f, 0.0f);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    m_count = 0;
   

    // creates shapes for all rigid bodies in the world.
    world.bodies.addAll();
  }
  
  
  public void addBodies(){
    if (m_count < MAX_NUM) {
      
      float x = random(-0.7f, 0.7f);
      float y = random(-0.7f, 0.7f);
      
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(x, y);
      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.18f, 0.18f);
      Fixture fixture = body.createFixture(shape, 0.01f);
      fixture.m_friction = 0.1f;
      fixture.m_restitution = 0.5f;
      
      ++m_count;

      colorMode(HSB, 360, 100, 100);
      float r = (360 * m_count /(float)MAX_NUM) % 360;
      float g = 100;
      float b = 100;
      world.bodies.add(body, true, color(r,g,b), true, color(r, g, b *0.5f), 1f);
      colorMode(RGB, 255, 255, 255);
    }
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Tumbler.class.getName() });
  }
  
}