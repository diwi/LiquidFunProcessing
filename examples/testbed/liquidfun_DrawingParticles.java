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
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import org.jbox2d.collision.shapes.ChainShape;
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
    if(world != null) world.release(); world = null;
  }


  public void reset(){
    // release old resources
    release();

    world = new DwWorld(this, 18);


    setParticleSpawnProperties(spawn_type);

    // create scene: rigid bodies, particles, etc ...
    initScene();
    
  }



  public void draw(){

    if(UPDATE_PHYSICS){
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
    PApplet.main(new String[] { liquidfun_DrawingParticles.class.getName() });
  }

}