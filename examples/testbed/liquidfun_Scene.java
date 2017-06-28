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
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleDef;
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
  boolean APPLY_LIQUID_FX = true;

  DwWorld world;  
//  PImage sprite;

  
  DwPixelFlow pixelflow;
  DwLiquidFX liquidfx;
  
  PGraphics2D pg_particles;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
//    sprite = loadImage("sprite.png");
    
    
    pixelflow = new DwPixelFlow(this);
    liquidfx = new DwLiquidFX(pixelflow);
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    
    
    
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
    
//    world.particles.param.falloff_exp1 = 3;
//    world.particles.param.falloff_exp2 = 1;
//    world.particles.param.radius_scale = 2;
    
    world.particles.param.falloff_exp1 = 1;
    world.particles.param.falloff_exp2 = 2;
    world.particles.param.radius_scale = 3;

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    
    if(UPDATE_PHYSICS){
      wave();
//      if(frameCount % 2 == 0){
      for(int i = 0; i < 5; i++){
        addParticles();
      }
//      }
      world.update();
    }
    
    int BACKGROUND = 32;

    if(USE_DEBUG_DRAW){
      PGraphics2D canvas = (PGraphics2D) this.g;
      
      canvas.background(BACKGROUND);
      canvas.pushMatrix();
      world.applyTransform(canvas);
      world.drawBulletSpawnTrack(canvas);
      world.displayDebugDraw(canvas);
      canvas.popMatrix();
    } else {
      PGraphics2D canvas = (PGraphics2D) pg_particles;

      canvas.beginDraw();
      canvas.clear();
      canvas.background(BACKGROUND, 0);
      world.applyTransform(canvas);
//      world.bodies.display(canvas);
      world.particles.display(canvas, 0);
      canvas.endDraw();
      
      if(APPLY_LIQUID_FX)
      {
        liquidfx.param.base_LoD = 0;
        liquidfx.param.base_blur_radius = 2;
        liquidfx.param.base_threshold = 0.7f;
        liquidfx.param.highlight_enabled = true;
        liquidfx.param.highlight_LoD = 1;
        liquidfx.param.highlight_decay = 0.6f;
        liquidfx.param.sss_enabled = !true;
        liquidfx.param.sss_LoD = 4;
        liquidfx.param.sss_decay = 0.8f;
        liquidfx.apply(canvas);
      }
      
      background(BACKGROUND);
      image(canvas, 0, 0);
      pushMatrix();
      world.applyTransform(this.g);
      world.bodies.display((PGraphics2D) this.g);
      world.drawBulletSpawnTrack(this.g);
      popMatrix();
    }
    
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
    if(key == 'g') APPLY_LIQUID_FX = !APPLY_LIQUID_FX;
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
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);
      
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
      shape.setAsBox(dimx/4, thick/2, new Vec2(0, 4), 10 * PI/180);
      ground.createFixture(shape, 0);
      
      shape.setAsBox(dimx/6, thick/2, new Vec2(-20, 8), -15 * PI/180);
      ground.createFixture(shape, 0);
      
      
      world.bodies.add(ground, true, color(220), !true, color(0), 1f);
    }
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-15, 20);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 5.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(5, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), !true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = !true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-6, 22);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 5.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(5, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), !true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = -0.2f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = !true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    
    
    
    
    
    
    
    
    
    

    
    world.setParticleRadius(0.15f);
    world.setParticleDamping(0.6f);

    {
      pgroup_def.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
      pgroup_def.setColor(new Color3f(1, 0.15f, 0.05f));
      pgroup_def.linearVelocity.set(0,0);
      pgroup_def.angularVelocity = 0f;
      
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(17.0f, 9.0f, new Vec2(0.0f, dimy/2), 0.0f);

      pgroup_def.shape = shape;
      world.createParticleGroup(pgroup_def);
    }

    m_time = 0;

  }
  
  
  ParticleGroupDef pgroup_def = new ParticleGroupDef();


  int counter = 0;
  public void addParticles(){
    
    float screen_scale = world.transform.screen_scale;
    
    
    float srandnoise = noise(counter / 100f) * 2 - 1;
    
    float rot_angle = 0 + srandnoise * 0.1f;
    float vel_mag   = 10 + srandnoise * 5f;
    
    float velx = (float) (Math.cos(rot_angle) * vel_mag);
    float vely = (float) (Math.sin(rot_angle) * vel_mag);
    
    Vec2 vel = new Vec2();
    vel.x = (float) (Math.cos(rot_angle) * vel_mag);
    vel.y = (float) (Math.sin(rot_angle) * vel_mag);
    
    Vec2 pos = new Vec2();
    world.transform.getScreen2box(200, 200, pos);
    pos.x += random(-1,1) * 1f;
    pos.y += random(-1,1) * 1f;
    
    
//    CircleShape shape = new CircleShape();
//    world.transform.getScreen2box(200, 200, shape.m_p);
//    shape.m_radius = 30 / screen_scale;
    
    

//    PolygonShape shape = new PolygonShape();
//    shape.setAsBox(10 / screen_scale, 2, pos, rot);
//    
//    world.mouse_destroy_particles.destroyParticles(shape);
    
//    pgroup_def.shape = shape;
    pgroup_def.angularVelocity = rot_angle;
    pgroup_def.linearVelocity.set(vel);
//    world.createParticleGroup(pgroup_def);
    
    

    ParticleDef pdef = new ParticleDef();
    pdef.position.set(pos);
    pdef.velocity.set(vel);
    pdef.color = pgroup_def.color;
    pdef.flags = pgroup_def.flags;
    
    world.createParticle(pdef);
    
    
    counter++;
  }



  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_Scene.class.getName() });
  }
  
}