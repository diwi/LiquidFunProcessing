/**]
 * 
 * PixelFlow | Copyright (C) 2016 Thomas Diewald - http://thomasdiewald.com
 * 
 * A Processing/Java library for high performance GPU-Computing (GLSL).
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */


package testbed;


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import com.thomasdiewald.liquidfun.java.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.DwMouseDragParticles;
import com.thomasdiewald.liquidfun.java.DwParticleRenderGL;
import com.thomasdiewald.liquidfun.java.DwParticleRenderP5;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class Example_template extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;

  
  World world;
  DwViewportTransform transform;
  
  DwDebugDraw debugdraw;
  DwBodyRenderP5 bodyrenderer;
  
  DwParticleRenderGL particlerender;
  DwParticleRenderP5 particlerender_p5;
  
  DwMouseDragParticles mouse_drag_particles;
  DwMouseDragBodies    mouse_drag_bodies;


  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    reset();
    frameRate(600);
  }
  
  
  public void release(){
    if(bodyrenderer != null){
      bodyrenderer.release();
      bodyrenderer = null;
    }
    if(particlerender != null){
      particlerender.release();
      particlerender = null;
    }
    if(particlerender_p5 != null){
      particlerender_p5.release();
      particlerender_p5 = null;
    }
    
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    // Box2d world
    world = new World(new Vec2(0, -10f));
    
    // particle settings
    world.setParticleGravityScale(0.4f);
    world.setParticleDensity(1.2f);
    world.setParticleDamping(1.0f);
    world.setParticleRadius(0.25f);
    
    // Transformation: world <-> screen
    transform = new DwViewportTransform(this);
 
    // Renderer
    debugdraw = new DwDebugDraw(this, world, transform);
    bodyrenderer = new DwBodyRenderP5(this, world, transform);
    particlerender = new DwParticleRenderGL(this, world, transform);
    particlerender_p5 = new DwParticleRenderP5(this, world, transform);

    // mouse interaction
    mouse_drag_particles = new DwMouseDragParticles(world, transform);
    mouse_drag_bodies    = new DwMouseDragBodies(world, transform);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  


  //////////////////////////////////////////////////////////////////////////////
  // draw
  //////////////////////////////////////////////////////////////////////////////
  
  
  public void draw(){
    
    if(UPDATE_PHYSICS){
      mouseDrawAction();
      world.step(1f/60, 8, 4);
      bodyrenderer.update();
      particlerender.update();
      particlerender_p5.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    
    canvas.applyMatrix(transform.mat_box2screen);
    canvas.background(32);
    canvas.fill(200);
    canvas.tint(255,128,96);
    canvas.stroke(0);
    canvas.strokeWeight(1f/transform.screen_scale);

//    DwDebugDraw.displayBodies   (canvas, world);
//    DwDebugDraw.displayParticles(canvas, world);
//    DwDebugDraw.displayJoints   (canvas, world);

    debugdraw.display(canvas);
    
//    bodyrenderer.display(canvas);
//    particlerender.display(canvas);
//    particlerender_p5.display(canvas);

    
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
  }
  

  public void mousePressed() {
    if(mouseButton == LEFT){
      mouse_drag_bodies.begin(mouseX, mouseY);
      if(!mouse_drag_bodies.active){
        mouse_drag_particles.begin(mouseX, mouseY);
      }
    }
  }

  public void mouseDrawAction(){
    mouse_drag_bodies   .update(mouseX, mouseY);
    mouse_drag_particles.update(mouseX, mouseY);
  }
  
  public void mouseReleased() {
    mouse_drag_bodies   .end(mouseX, mouseY);
    mouse_drag_particles.end(mouseX, mouseY);
  }
  
  
  
  
  

  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  public void initScene() {
    
    rectMode(CENTER);

    float b2d_screen_w = width / transform.screen_scale;
    float b2d_screen_h = height / transform.screen_scale;
    float b2d_thickness = 40 / transform.screen_scale;
    
    {
      float radius = 30 / transform.screen_scale;
      CircleShape circle_shape = new CircleShape();
      circle_shape.setRadius(radius);

      FixtureDef fixture_def = new FixtureDef();
      fixture_def.shape = circle_shape;
      fixture_def.density = 1.0f;
      fixture_def.friction = 0.30f;
      fixture_def.restitution = 0.30f;
      
      BodyDef body_def = new BodyDef();
      body_def.type = BodyType.DYNAMIC;
      body_def.angle = 0.0f;
      body_def.position.x = -1/transform.screen_scale;
      body_def.position.y = b2d_screen_h - 10;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      Fixture fixture = circle_body.createFixture(fixture_def);
      
      bodyrenderer.createShape(circle_body);
      bodyrenderer.styleShape(circle_body, true, color(0,128,255), true, color(0), 1f);
    }
    
    {
      BodyDef bd = new BodyDef();
      bd.position.set(0, 0);

      Body ground = world.createBody(bd);
      PolygonShape sd = new PolygonShape();
      
      float x, y, w, h;

      // BOTTOM
      x = 0;
      y = 0;
      w = b2d_screen_w;
      h = b2d_thickness;
     
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);

      // TOP
      x = 0;
      y = b2d_screen_h;
      w = b2d_screen_w;
      h = b2d_thickness;
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      // LEFT
      x = -b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h - b2d_thickness;

      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      // RIGHT
      x = +b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h - b2d_thickness;

      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
    }

    createWall(15, 30, 30, 15, 240, 20);
    
    bodyrenderer.createShape(false);
    

    
    
    // Particles
    float box_w = 15;
    float box_h = 16;

    PolygonShape shape = new PolygonShape();
    ParticleGroupDef def = new ParticleGroupDef();

    def.shape = shape;
    def.flags = 0
        | ParticleType.b2_waterParticle
        | ParticleType.b2_viscousParticle
//        | ParticleType.b2_tensileParticle
//        | ParticleType.b2_colorMixingParticle
        ;
    def.groupFlags = 0;
    
    def.color = new ParticleColor(new Color3f(0.9f, 0.25f, 0.1f));
    shape.setAsBox(box_w, box_h, new Vec2(-(box_w*1.5f), box_h*1.5f), 0);
    ParticleGroup particle_group = world.createParticleGroup(def);
    
//    particlerender_p5.add(particle_group);
  }
  
  
  
  
  
  
  

  public void createWall(int numx, int numy, float dimx, float dimy, float tx, float ty){
    
    dimx /= transform.screen_scale;
    dimy /= transform.screen_scale;
    
    tx /= transform.screen_scale;
    ty /= transform.screen_scale;
    
    PolygonShape cube = new PolygonShape();
    cube.setAsBox(dimx*0.5f, dimy*0.5f);
    
    FixtureDef fixture_def = new FixtureDef();
    fixture_def.shape = cube;
    fixture_def.density = 1;
    fixture_def.friction = 0.50f;
    fixture_def.restitution = 0.5f;
    
    BodyDef body_def = new BodyDef();
    body_def.type = BodyType.DYNAMIC;
    body_def.angle = 0.0f;
    body_def.allowSleep = true;
    

        
    float ox = 0;
    float oy = dimy/2;
    for(int y = 0; y < numy; y++){
      for(int x = 0; x < numx; x++){
        boolean odd_row = (y & 1) == 1;
        if(odd_row && x == numx-1) continue;

        ox = -numx * dimx * 0.5f;
        ox += odd_row ? dimx * 0.5f : 0;
        
        body_def.position.x = tx + ox + x * (dimx);
        body_def.position.y = ty + oy + y * (dimy);
   
        Body cube_body = world.createBody(body_def);
        Fixture fixture = cube_body.createFixture(fixture_def);
       
      }
    }
   
  }
  
  
  
  

  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { Example_template.class.getName() });
  }
  
}