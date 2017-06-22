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


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.render.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Tumbler extends PApplet {

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
    transform.setScreen(width, height, 22, width/2, height/2);

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
      if(frameCount % 4 == 0){
        addBodies();
      }
      mouseDrawAction();
      world.step(1f/60, 8, 4);
      bodyrenderer.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;


    canvas.background(255);
    canvas.pushMatrix();
    canvas.applyMatrix(transform.mat_box2screen);
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
    mouse_drag_bodies.update(mouseX, mouseY);
  }
  
  public void mouseDragged() {
  }
  
  public void mouseReleased() {
    mouse_drag_bodies.release(mouseX, mouseY);
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
  int MAX_NUM = 800;
  RevoluteJoint m_joint;
  int m_count;
  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Tumbler.java
  public void initScene() {
    

    {
      BodyDef bd = new BodyDef();
      Body groundbody = world.createBody(bd);
      
      bd.type = BodyType.DYNAMIC;
      bd.allowSleep = false;
      bd.position.set(0.0f, 0.0f);
      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(1, 11.0f, new Vec2(10.0f, 0.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(1, 11.0f, new Vec2(-10.0f, 0.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(11.0f, 1, new Vec2(0.0f, 10.0f), 0.0f);
      body.createFixture(shape, 15.0f);
      shape.setAsBox(11.0f, 1, new Vec2(0.0f, -10.0f), 0.0f);
      body.createFixture(shape, 15.0f);

      
      CircleShape obstacle = new CircleShape(); 
      obstacle.m_radius = 1; 
      obstacle.m_p.set(-2, -6.5f);
      body.createFixture(obstacle, 115.0f);
      obstacle.m_p.set(2, +6.5f);
      body.createFixture(obstacle, 115.0f);
//      obstacle.m_p.set(-7.5f, 0);
//      body.createFixture(obstacle, 15.0f);
//      obstacle.m_p.set(+7.5f, 0);
//      body.createFixture(obstacle, 15.0f);

      bodyrenderer.createShape(body);
      bodyrenderer.styleShape(body, true, color(64), true, color(0), 1f);

      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = groundbody;
      jd.bodyB = body;
      jd.localAnchorA.set(0.0f, 0.0f);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    m_count = 0;
   

    // creates shapes for all rigid bodies in the world.
    bodyrenderer.createShape();

  }
  
  
  public void addBodies(){
    if (m_count < MAX_NUM) {
      
      float x = random(-0.7f, 0.7f);
      float y = random(-0.7f, 0.7f);
      
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(x, y);
      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.18f, 0.18f);
      Fixture fixture = body.createFixture(shape, 0.01f);
      fixture.m_friction = 0.1f;
      fixture.m_restitution = 0.5f;
      
      ++m_count;
      
      bodyrenderer.createShape(body);
      
      colorMode(HSB, 360, 100, 100);
      float r = (360 * m_count /(float)MAX_NUM) % 360;
      float g = 100;
      float b = 100;
      bodyrenderer.styleShape(body, true, color(r,g,b), true, color(r, g, b *0.5f), 1f);
      colorMode(RGB, 255, 255, 255);
    }
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Tumbler.class.getName() });
  }
  
}