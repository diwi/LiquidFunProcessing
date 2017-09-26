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



package liquidfun_DrawingParticles_LiquidFx;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;


public class liquidfun_DrawingParticles_LiquidFx extends PApplet {

  //
  // Dynamically drawing particles width different properties (types).
  // ... water, viscous, tensile, powder, wall
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
  
  PFont font;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
    PJOGL.profile = 3;
  }


  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
//    sprite = loadImage("sprite.png");
    font = createFont("../data/SourceCodePro-Regular.ttf", 12);
   
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

    setParticleSpawnProperties(spawn_type);

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
//      world.bodies.display(canvas);
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
      world.bodies.display((PGraphics2D) this.g);
      world.drawBulletSpawnTrack(this.g);
      popMatrix();
    }
    
    
    
    int tx = 16;
    int ty = 16;
    int gy = 14;
    textFont(font);
    fill(220);
    text("LMB + SHIFT   shoot bullet" , tx, ty+=gy);
    text("MMB           draw particles"  , tx, ty+=gy);
    text("RMB           delete particles", tx, ty+=gy);
    ty+=gy;
    text("'1'  water"   , tx, ty+=gy);
    text("'2'  viscous" , tx, ty+=gy);
    text("'3'  tensile" , tx, ty+=gy);
    text("'4'  powder"  , tx, ty+=gy);
    ty+=gy;
    text("'5'  wall"    , tx, ty+=gy);
    
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
    if(key >= '1' && key <= '6') setParticleSpawnProperties(key - '1');
    if(key == 'g') APPLY_LIQUID_FX = !APPLY_LIQUID_FX;
  }

  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  int spawn_type = 0;
 
  public void setParticleSpawnProperties(int type){
    
    spawn_type = type;

    int COLOR_MIX = 0;
//    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    DwParticleSpawn particle_spawn = world.mouse_spawn_particles;
    particle_spawn.join_groups = false;
    particle_spawn.setCircleShape(40);
    
    colorMode(HSB, 360, 100, 100);

    switch (type) {
      case 0:
        particle_spawn.group_def.setColor(color(225, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_spawn.group_def.setColor(color(100, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 2:
        particle_spawn.group_def.setColor(color(0, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 3:
        particle_spawn.group_def.setColor(color(60, 50, 100));
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 4:
        particle_spawn.group_def.setColor(color(330, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        particle_spawn.join_groups = true;
        particle_spawn.setCircleShape(20);
        break;
    }
    
    colorMode(RGB, 255);

  }



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
    
    FixtureDef fixture_def = new FixtureDef();
    fixture_def.density = 10f;
    fixture_def.friction = 0.30f;
    fixture_def.restitution = 0.30f;
    
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    
    {
      Body body = world.createBody(bd);
      
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(2, 4, new Vec2(0, 4), 0);
      
      fixture_def.shape = shape;
      body.createFixture(fixture_def);
      
      world.bodies.add(body, true, color(200), false, color(0), 1f);
    }
    
    
    {
      Body body = world.createBody(bd);

      CircleShape shape = new CircleShape();
      shape.m_p.set(dimxh/2, dimyh);
      shape.m_radius = 2.0f;
      
      fixture_def.shape = shape;
      body.createFixture(fixture_def);
      
      world.bodies.add(body, true, color(200), false, color(0), 1f);
    }
    
    {
      float screen_x = width/2;
      float screen_y = height/2;
      float size_x = width/2;
      float size_y = height/2;
      world.mouse_spawn_particles.setBoxShape(size_x, size_y);
      world.mouse_spawn_particles.spawn(screen_x, screen_y);
      
      setParticleSpawnProperties(spawn_type);
    }
  }


  
  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_DrawingParticles_LiquidFx.class.getName() });
  }

}