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


public class box2d_DominoTower extends PApplet {

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
    transform.setScreen(width, height, 20, width/2, height-10);

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
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/DominoTower.java
  
  
  final float dwidth = .20f;
  final float dheight = 1.0f;
  float ddensity;// = 10f;
  final float dfriction = 0.1f;
  int baseCount = 25;

  public void initScene() {
    
    { // Floor
      PolygonShape sd = new PolygonShape();
      sd.setAsBox(50.0f, 10.0f);

      BodyDef bd = new BodyDef();
      bd.position = new Vec2(0.0f, -10.0f);
      Body floor = world.createBody(bd);
      floor.createFixture(sd, 0f);
      
      bodyrenderer.createShape(floor);
      bodyrenderer.styleShape(floor, true, color(0), false, color(0), 1f);
    }

    { // Bullets
      
      Body bullet1;
      Body bullet2;
      
      ddensity = 10f;
   
      PolygonShape sd = new PolygonShape();
      sd.setAsBox(.7f, .7f);

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.bullet = true;

      FixtureDef fd = new FixtureDef();
      fd.shape = sd;
      fd.friction = 0f;
      fd.restitution = 0.85f;
      
      fd.density = 25f;
      bd.position = new Vec2(30f, 50f);
      bullet1 = world.createBody(bd);
      bullet1.createFixture(fd);
      bullet1.setLinearVelocity(new Vec2(-25f, -25f));
      bullet1.setAngularVelocity(6.7f);

      fd.density = 25f;
      bd.position = new Vec2(-30, 25f);
      bullet2 = world.createBody(bd);
      bullet2.createFixture(fd);
      bullet2.setLinearVelocity(new Vec2(35f, -10f));
      bullet2.setAngularVelocity(-8.3f);
      
      bodyrenderer.createShape(bullet1);
      bodyrenderer.styleShape(bullet1, true, color(255), false, color(0), 1f);
      
      bodyrenderer.createShape(bullet2);
      bodyrenderer.styleShape(bullet2, true, color(255), false, color(0), 1f);
    }

    {
      float currX;
      // Make base
      for (int i = 0; i < baseCount; ++i) {
        currX = i * 1.5f * dheight - (1.5f * dheight * baseCount / 2f);
        makeDomino(currX, dheight / 2.0f, false, world);
        makeDomino(currX, dheight + dwidth / 2.0f, true, world);
      }
      currX = baseCount * 1.5f * dheight - (1.5f * dheight * baseCount / 2f);
      // Make 'I's
      for (int j = 1; j < baseCount; ++j) {
        if (j > 3)  ddensity *= .8f;
        float currY = dheight * .5f + (dheight + 2f * dwidth) * .99f * j; // y at center of 'I'structure

        for (int i = 0; i < baseCount - j; ++i) {
          currX = i * 1.5f * dheight - (1.5f * dheight * (baseCount - j) / 2f);// +parent.random(-.05f,.05f);
          ddensity *= 2.5f;
          if (i == 0) {
            makeDomino(currX - (1.25f * dheight) + .5f * dwidth, currY - dwidth, false, world);
          }
          if (i == baseCount - j - 1) {
            // if (j != 1) //djm: why is this here? it makes it off balance
            makeDomino(currX + (1.25f * dheight) - .5f * dwidth, currY - dwidth, false, world);
          }
          ddensity /= 2.5f;
          makeDomino(currX, currY, false, world);
          makeDomino(currX, currY + .5f * (dwidth + dheight), true, world);
          makeDomino(currX, currY - .5f * (dwidth + dheight), true, world);
        }
      }
    }
   

    // creates shapes for all rigid bodies in the world.
    bodyrenderer.createShape();

  }
  
  int count = 0;
  public void makeDomino(float x, float y, boolean horizontal, World world) {

    PolygonShape sd = new PolygonShape();
    sd.setAsBox(.5f * dwidth, .5f * dheight);
    FixtureDef fd = new FixtureDef();
    fd.shape = sd;
    fd.density = ddensity;
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    fd.friction = dfriction;
    fd.restitution = 0.65f;
    bd.position = new Vec2(x, y);
    bd.angle = horizontal ? (float) (Math.PI / 2.0) : 0f;
    Body myBody = world.createBody(bd);
    myBody.createFixture(fd);
    
    
    
    colorMode(HSB, 360, 100, 100);
    int ch = (count/5) % 360;
    int cs = 100;
    int cb = 100;
    if(horizontal) cb = 50;
    int fcol = color(ch, cs, cb);
    int scol = color(ch, cs*0.5f, cb*0.5f, 128);
    bodyrenderer.createShape(myBody);
    bodyrenderer.styleShape(myBody, true, fcol, true, scol, 1f);
    
    colorMode(RGB, 255);
    
    count++;
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_DominoTower.class.getName() });
  }
  
}