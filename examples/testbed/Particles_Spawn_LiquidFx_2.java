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
import com.thomasdiewald.liquidfun.java.interaction.DwParticleDestroyer;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.liquidfun.java.render.DwParticleRender;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderP5;
import com.thomasdiewald.liquidfun.java.render.deprecated.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.DwUtils;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwParticleFluidFX;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;



public class Particles_Spawn_LiquidFx_2 extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  

  
  DwPixelFlow pixelflow;
  DwWorld world;


  DwParticleRenderGL particle_render_gl;
  DwParticleRenderP5 particle_render_p5;
  DwBodyRenderP5     body_render;
  
  DwParticleDestroyer  particle_destroyer;
  DwParticleSpawn      particle_spawn;

  DwParticleFluidFX particle_fluidfx;
  
  PGraphics2D pg_particles;
  PGraphics2D pg_bodies;
  PGraphics2D pg_checker;
  
  PImage sprite;


  boolean UPDATE_PHYSICS = true;
  boolean APPLY_LIQUID_FX = true;
  boolean USE_PARTICLE_GL_RENDER = true;

  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    PJOGL.profile = 3;
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    
    pixelflow = new DwPixelFlow(this);

    particle_fluidfx = new DwParticleFluidFX(pixelflow);
    
    pg_checker = DwUtils.createCheckerBoard(this, width, height, 128, color(16, 0), color(64, 0));
    
    reset();
    
    frameRate(600);
  }
  
  
  public void release(){
    if(body_render != null){
      body_render.release();
      body_render = null;
    }
    
    if(particle_render_gl != null){
      particle_render_gl.release();
      particle_render_gl = null;
    }
    
    if(particle_render_p5 != null){
      particle_render_p5.release();
      particle_render_p5 = null;
    }

    if(world     != null) world    .release(); world     = null;
  }
  

  
  public void reset(){
    
    release();
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    pg_particles.smooth(0);
    pg_bodies = (PGraphics2D) createGraphics(width, height, P2D);
    pg_bodies.smooth(0);
    
    sprite = loadImage("../data/sprite.png");
    sprite = DwUtils.createSprite(this, 64, 2, 1, 1);
    
    world = new DwWorld(this);

    particle_destroyer  = new DwParticleDestroyer(world, world.transform);
    particle_spawn      = new DwParticleSpawn(world, world.transform);

    body_render = new DwBodyRenderP5(this, world, world.transform);
    
    particle_render_gl = new DwParticleRenderGL(this, world, world.transform);
    particle_render_gl.param.tex_sprite = sprite;
    particle_render_gl.param.falloff_exp1 = 1f;
    particle_render_gl.param.falloff_exp2 = 2f;
    particle_render_gl.param.falloff_mult = 1f;
    particle_render_gl.param.radius_scale = 3f;
    particle_render_gl.param.color_mult   = 1f;
    
    particle_render_p5 = new DwParticleRenderP5(this, world, world.transform);
    particle_render_p5.param.tex_sprite = sprite;
    particle_render_p5.param.falloff_exp1 = 1f;
    particle_render_p5.param.falloff_exp2 = 2f;
    particle_render_p5.param.falloff_mult = 1f;
    particle_render_p5.param.radius_scale = 4f;
    particle_render_p5.param.color_mult   = 1f;
    
    setParticleProperties(PARTICLE_TYPE_INDEX);
    
    background(0);
    
    initTest();
  }
  


  
  public void draw(){
    DwParticleRender particle_render = USE_PARTICLE_GL_RENDER ? particle_render_gl : particle_render_p5;
    
    if(UPDATE_PHYSICS){
      world.update();
      particle_render.update();
    }
    

    int BACKGROUND = 16;

    pg_particles.beginDraw();
    pg_particles.clear();
    pg_particles.background(BACKGROUND, 0);
    pg_particles.blendMode(REPLACE);
    pg_particles.image(pg_checker, 0, 0);
    pg_particles.blendMode(BLEND);
    pg_particles.applyMatrix(world.transform.mat_box2screen);
    body_render.display(pg_particles);
    particle_render.display(pg_particles);

    pg_particles.endDraw();
    
    if(APPLY_LIQUID_FX){
      particle_fluidfx.apply(pg_particles);
    }
    
    pg_bodies.beginDraw();
    pg_bodies.blendMode(REPLACE);
    pg_bodies.image(pg_checker, 0, 0);
    pg_bodies.blendMode(BLEND);
    pg_bodies.applyMatrix(world.transform.mat_box2screen);
    body_render.display(pg_bodies);
    pg_bodies.endDraw();
    
    if(APPLY_LIQUID_FX){
      particle_fluidfx.apply(pg_bodies);
    }
    
    

    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.background(BACKGROUND);
    canvas.blendMode(REPLACE);
    canvas.tint(255);
    canvas.image(pg_checker, 0, 0);
    canvas.blendMode(BLEND);
    canvas.image(pg_particles, 0, 0);
    canvas.image(pg_bodies, 0, 0);
    canvas.pushMatrix();
    canvas.applyMatrix(world.transform.mat_box2screen);
    canvas.tint(255);
    canvas.fill(255);
    canvas.strokeWeight(1f/world.transform.screen_scale);
    canvas.stroke(128);
//    DwDebugDraw.displayBodies   (canvas, world);
//    DwDebugDraw.displayParticles(canvas, world);
//    DwDebugDraw.displayJoints   (canvas, world);
//    debug_render.display(canvas);
//    

//    background(0);
//    particle_render.display(canvas);
//    body_render.display(canvas);
    
 

    canvas.popMatrix();
    


    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  


  
  public void initTest() {
    BodyDef bodyDef = new BodyDef();
    Body groundBody = world.createBody(bodyDef);
    
    float sizex = world.transform.box2d_dimx;
    float sizey = world.transform.box2d_dimy;
    
    float sizexh = sizex * 0.5f;
    float sizeyh = sizey * 0.5f;

    ChainShape shape = new ChainShape();
    Vec2[] vertices = new Vec2[] {new Vec2(-sizexh, 0), new Vec2(sizexh, 0), new Vec2(sizexh, sizey), new Vec2(-sizexh, sizey)};
    shape.createLoop(vertices, 4);
    groundBody.createFixture(shape, 0.0f);
    
    
    body_render.createShape(groundBody);
    body_render.styleShape(groundBody, false, color(0,128,255), true, color(0), 1f);
    

    {
      float radius = 20 / world.transform.screen_scale;
      CircleShape circle_shape = new CircleShape();
      circle_shape.setRadius(radius);

      FixtureDef fixture_def = new FixtureDef();
      fixture_def.shape = circle_shape;
      fixture_def.density = 0.3f;
      fixture_def.friction = 0.30f;
      fixture_def.restitution = 0.30f;
      
      BodyDef body_def = new BodyDef();
      body_def.type = BodyType.DYNAMIC;
      body_def.angle = 0.0f;
      body_def.position.x = 20;
      body_def.position.y = 10;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      circle_body.createFixture(fixture_def);
      
      circle_shape.m_radius = radius * 2.5f;
      circle_shape.m_p.x = +radius * 2.5f;
      circle_body.createFixture(fixture_def);

      circle_shape.m_radius = radius * 2.5f;
      circle_shape.m_p.x = -radius * 2.5f;
      circle_body.createFixture(fixture_def);
      
      body_render.createShape(circle_body);
      body_render.styleShape(circle_body, true, color(0,128,255), !true, color(255), 3f);
    }
    
    body_render.createShape();
    
    particle_spawn.setCircleShape(height/3);
    particle_spawn.spawn(width/2,  height/2);
  }
  
  
  

  
  
  int particle_color_hue = 5;
  int PARTICLE_TYPE_INDEX = 0;

  public void keyReleased() {
    
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'l') APPLY_LIQUID_FX = !APPLY_LIQUID_FX;
    if(key == 'g') USE_PARTICLE_GL_RENDER = !USE_PARTICLE_GL_RENDER;

    if(key >= '1' && key <= '6') setParticleProperties(key - '1');
  }
  

  
  public void setParticleProperties(int type){
    
    PARTICLE_TYPE_INDEX = type;
    
    int COLOR_MIX = 0;
    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    particle_spawn.group_def.flags = 0;
    particle_spawn.group_def.groupFlags = 0;
    
    
    int col_dif = 60;
    int col_off = 5;

    switch (PARTICLE_TYPE_INDEX) {
 
      case 0:
        particle_color_hue = 0 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_color_hue = 1 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        break;
      case 2:
        particle_color_hue = 2 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        break;
      case 3:
        particle_color_hue = 3 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        break;
      case 4:
        particle_color_hue = 4 * col_dif + col_off;
        particle_spawn.group_def.groupFlags =  ParticleGroupType.b2_rigidParticleGroup | ParticleGroupType.b2_solidParticleGroup;
        break;
      case 5:
        particle_color_hue = 5 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        break;
      
    }
    
    
    particle_color_hue %= 360;
    particle_spawn.group_def.color = new ParticleColor(createHSBColor(particle_color_hue, 90, 90));
  }




  public void mousePressed() {
    if(mouseButton == CENTER){
      spawnParticles(mouseX, mouseY);
    }
    if(mouseButton == RIGHT){
      particle_destroyer.destroyParticles(mouseX, mouseY, 30);
    }
  }
  
  public void mouseDragged(){
    if(mouseButton == CENTER){
      spawnParticles(mouseX, mouseY);
    }
  }

  public void mouseDrawAction(){    
    if(mousePressed){
      if(mouseButton == RIGHT){
        particle_destroyer.destroyParticles(mouseX, mouseY, 30);
      }
    }
  }
  
  public void mouseReleased() {
    particle_spawn  .end(mouseX, mouseY);
  }
  
  
  public void spawnParticles(float screen_x, float screen_y){
    float screen_radius = 20;
    particle_spawn.join_groups = false;
    if((particle_spawn.group_def.groupFlags & ParticleGroupType.b2_solidParticleGroup) != 0){
      particle_spawn.join_groups = true;
      screen_radius = 10;
    }
    particle_spawn.setCircleShape(screen_radius);
    particle_spawn.spawn(screen_x, screen_y);
  }
  
  
  public void genParticleColors(ParticleGroup group, int hue){
    int group_size = group.getParticleCount();
    int group_start = group.getBufferIndex();
    genParticleColors(group_start, group_start+group_size, hue);
  }
  
  public void genParticleColors(int idx_from, int idx_to, int hue){
    
    int             particle_num = world.getParticleCount();
    ParticleColor[] particle_col = world.getParticleColorBuffer();
    idx_to = Math.min(idx_to, particle_num);
    
    colorMode(HSB, 360, 100, 100);
    for(int i = idx_from; i < idx_to; i++){ 
      float hsb_h = hue + random(-15, 15);
      float hsb_s = random(20, 100);
      float hsb_b = random(40, 100);
      int   argb  = color(hsb_h, hsb_s, hsb_b);
      
      particle_col[i].a = (byte) ((argb >> 24) & 0xFF);
      particle_col[i].r = (byte) ((argb >> 16) & 0xFF);
      particle_col[i].g = (byte) ((argb >>  8) & 0xFF);
      particle_col[i].b = (byte) ((argb >>  0) & 0xFF);
    }
    colorMode(RGB, 255, 255, 255);
  }
  
  
  
  Color3f createHSBColor(float hsb_h, float hsb_s, float hsb_b){
    colorMode(HSB, 360, 100, 100);
    int rgb = color(hsb_h, hsb_s, hsb_b);
    colorMode(RGB, 255, 255, 255);
    
    float r = ((rgb >> 16) & 0xFF) / 255f;
    float g = ((rgb >>  8) & 0xFF) / 255f;
    float b = ((rgb >>  0) & 0xFF) / 255f;
    return new Color3f(r,g,b);
  }
  
  
 
  public static void main(String args[]) {
    PApplet.main(new String[] { Particles_Spawn_LiquidFx_2.class.getName() });
  }
  
}