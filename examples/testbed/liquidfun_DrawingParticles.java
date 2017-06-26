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
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_DrawingParticles extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;

  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;
  DwBodyGroup bodies;
  DwParticleRenderGL particles;

  DwParticleDestroyer  particle_destroyer;
//  DwParticleSpawn      particle_spawn;

//  PImage sprite;
  
  PFont font;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }


  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);

//    sprite = loadImage("sprite.png");
    font = createFont("data/SourceCodePro-Regular.ttf", 12);

    reset();
    frameRate(120);
  }


  public void release(){
    if(bodies    != null) bodies   .release(); bodies    = null;
    if(particles != null) particles.release(); particles = null;
    if(world     != null) world    .release(); world     = null;
  }


  public void reset(){
    // release old resources
    release();

    world = new DwWorld(this, 18);
    world.transform.setScreen(width, height, 18, width/2, height);

    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);

    particles = new DwParticleRenderGL(this, world, world.transform);
//    particles.param.tex_sprite = sprite;
    particles.param.falloff_exp1 = 1;
    particles.param.falloff_exp2 = 5;
    particles.param.radius_scale = 4f;
    particles.param.falloff_mult = 1;

    particle_destroyer  = new DwParticleDestroyer(world, world.transform);
//    particle_spawn      = new DwParticleSpawn(world, world.transform);

    
    setParticleProperties(spawn_type);

    // create scene: rigid bodies, particles, etc ...
    initScene();
    
  }



  public void draw(){

    bodies.addBullet(true, color(255), true, color(0), 1f);

    if(UPDATE_PHYSICS){
      mouseDrawAction();
      world.update();
      particles.update();
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
      bodies.display(canvas);
      particles.display(canvas);
    }
    canvas.popMatrix();
    
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
    if(key >= '1' && key <= '6') setParticleProperties(key - '1');
  }

  
  int spawn_radius = 40;

  public void mousePressed() {
    if(mouseButton == RIGHT){
      particle_destroyer.destroyParticles(mouseX, mouseY, spawn_radius);
    }
  }
  
  
  public void mouseDrawAction(){    
    if(mousePressed){
      if(mouseButton == RIGHT){
        particle_destroyer.destroyParticles(mouseX, mouseY, spawn_radius);
      }
    }
  }

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  int spawn_type = 0;
 
  public void setParticleProperties(int type){
    
    spawn_type = type;

    int COLOR_MIX = 0;
//    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    DwParticleSpawn particle_spawn = world.mouse_spawn_particles;
    particle_spawn.join_groups = false;
    particle_spawn.setCircleShape(40);

    switch (type) {
      case 0:
        particle_spawn.group_def.setColor(createHSBColor(225, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_spawn.group_def.setColor(createHSBColor(100, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 2:
        particle_spawn.group_def.setColor(createHSBColor(0, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 3:
        particle_spawn.group_def.setColor(createHSBColor(60, 50, 100));
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 4:
        particle_spawn.group_def.setColor(createHSBColor(330, 100, 100));
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        particle_spawn.join_groups = true;
        particle_spawn.setCircleShape(20);
        break;
    }

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

      bodies.add(ground, false, color(0), true, color(0), 1f);
    }
    
    {
      float screen_x = width/2;
      float screen_y = height/2;
      float size_x = width/2;
      float size_y = height/2;
      world.mouse_spawn_particles.setBoxShape(size_x, size_y);
      world.mouse_spawn_particles.spawn(screen_x, screen_y);
      
      setParticleProperties(spawn_type);
    }
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
    PApplet.main(new String[] { liquidfun_DrawingParticles.class.getName() });
  }

}