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


import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import processing.core.PApplet;
import processing.opengl.PGraphics2D;


public class liquidfun_ParticleColors_LiquidFx extends PApplet {

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
  PGraphics2D pg_checkerboard;

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
    
    // used for background texture
    pg_checkerboard = DwUtils.createCheckerBoard(this, width, height, 100, color(32,0), color(128,0));

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
    world.particles.param.falloff_exp1 = 1;
    world.particles.param.falloff_exp2 = 1;
    world.particles.param.radius_scale = 4;
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    if(UPDATE_PHYSICS){
      world.update();
    }
    

    int BACKGROUND = 0;
    
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
      canvas.blendMode(REPLACE);
      canvas.image(pg_checkerboard, 0, 0);
      canvas.blendMode(BLEND);
      world.applyTransform(canvas);
//      canvas.blendMode(DARKEST);
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
        liquidfx.param.sss_enabled = true;
        liquidfx.param.sss_LoD = 5;
        liquidfx.param.sss_decay = 0.55f;
        liquidfx.apply(canvas);
      }
      
 
      blendMode(REPLACE);
      image(pg_checkerboard, 0, 0);
      blendMode(BLEND);
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
      pd.shape = shape;
      pd.flags = 0
         | ParticleType.b2_waterParticle
         | ParticleType.b2_viscousParticle
//         | ParticleType.b2_colorMixingParticle
//         | ParticleType.b2_powderParticle
//         | ParticleType.b2_springParticle
//         | ParticleType.b2_tensileParticle
         ;
      
      float sx = dimxh * 0.2f;
      float sy = dimyh * 0.95f;
      
      
      pd.flags |= ParticleType.b2_colorMixingParticle; // set flag
      shape.setAsBox(sx, sy, new Vec2(-3*dimxh/4f, dimyh), 0);
      ParticleGroup group1 = world.createParticleGroup(pd);
      
      pd.flags |= ParticleType.b2_colorMixingParticle; // set flag
      shape.setAsBox(sx, sy, new Vec2(0, dimyh), 0);
      ParticleGroup group2 = world.createParticleGroup(pd);
      
      pd.flags &= ~ParticleType.b2_colorMixingParticle; // clear flag
      shape.setAsBox(sx, sy, new Vec2(+3*dimxh/4f, dimyh), 0);
      ParticleGroup group3 = world.createParticleGroup(pd);
      

      generteParticleColors(group1,   5);
      generteParticleColors(group2, 115);
      generteParticleColors(group3, 225);
    }
  }
  
  
  public void generteParticleColors(ParticleGroup group, int hue){
    int count = group.getParticleCount();
    int from = group.getBufferIndex();
    int to = from + count;
    
    ParticleColor[] colors = world.getParticleColorBuffer();
    Vec2[] pos = world.getParticlePositionBuffer();
    
    colorMode(HSB, 360, 100, 100, 100);
    
    for(int i = from; i < to; i++){
      float x = pos[i].x / 3f;
      float y = pos[i].y / 3f;
      float noisexy = noise(x, y, i /(float)count) * 2 - 1;
      float rand = random(-1, +1);

      float hsb_h = hue + 90 * rand * noisexy;
      float hsb_s = 100;
      float hsb_b = 100;
      float hsb_a = 50 + noisexy * 50;
      
      hsb_h = (hsb_h + 360) % 360;

      int argb = color(hsb_h, hsb_s, hsb_b, hsb_a);
      
      colors[i].set(argb);
    }
    
    colorMode(RGB, 255);
  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_ParticleColors_LiquidFx.class.getName() });
  }
  
}