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



package testbed;


import com.thomasdiewald.liquidfun.java.DwWorld;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_Scene extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;  
//  PImage sprite;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
//    sprite = loadImage("sprite.png");
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
      wave();
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
  
  RevoluteJoint m_joint;
  float m_time;
  
  
  public void wave() {
//    m_time += 1 / 120f;
//    m_joint.setMotorSpeed(0.1f * MathUtils.cos(m_time) * MathUtils.PI);
  }
 
  public void initScene() {
    
    
    float screenscale =  world.transform.screen_scale;
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    float thick = 10 / screenscale;
    
    
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);
      
//      bd.type = BodyType.DYNAMIC;
//      bd.allowSleep = false;
//      bd.position.set(0.0f, 0.0f);
//      Body body = world.createBody(bd);

//      PolygonShape shape = new PolygonShape();
//      shape.setAsBox(1, 9.5f, new Vec2(19.0f, 0.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(1, 9.5f, new Vec2(-19.0f, 0.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(20.0f, 1, new Vec2(0.0f, 11.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(20.0f, 1, new Vec2(0.0f, -11.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      world.bodies.add(body, true, color(0), true, color(0), 1f);
//      
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(dimx/4, thick/2, new Vec2(0, 5), 10 * PI/180);
      ground.createFixture(shape, 0);
      
      
      world.bodies.add(ground, true, color(0), true, color(0), 1f);

//      RevoluteJointDef jd = new RevoluteJointDef();
//      jd.bodyA = ground;
//      jd.bodyB = body;
//      jd.localAnchorA.set(0.0f, 0.0f);
//      jd.localAnchorB.set(0.0f, 0.0f);
//      jd.referenceAngle = 0.0f;
//      jd.motorSpeed = 0.05f * MathUtils.PI;
//      jd.maxMotorTorque = 1e7f;
//      jd.enableMotor = true;
//      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    

    
    world.setParticleRadius(0.15f);
    world.setParticleDamping(0.6f);

    {
      ParticleGroupDef pd = new ParticleGroupDef();
      pd.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
      pd.setColor(new Color3f(1, 0.15f, 0.05f));
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(17.0f, 9.0f, new Vec2(0.0f, dimy/2), 0.0f);

      pd.shape = shape;
      world.createParticleGroup(pd);
    }

    m_time = 0;

  }




  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_Scene.class.getName() });
  }
  
}