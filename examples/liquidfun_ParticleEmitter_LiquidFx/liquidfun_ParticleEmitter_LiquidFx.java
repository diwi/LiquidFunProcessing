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



package liquidfun_ParticleEmitter_LiquidFx;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.DwParticleEmitter;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_ParticleEmitter_LiquidFx extends PApplet {
  
  //
  // This Examples show how to use ParticleEmitters.
  // 
  //
  // required libraries:
  //  - PixelFlow, https://github.com/diwi/PixelFlow
  //
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
  // 'g'         ... toggle DwLiquidFX
  //

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
//    frameRate(1000);
    frameRate(120);
  }
  
  
  public void release(){
    if(world != null) world.release(); world = null;
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 20);
    
    world.setParticleRadius(0.15f);
//    world.particles.param.falloff_exp1 = 3;
//    world.particles.param.falloff_exp2 = 1;
//    world.particles.param.radius_scale = 2;
    
    world.particles.param.falloff_exp1 = 1;
    world.particles.param.falloff_exp2 = 2;
    world.particles.param.radius_scale = 4;

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    
    if(UPDATE_PHYSICS){
      addParticles();
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
        liquidfx.param.base_LoD = 1;
        liquidfx.param.base_blur_radius = 1;
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
  
  
//  public void mouseReleased(){
//    Vec2 mworld = new Vec2();
//    world.transform.getScreen2box(mouseX, mouseY, mworld);
//    System.out.println("mouse: "+mouseX+", "+mouseY+", "+mworld);
//  }

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  


  public void initScene() {
    
    
    float screenscale =  world.transform.screen_scale;
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    float thick = 10 / screenscale;
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(dimx/4, thick/2, new Vec2(0, 4), 10 * PI/180);
      ground.createFixture(shape, 0);
      
      shape.setAsBox(dimx/6, thick/2, new Vec2(-20, 8), -15 * PI/180);
      ground.createFixture(shape, 0);
      
      world.bodies.add(ground, true, color(220), true, color(0), 1f);
    }
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-14, 24);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 7.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(7, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 100000;
      jd.enableMotor = !true;
      world.createJoint(jd);
    }
    

    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-4, 22);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 5.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(5, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = -0.2f * MathUtils.PI;
      jd.maxMotorTorque = 100000;
      jd.enableMotor = !true;
      world.createJoint(jd);
    }
    

    
    emitter0 = new DwParticleEmitter(world, world.transform);
    emitter1 = new DwParticleEmitter(world, world.transform);
    emitter2 = new DwParticleEmitter(world, world.transform);
    emitter3 = new DwParticleEmitter(world, world.transform);
    
    
    int flags = 0;
    flags |= ParticleType.b2_waterParticle;
    flags |= ParticleType.b2_viscousParticle;
//    flags |= ParticleType.b2_colorMixingParticle;
    
    
    float hsb_s = 100;
    float hsb_b = 100;
    colorMode(HSB, 360, 100, 100);
    
    emitter0.setInScreen( 131, 315, 300,   40, color(  5, hsb_s, hsb_b), flags);
    emitter1.setInScreen( 100, 100, 300,    0, color( 55, hsb_s, hsb_b), flags);
    emitter2.setInScreen(1000, 100, 300, -175, color(115, hsb_s, hsb_b), flags);
    emitter3.setInScreen(1200, 400, 300,  140, color(230, hsb_s, hsb_b), flags);
    
    colorMode(RGB, 255);
  }
  
  
  DwParticleEmitter emitter0;
  DwParticleEmitter emitter1;
  DwParticleEmitter emitter2;
  DwParticleEmitter emitter3;
  
  int particle_counter = 0;
  public void addParticles(){
    
    emitter2.emit_vel = 25 * (sin(particle_counter/200f + PI) * 0.5f  + 0.5f);
    emitter3.emit_vel = 25 * (sin(particle_counter/200f) * 0.5f  + 0.5f);
    
    if(particle_counter % 1 == 0)
    {
      emitter0.emitParticles(2);
      emitter1.emitParticles(2);
      emitter2.emitParticles(2);
      emitter3.emitParticles(2);
    }
    particle_counter++;
  }

  



  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_ParticleEmitter_LiquidFx.class.getName() });
  }
  
}