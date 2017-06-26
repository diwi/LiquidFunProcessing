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

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_BrickWall extends PApplet {

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
      world.bodies.display(canvas);
      world.particles.display(canvas);
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

  public void initScene() {
    
    float screen_scale = world.transform.screen_scale;
    float b2d_screen_w = world.transform.box2d_dimx;
    float b2d_screen_h = world.transform.box2d_dimy;
    float b2d_thickness = 20 / screen_scale;
    
    {

      float radius = 25 / screen_scale;
      CircleShape circle_shape = new CircleShape();
      circle_shape.setRadius(radius);

      FixtureDef fixture_def = new FixtureDef();
      fixture_def.shape = circle_shape;
      fixture_def.density = 10f;
      fixture_def.friction = 0.30f;
      fixture_def.restitution = 0.30f;
      
      BodyDef body_def = new BodyDef();
      body_def.type = BodyType.DYNAMIC;
      body_def.angle = 0.0f;
      body_def.position.x = -1/screen_scale;
      body_def.position.y = b2d_screen_h - 10;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      circle_body.createFixture(fixture_def);
      
      world.bodies.add(circle_body, true, color(64, 125, 255), true, color(0), 1f);
    }
    
    { // Walls
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

      world.bodies.add(ground, true, color(0), !true, color(0), 1f);
    }

    createWall(10, 20, 40, 20, 0, 10);
 
  }
  
  
  public void createWall(int numx, int numy, float dimx, float dimy, float tx, float ty){
    
    float scree_scale = world.transform.screen_scale;
    
    
    dimx /= scree_scale;
    dimy /= scree_scale;
    
    tx /= scree_scale;
    ty /= scree_scale;
    
    PolygonShape brick_shape = new PolygonShape();
    brick_shape.setAsBox(dimx*0.5f, dimy*0.5f);
    
    PolygonShape brick_shape2 = new PolygonShape();
    brick_shape2.setAsBox(dimx*0.25f, dimy*0.5f);
    
    FixtureDef fixture_def = new FixtureDef();
    fixture_def.shape = brick_shape;
    fixture_def.density = 30;
    fixture_def.friction = 0.50f;
    fixture_def.restitution = 0.5f;
    
    BodyDef body_def = new BodyDef();
    body_def.type = BodyType.DYNAMIC;
    body_def.angle = 0.0f;
    body_def.allowSleep = true;
    
    
    int scol = color(0);
    int fcol = color(224,128,64);

    colorMode(HSB, 360,100,100);
    
    randomSeed(1);
    
    float ox = 0;
    float oy = dimy/2;
    for(int y = 0; y < numy; y++){
      
      float off = 0.5f;
      
      for(int x = 0; x < numx; x++){
        boolean odd_row = (y & 1) == 1;

        ox = -numx * dimx * 0.5f;
        ox += odd_row ? dimx * off : 0;
        
        fixture_def.shape = brick_shape;
        if(!odd_row && x == 0){
          fixture_def.shape = brick_shape2;
          ox += dimx * 0.25;
        }
        else if(odd_row && x == (numx-1)){
          fixture_def.shape = brick_shape2;
          ox -= dimx * 0.25;
        }
        

        
        body_def.position.x = tx + ox + x * (dimx);
        body_def.position.y = ty + oy + y * (dimy);
   
        Body brick = world.createBody(body_def);
        brick.createFixture(fixture_def);
        
        float hsb_h = 20 + random(-3, 3);
        float hsb_s = random(70,60);
        float hsb_b = random(70,100);
        fcol = color(hsb_h, hsb_s, hsb_b);
        world.bodies.add(brick, true, fcol, true, scol, 0.5f);
      }
    }
    
    colorMode(RGB, 255);
   
  }
  

  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_BrickWall.class.getName() });
  }
  
}