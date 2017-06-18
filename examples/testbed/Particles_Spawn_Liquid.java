/**]
 * 
 * PixelFlow | Copyright (C) 2016 Thomas Diewald - http://thomasdiewald.com
 * 
 * A Processing/Java library for high performance GPU-Computing (GLSL).
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */


package testbed;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import com.thomasdiewald.liquidfun.java.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.DwMouseDragParticles;
import com.thomasdiewald.liquidfun.java.DwParticleDestroyer;
import com.thomasdiewald.liquidfun.java.DwParticleRender;
import com.thomasdiewald.liquidfun.java.DwParticleRenderGL;
import com.thomasdiewald.liquidfun.java.DwParticleRenderP5;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwParticleFluidFX;

import processing.core.*;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;



public class Particles_Spawn_Liquid extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean USE_PARTICLE_GL_RENDER = !true;
  boolean UPDATE_PHYSICS = true;
  
  DwPixelFlow pixelflow;
  World world;
  DwViewportTransform transform;

  DwDebugDraw debug_render;
  DwParticleRenderGL particle_render_gl;
  DwParticleRenderP5 particle_render_p5;
  DwBodyRenderP5 body_render;
  
  DwMouseDragBodies    body_dragger;
  DwMouseDragParticles particle_dragger   ;
  DwParticleDestroyer  particle_destroyer ;
  ParticleGroupDef     particle_group_def ;
  ParticleGroup        particle_group_prev;
  
  
  DwParticleFluidFX particle_fluidfx;
  
  PGraphics2D pg_particles;
  PGraphics2D pg_checker;
  
  PImage sprite;

  
  public int PARTICLE_TYPE_INDEX = 0;
  
  boolean APPLY_LIQUID_FX = !true;
  
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
  }
  

  public void reset(){
    
    release();
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    
    
    sprite = loadImage("../data/sprite.png");
//    sprite = DwUtils.createSprite(this, 64, 1, 1, 1);
    
    transform = new DwViewportTransform(this);
    
    world = new World(new Vec2(0, -10f));
    world.setParticleGravityScale(0.4f);
    world.setParticleDensity(1.2f);
    world.setParticleDamping(1.0f);
    world.setParticleRadius(0.15f);
    
    debug_render = new DwDebugDraw(this, world, transform);
    
    
    particle_dragger    = new DwMouseDragParticles(world, transform);
    particle_destroyer  = new DwParticleDestroyer();
    particle_group_def  = new ParticleGroupDef();
    particle_group_prev = null;
    
    body_render = new DwBodyRenderP5(this, world, transform);

    
    particle_render_gl = new DwParticleRenderGL(this, world, transform);
    particle_render_gl.param.tex_sprite = sprite;
    particle_render_gl.param.falloff_exp1 = 1f;
    particle_render_gl.param.falloff_exp2 = 2f;
    particle_render_gl.param.falloff_mult = 1f;
    particle_render_gl.param.radius_scale = 4f;
    particle_render_gl.param.color_mult   = 1f;
    

    particle_render_p5 = new DwParticleRenderP5(this, world, transform);
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
    mouseDrawAction();
    
    DwParticleRender particle_render = USE_PARTICLE_GL_RENDER ? particle_render_gl : particle_render_p5;
    
    if(UPDATE_PHYSICS){
      world.step(1f/60, 8, 4);
     
      body_render.update();
   
    }
    
    particle_render.update();
    
    int BACKGROUND = 16;
    
    
//    pg_particles.beginDraw();
//    pg_particles.clear();
//    pg_particles.background(BACKGROUND, 0);
//    pg_particles.blendMode(REPLACE);
//    pg_particles.image(pg_checker, 0, 0);
//    pg_particles.blendMode(BLEND);
//    pg_particles.applyMatrix(transform.mat_box2screen);
//    particle_render.display(pg_particles);
//    
//    
//    if(APPLY_LIQUID_FX){
//      particle_fluidfx.apply(pg_particles);
//    }

    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.background(BACKGROUND);
    canvas.blendMode(REPLACE);
    canvas.tint(255);
    canvas.image(pg_checker, 0, 0);
    canvas.blendMode(BLEND);
    canvas.image(pg_particles, 0, 0);
    
    canvas.pushMatrix();
    canvas.applyMatrix(transform.mat_box2screen);
    canvas.tint(255);
    canvas.fill(255);
    canvas.strokeWeight(1f/transform.screen_scale);
    canvas.stroke(128);
//    DwDebugDraw.displayBodies   (canvas, world);
//    DwDebugDraw.displayParticles(canvas, world);
//    DwDebugDraw.displayJoints   (canvas, world);
//    debug_render.display(canvas);
    

    particle_render.display(canvas);
    body_render.display(canvas);
    
 

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
    
    float sizex = transform.box2d_dimx;
    float sizey = transform.box2d_dimy;
    
    float sizexh = sizex * 0.5f;
    float sizeyh = sizey * 0.5f;

    ChainShape shape = new ChainShape();
    Vec2[] vertices = new Vec2[] {new Vec2(-sizexh, 0), new Vec2(sizexh, 0), new Vec2(sizexh, sizey), new Vec2(-sizexh, sizey)};
    shape.createLoop(vertices, 4);
    groundBody.createFixture(shape, 0.0f);
    
    Vec2 pos = transform.getScreen2box(width/2,  height/2, (Vec2) null);
    addParticlesAtlocation(pos, 15);
  }
  
  
  

  
  
  int particle_color_hue = 5;
  
  Color3f createHSBColor(float hsb_h, float hsb_s, float hsb_b){
    colorMode(HSB, 360, 100, 100);
    int rgb = color(hsb_h, hsb_s, hsb_b);
    colorMode(RGB, 255, 255, 255);
    
    float r = ((rgb >> 16) & 0xFF) / 255f;
    float g = ((rgb >>  8) & 0xFF) / 255f;
    float b = ((rgb >>  0) & 0xFF) / 255f;
    return new Color3f(r,g,b);
  }
  

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
    
    particle_group_def.flags = 0;
    particle_group_def.groupFlags = 0;
    
    
    int col_dif = 60;
    int col_off = 5;

    switch (PARTICLE_TYPE_INDEX) {
 
      case 0:
        particle_color_hue = 0 * col_dif + col_off;
        particle_group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_group_def.groupFlags = 0;
        break;
      case 1:
        particle_color_hue = 1 * col_dif + col_off;
        particle_group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        break;
      case 2:
        particle_color_hue = 2 * col_dif + col_off;
        particle_group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        break;
      case 3:
        particle_color_hue = 3 * col_dif + col_off;
        particle_group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        break;
      case 4:
        particle_color_hue = 4 * col_dif + col_off;
        particle_group_def.groupFlags =  ParticleGroupType.b2_rigidParticleGroup | ParticleGroupType.b2_solidParticleGroup;
        break;
      case 5:
        particle_color_hue = 5 * col_dif + col_off;
        particle_group_def.flags = ParticleType.b2_wallParticle;
        particle_group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        break;
      
    }
    
    
    particle_color_hue %= 360;
  }

  
  
  Vec2 b2_mouse = new Vec2();


  public void mousePressed() {
    transform.getScreen2box(mouseX, mouseY, b2_mouse);
    if(mouseButton == LEFT){
      addParticlesAtlocation(b2_mouse);
    }
    if(mouseButton == CENTER){
      particle_dragger.begin(mouseX, mouseY);
    }
    if(mouseButton == RIGHT){
      particle_destroyer.destroyParticlesAtLocation(world, b2_mouse, 2f, false);
    }
  }

  public void mouseDragged() {
    transform.getScreen2box(mouseX, mouseY, b2_mouse);
    if(mouseButton == LEFT){
      addParticlesAtlocation(b2_mouse);
    }
  }
  
  public void mouseDrawAction(){
    transform.getScreen2box(mouseX, mouseY, b2_mouse);
    
    if(mousePressed){
      if(mouseButton == CENTER){
        particle_dragger.update(mouseX, mouseY);
      }
      if(mouseButton == RIGHT){
        particle_destroyer.destroyParticlesAtLocation(world, b2_mouse, 2f, false);
      }
    }
  }


  public void mouseReleased() {
    transform.getScreen2box(mouseX, mouseY, b2_mouse);
    particle_group_prev = null;
    particle_dragger.end(mouseX, mouseY);
  }
  
  public void addParticlesAtlocation(Vec2 pos_box2d){
    addParticlesAtlocation(pos_box2d, 2);
  }

  public void addParticlesAtlocation(Vec2 pos_box2d, float radius){
  
    if (UPDATE_PHYSICS) {
      CircleShape pshape = new CircleShape();
      pshape.m_p.set(pos_box2d);
      pshape.m_radius = radius;
      
      if((particle_group_def.groupFlags & ParticleGroupType.b2_solidParticleGroup) != 0){
        pshape.m_radius = 0.8f;
      }
           
      
      particle_destroyer.destroyParticlesInShape(world, pshape, false);

      particle_group_def.shape = pshape;
      particle_group_def.setColor(createHSBColor(particle_color_hue, 90, 90));
 
      ParticleGroup group = world.createParticleGroup(particle_group_def);
//      genParticleColors(group, particle_color_hue);
//      createParticlePShapes(group, particle_color_hue);
      
      if (particle_group_prev != null && group.getGroupFlags() == particle_group_prev.getGroupFlags()) {
        world.joinParticleGroups(particle_group_prev, group);
      } else {
        particle_group_prev = group;
      }

    }
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
  
  
  
 
  PShape shapeStyle(PShape shp, boolean bfill, int fill, boolean bstroke, int stroke, float strokeweight){
    shp.setFill(bfill);
    shp.setFill(fill);
    shp.setStroke(bstroke);
    shp.setStroke(stroke);
    shp.setStrokeWeight(strokeweight);
    return shp;
  }
  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { Particles_Spawn_Liquid.class.getName() });
  }
  
}