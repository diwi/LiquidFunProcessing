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



package _Get_Started_;

import com.thomasdiewald.liquidfun.java.DwWorld;


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class _Get_Started_ extends PApplet {
  
  //
  // A more detailed documented example to get started.
  // All other examples of this library follow the exact same structure as shown here.
  // 
  // setup:
  //   1) init
  //      a new world is created and bodies/joints /particles are added to this world
  // 
  // draw:
  //   1) update 
  //      the world gets updated (physics simulations step)
  //      bodies, joints, particles are all updated internally
  //   2) render
  //
  //
  // references, tutorials:
  
  // - Box2D tutorial:
  //   http://www.iforce2d.net/b2dtut/
  //
  // - LiquidFun guide:
  //   http://google.github.io/liquidfun/Programmers-Guide/html/index.html
  //
  // - jBox2d Reference:
  //   TODO
  // - LiquidFunProcessing Reference:
  //   http://thomasdiewald.com/processing/libraries/liquidfun/reference/index.html
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
  //
  

  // window dimension
  int viewport_w = 1280;
  int viewport_h = 720;
  // window position
  int viewport_x = 230;
  int viewport_y = 0;
  
  // some state variables for controlling the program flow
  boolean UPDATE_PHYSICS = true;  // key 't' ... to enable/disable
  boolean USE_DEBUG_DRAW = false; // key 'f' ... to enable/disable

  // main world-object, contains all bodies/joints/particles/mouse actions etc...
  DwWorld world;
  
  
  public void settings(){
    // P2D renderer (OpenGL)
    size(viewport_w, viewport_h, P2D);
    // some MSAA antialiasing
    smooth(8); 
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    
    // initialize the scene, ... reset() is also called when key 'r' is pressed
    reset();
    
    frameRate(120);
  }
  
  
  // (important) always release an existing world, before creating a new one
  // this releases all resources managed by DwWorld
  // e.g. OpenGL-Buffers used for particle rendering, etc...
  public void release(){
    if(world != null) world.release(); world = null;  
  }
  
  
  public void reset(){
    // release old resources before (re)creating a new world
    release();
    
    // create a new box2d world, ... the scale is 20
    world = new DwWorld(this, 20);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
    // update physics simulation
    if(UPDATE_PHYSICS){
      
      // dynamically adding bodies to the world
      addBodies();
      
      // next update step
      world.update();
      
      // use the following if you want to have better control of the next update step
      // world.update(timestep, iter_velocity, iter_position);
    }

    // canvas will be the render-target for the bodies/joints/particles.
    // In this example it is just the primary PGraphics "this.g"
    // but can be an offscreen render-target too if any postprocessing is applied
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    // background
    canvas.background(32);
    
    // push current matrix
    canvas.pushMatrix();
    
    // apply box2d matrix.
    // a box2d-world has its own coordinate system (origin, scale), which in this 
    // example is set automatically in DwWorld at initialization.
    // A custom transformation can be set too (see reference):
    // - world.transform.setCamera(x, y, scale);
    // - world.transform.setScreen(dim_x, dim_y, scale, origin_x, origin_y)
    // - ... etc.
    world.applyTransform(canvas);
    
    // if a bullet was shot (LMB + SHIFT + mouse-drag) the bullet gets automatically
    // added to the world.bodies scene-graph and is managed internally.
    // the spawn-animation however is not really a box2d entity and therefore
    // needs its own render-call, ... or can be ignored at all.
    world.drawBulletSpawnTrack(canvas);
    
    // render all bodies/joints/particles.
    if(USE_DEBUG_DRAW){
      
      // debug draw is a very comfortable way to render everything during developing.
      // the following flags can be used:
      //   DebugDraw.e_shapeBit            ... Draw shapes
      //   DebugDraw.e_jointBit            ... Draw joints
      //   DebugDraw.e_aabbBit             ... Draw axis aligned bounding boxes
      //   DebugDraw.e_pairBit             ... Draw pairs of connected object
      //   DebugDraw.e_centerOfMassBit     ... Draw center of mass frame
      //   DebugDraw.e_dynamicTreeBit      ... Draw dynamic tree
      //   DebugDraw.e_wireframeDrawingBit ... Draw only the wireframe
      //     
      //   world.debug_draw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit);
      
      //   world.debug_draw.appendFlags(flags);
      //   world.debug_draw.clearFlags(flags);
      
      world.displayDebugDraw(canvas);
      // DwDebugDraw.display(canvas, world); // alternative way
      
    } else {
      
      // default rendering of all bodies/joints/particles the are part of
      // the box2d world.
      world.display(canvas);
      
      // alternatively, bodies/joints and particles can be rendered separately
      // world.bodies.display(canvas);    // ... to render bodies and joints
      // world.particles.display(canvas); // ... to render particles
      
      // if even more separation is required, a custom DwBodyGroup instance
      // can be used next to the default world.bodies scene graph.

      // particles can be rendered in separate groups too, see the examples
      // liquidfun_ParticleRenderGroups*
    }
    
    
    // restore matrix
    canvas.popMatrix();
    
    
    // info, window title
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  


  
  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////

  public void keyReleased(){
    if(key == 'r') reset();
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  public void initScene() {
    
    // recommended reading:
    //
    // http://www.iforce2d.net/b2dtut/bodies
    // http://www.iforce2d.net/b2dtut/fixtures
    //
    // http://google.github.io/liquidfun/Programmers-Guide/html/md__chapter06__bodies.html
    // http://google.github.io/liquidfun/Programmers-Guide/html/md__chapter07__fixtures.html
    //
    
    // box2d has its own coordinate system.
    float screen_scale = world.transform.screen_scale;
    float bdimx = world.transform.box2d_dimx;
    float bdimy = world.transform.box2d_dimy;
    float bhick = 20 / screen_scale;
    

    
    // create a static ground body
    { 
      
      // 1.0) create a new Body 
      //      a body is basically just a collection of abstract physics attributes:
      //      velocity (angular, linear), position, mass, etc...
      //      http://www.iforce2d.net/b2dtut/bodies
      
      // 1.1) BodyDef 
      //      contains all information required to create a new body
      BodyDef bd = new BodyDef();

      // 1.2) create the actual Body
      Body ground = world.createBody(bd);

      
      
      
      // 2.0) create Fixtures for this body
      //      a body needs further physical properties, like shape, density, etc...
      //      http://www.iforce2d.net/b2dtut/fixtures
      
      // 2.1) create a shape
      
      PolygonShape sd = new PolygonShape();
      
      // ... in this case a polygon-shape in form of a box
      sd.setAsBox(bdimx/4f, bhick/2f, new Vec2(8, 10), 0);
      
      // 2.2) create a fixture for this body
      //      density is set to 0, which makes this body static (zero mass)
      ground.createFixture(sd, 0);
 
      // ... another box, another position
      sd.setAsBox(bdimx/4f, bhick/2f, new Vec2(-8, 2), 0);
      
      // 2.3) create another fixture for this body
      //      density is set to 0, which makes this body static (zero mass)
      ground.createFixture(sd, 0);
      
      
      
      // 3.0) add the body to the scene-graph for rendering
      world.bodies.add(ground, true, color(0), !true, color(0), 1f);
    }
    
    
    
    
    
    // create particles
    {
      // 1) particles-settings for simulation
      world.setParticleRadius(0.15f);
      world.setParticleDamping(1);
      // world.setParticle...(...);
      
      // 2) particles-settings for rendering
      world.particles.param.tex_sprite   = null;
      world.particles.param.falloff_exp1 = 4f;
      world.particles.param.falloff_exp2 = 1f;
      world.particles.param.falloff_mult = 1f;
      world.particles.param.radius_scale = 1f;
      world.particles.param.color_mult   = 1f;
      
      
      world.mouse_spawn_particles.group_def.setColor(color(64,255,0));
      
      
      // 3) create particles (or particle-groups)
      
      // shape, in which the particles are spawned
      PolygonShape pshape = new PolygonShape();
      
      // particles (group)definition
      ParticleGroupDef pd = new ParticleGroupDef();  
      pd.shape = pshape;
      pd.flags = 0
         | ParticleType.b2_waterParticle
         | ParticleType.b2_viscousParticle
//         | ParticleType.b2_colorMixingParticle
//         | ParticleType.b2_powderParticle
//         | ParticleType.b2_springParticle
//         | ParticleType.b2_tensileParticle
         ;
      
      float sx = bdimx * 0.10f;
      float sy = bdimy * 0.25f;
      
      // set shape
      pshape.setAsBox(sx, sy, new Vec2(-sx, bdimy), 0);
      // set color
      pd.setColor(color(0, 64, 255));
      // create a new group of particles
      world.createParticleGroup(pd);
      
      // set shape
      pshape.setAsBox(sx, sy, new Vec2(+sx, bdimy), 0);
      // set color
      pd.setColor(color(255, 64, 0));
      // create a new group of particles
      world.createParticleGroup(pd);
    }
    
    
  }
  
  
  
  
  
  // body counter, for creating a color transition
  int body_count = 0;
  

  public void addBodies(){
    // add a new body every 20th frame.
    if(frameCount % 20 == 0){
      addBody();
    }
  }
  
  public void addBody(){
    
    // recommended reading:
    //
    // http://www.iforce2d.net/b2dtut/bodies
    // http://www.iforce2d.net/b2dtut/fixtures
    
    
    // box2d has its own coordinate system.
    float screen_scale = world.transform.screen_scale;
    float bdimx = world.transform.box2d_dimx;
    float bdimy = world.transform.box2d_dimy;
    float bhick = 20 / screen_scale;
    
    
    // body position and size
    float x = 0;
    float y = bdimy;
    float w = random(0.5f, 2.2f);
    float h = random(0.5f, 1.2f);
    
    // create a new BodyDefinition
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(x, y);
    
    // create a new body, using BodyDef
    Body body = world.createBody(bd);
    
    
    // create a new FixtureDefinition
    FixtureDef fd = new FixtureDef();
    fd.density = 1f;
    fd.friction = 0.1f;
    fd.restitution = 0.2f;
    
    // randomly choose between a circle or a box shape for this fixture
    if(random(1) < 0.5){
      PolygonShape pshape = new PolygonShape();
      pshape.setAsBox(w, h, new Vec2(0,0), random(TWO_PI));
      fd.shape = pshape;
    } else {
      CircleShape cshape = new CircleShape();
      cshape.m_p.set(0,0);
      cshape.m_radius = w / 2f;
      fd.shape = cshape;
    }
 
    // create a new fixture, using FixtureDef
    body.createFixture(fd);

    // set colorMode to HSB
    colorMode(HSB, 360, 100, 100);

    // create color-components
    float r = body_count % 360;
    float g = 100;
    float b = 100;
    
    // add the body to the scene-graph for rendering
    world.bodies.add(body, true, color(r,g,b), true, color(r, g, b * 0.5f), 1f);
    
    // reset colorMode to RGB
    colorMode(RGB, 255);
    
    // increase counter
    body_count++;

  }
  


 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { _Get_Started_.class.getName() });
  }
  
}