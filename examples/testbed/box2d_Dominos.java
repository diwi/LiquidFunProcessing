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


import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.render.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Dominos extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;
  
  World world;
  DwViewportTransform transform;
  
  DwDebugDraw debugdraw;
  DwBodyRenderP5 bodyrenderer;
  DwMouseDragBodies mouse_drag_bodies;
  
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
    if(bodyrenderer != null){
      bodyrenderer.release();
      bodyrenderer = null;
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
    transform.setScreen(width, height, 25, width/2, height-10);

    // Renderer
    debugdraw = new DwDebugDraw(this, world, transform);
    bodyrenderer = new DwBodyRenderP5(this, world, transform);

    // mouse interaction
    mouse_drag_bodies = new DwMouseDragBodies(world, transform);

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
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.pushMatrix();
    canvas.applyMatrix(transform.mat_box2screen);
    canvas.background(32);
    canvas.fill(200);
    canvas.tint(255);
    canvas.stroke(0);
    canvas.strokeWeight(1f/transform.screen_scale);

//    DwDebugDraw.displayBodies   (canvas, world);
//    DwDebugDraw.displayParticles(canvas, world);
//    DwDebugDraw.displayJoints   (canvas, world);

    if(USE_DEBUG_DRAW){
      debugdraw.display(canvas);
    } else {
      bodyrenderer.display(canvas);
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

  public void mousePressed() {
    if(mouseButton == LEFT){
      mouse_drag_bodies.press(mouseX, mouseY);
    }
  }

  public void mouseDrawAction(){
    mouse_drag_bodies   .update(mouseX, mouseY);
  }
  
  public void mouseDragged() {
  }
  
  public void mouseReleased() {
    mouse_drag_bodies   .release(mouseX, mouseY);
  }
  
  public void keyReleased(){
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  
  public void keyPressed() {
  }


  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/DominoTest.java
  public void initScene() {
    
    
    { // Floor
      FixtureDef fd = new FixtureDef();
      PolygonShape sd = new PolygonShape();
      sd.setAsBox(50.0f, 10.0f);
      fd.shape = sd;

      BodyDef bd = new BodyDef();
      bd.position = new Vec2(0.0f, -10.0f);
      Body body = world.createBody(bd);
      body.createFixture(fd);
      
      PShape shp = bodyrenderer.createShape(body);
      shp.setFill(color(0));

    }

    { // Platforms
      for (int i = 0; i < 4; i++) {
        FixtureDef fd = new FixtureDef();
        PolygonShape sd = new PolygonShape();
        sd.setAsBox(15.0f, 0.125f);
        fd.shape = sd;

        BodyDef bd = new BodyDef();
        bd.position = new Vec2(0.0f, 5f + 5f * i);
        Body body = world.createBody(bd);
        body.createFixture(fd);
        
        PShape shp = bodyrenderer.createShape(body);
        shp.setFill(color(0));
      }
    }

    { // Dominos
      FixtureDef fd = new FixtureDef();
      PolygonShape sd = new PolygonShape();
      sd.setAsBox(0.125f, 2f);
      fd.shape = sd;
      fd.density = 25.0f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      float friction = .5f;
      int num_col = 4;
      int num_row = 25;

      colorMode(HSB, 360, 100, 100);
      
      for (int i = 0; i < num_col; ++i) {
        for (int j = 0; j < num_row; j++) {
          fd.friction = friction;
          bd.position = new Vec2(-14.75f + j * (29.5f / (num_row - 1)), 7.3f + 5f * i);
          if (i == 2 && j == 0) {
            bd.angle = -0.1f;
            bd.position.x += .1f;
          } else if (i == 3 && j == num_row - 1) {
            bd.angle = .1f;
            bd.position.x -= .1f;
          } else
            bd.angle = 0f;
          Body bdomino = world.createBody(bd);
          bdomino.createFixture(fd);
        
          // create shape, and define individual fill
          int hue = (int) ((i * num_row + j) / (float)(num_col * num_row) * 360);
          int fcol = color(hue, 100, 100);
          int scol = color(hue, 50, 50, 128);
          bodyrenderer.createShape(bdomino);
          bodyrenderer.styleShape (bdomino, true, fcol, true, scol, 1f);
          
        }
      }
      
      colorMode(RGB, 255);
    }
   

    // creates shapes for all rigid bodies in the world.
    bodyrenderer.createShape();

  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Dominos.class.getName() });
  }
  
}