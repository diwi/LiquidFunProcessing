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



package liquidfun_DamBreak_LiquidFx;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_DamBreak_LiquidFx extends PApplet {

  //
  // Simulation of a clash of two big particle-groups. 
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
  
  DwPixelFlow pixelflow;
  DwLiquidFX liquidfx;
  
  PGraphics2D pg_particles;

//  PImage sprite;

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
    
    world = new DwWorld(this, 18);
    world.particles.param.falloff_exp1 = 3;
    world.particles.param.falloff_exp2 = 1;
    world.particles.param.radius_scale = 2;
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    if(UPDATE_PHYSICS){
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
      world.particles.display(canvas, 0);
      canvas.endDraw();
      
      if(APPLY_LIQUID_FX)
      {
        liquidfx.param.base_LoD = 1;
        liquidfx.param.base_blur_radius = 2;
        liquidfx.param.base_threshold = 0.7f;
        liquidfx.param.highlight_enabled = true;
        liquidfx.param.highlight_LoD = 1;
        liquidfx.param.highlight_decay = 0.6f;
        liquidfx.param.sss_enabled = true;
        liquidfx.param.sss_LoD = 3;
        liquidfx.param.sss_decay = 0.5f;
        liquidfx.apply(canvas);
      }
      
      background(BACKGROUND);
      image(canvas, 0, 0);
      pushMatrix();
      world.applyTransform(this.g);
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
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/DamBreak.java
  public void initScene() {
    
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    
    float dimxh = dimx/2;
    float dimyh = dimy/2;
    
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      ChainShape shape = new ChainShape();
      Vec2[] vertices = {new Vec2(-dimxh, 0), new Vec2(dimxh, 0), new Vec2(dimxh, dimy), new Vec2(-dimxh, dimy)};
      shape.createLoop(vertices, 4);
      ground.createFixture(shape, 0.0f);
      
      world.bodies.add(ground, false, color(0), true, color(0), 1f);
    }
    
    

    {
      PolygonShape shape = new PolygonShape();
      ParticleGroupDef pd = new ParticleGroupDef();
       
      pd.flags = 0
         | ParticleType.b2_waterParticle
         | ParticleType.b2_viscousParticle
         | ParticleType.b2_colorMixingParticle
//         | ParticleType.b2_powderParticle
//         | ParticleType.b2_springParticle
//         | ParticleType.b2_tensileParticle
         ;
      
      float sx = dimxh * 0.25f;
      float sy = dimyh * 0.95f;
      
      shape.setAsBox(sx, sy, new Vec2(-dimxh/2, dimyh), 0);
      pd.shape = shape;
      pd.setColor(new Color3f(0.00f, 0.2f, 1));
      world.createParticleGroup(pd);
      
      shape.setAsBox(sx, sy, new Vec2(+dimxh/2, dimyh), 0);
      pd.shape = shape;
      pd.setColor(new Color3f(1.00f, 0.2f, 0.00f));
      world.createParticleGroup(pd);
    }
  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_DamBreak_LiquidFx.class.getName() });
  }
  
}