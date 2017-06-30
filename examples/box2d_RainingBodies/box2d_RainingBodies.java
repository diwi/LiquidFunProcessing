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



package box2d_RainingBodies;

import com.thomasdiewald.liquidfun.java.DwWorld;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_RainingBodies extends PApplet {
  
  //
  // Randomly falling bodies. Automatically removed from the world if the exceed
  // some boundary.
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
  

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    reset();
    frameRate(120);
  }
  
  
  public void release(){
    if(world != null) world.release(); world = null;  
  }
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 20);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
    if(UPDATE_PHYSICS){
      if(frameCount % 10 == 0){
        addBodies();
      }
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
    if(key == 'r') reset();
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  int MAX_NUM = 800;
  int m_count = 0;
  
  public void initScene() {
    
    m_count = 0;
    
    float screen_scale = world.transform.screen_scale;
    float b2d_screen_w = world.transform.box2d_dimx;
    float b2d_screen_h = world.transform.box2d_dimy;
    float b2d_thickness = 10 / screen_scale;
    
    { // Walls
      BodyDef bd = new BodyDef();
      bd.position.set(0, 0);

      Body ground = world.createBody(bd);
      PolygonShape sd = new PolygonShape();
      
      float x, y, w, h;

      float angle = 60 * PI/180;
      for(int i = 0; i < 10; i++){
        x = random(-0.5f, 0.5f) * b2d_screen_w;
        y = random( 0, 0.5f) * b2d_screen_h;
        w = random(15, 20);
        h = b2d_thickness;
        sd.setAsBox(w/2f, h/2f, new Vec2(x, y), random(-1, 1) * angle);
        ground.createFixture(sd, 0);
      }

      world.bodies.add(ground, true, color(0), !true, color(0), 1f);
    }
  }
  
  
  
  public void addBodies(){
    if (world.getBodyCount() < MAX_NUM) {

      float b2d_screen_w = world.transform.box2d_dimx;
      float b2d_screen_h = world.transform.box2d_dimy;
      
      float x = random(-0.4f, 0.4f) * b2d_screen_w;
      float y = random(-2f, 2) + b2d_screen_h;
      
      float w = random(0.5f, 2.2f);
      float h = random(0.5f, 1.2f);
      
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(x, y);
      Body body = world.createBody(bd);
      
      Shape shape = null;
      if(random(1) < 0.5){
        PolygonShape pshape = new PolygonShape();
        pshape.setAsBox(w, h, new Vec2(0,0), random(TWO_PI));
        shape = pshape;
      } else {
        CircleShape cshape = new CircleShape();
        cshape.m_p.set(0,0);
        cshape.m_radius = w / 2f;
        shape = cshape;
      }
 
      Fixture fixture = body.createFixture(shape, 0.01f);
      fixture.m_friction = 0.1f;
      fixture.m_restitution = 0.5f;
      
      colorMode(HSB, 360, 100, 100);

      float r = (360 * m_count /(float)MAX_NUM) % 360;
      float g = 100;
      float b = 100;
      
      world.bodies.add(body, true, color(r,g,b), true, color(r, g, b *0.5f), 1f);
      colorMode(RGB, 255, 255, 255);
      
      m_count++;
    }
  }
  


 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_RainingBodies.class.getName() });
  }
  
}