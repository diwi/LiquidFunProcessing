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


import org.jbox2d.collision.shapes.ChainShape;
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


public class box2d_TumblerMod extends PApplet {

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


    canvas.background(32);
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

      Body bwheel = world.createBody(bd);
      bodyrenderer.createShape(bwheel);
      
      ChainShape shape = new ChainShape();
      int num_verts = 128;
      float rad = 10;
      float angle_step = (float) (Math.PI * 2 / num_verts);
      
      float dent_size = 1.5f;
      int num_dents = 5;
      
      // wheel
      Vec2[] vertices = new Vec2[num_verts];
      for(int i = 0; i < num_verts; i++){
        float inorm = (float) ((Math.PI * 2) * i / (float) num_verts);
        float rad_off = (float) Math.cos(inorm * num_dents) * dent_size;
        float x = (rad + rad_off) * (float) Math.cos(angle_step * i);
        float y = (rad + rad_off) * (float) Math.sin(angle_step * i);
        vertices[i] = new Vec2(x, y); 
      }
      
      shape.createLoop(vertices, num_verts);
      Fixture wheel = bwheel.createFixture(shape, 15.0f);
      
      bodyrenderer.createShape(wheel);
      bodyrenderer.styleShape(wheel, true, color(240), false, color(255), 1f);
      
      // obstacles inside wheel
      CircleShape sobstacle = new CircleShape(); 
      sobstacle.m_radius = 4f/num_dents;
      angle_step = (float) (Math.PI * 2 / num_dents);
      for(int i = 0; i < num_dents; i++){
        float angle = angle_step * 0.5f + angle_step * i;
        float radius = rad - 4f;
        float x = radius * (float) Math.cos(angle);
        float y = radius * (float) Math.sin(angle);
        sobstacle.m_p.set(x, y);
        Fixture fobstacle = bwheel.createFixture(sobstacle, 150.0f);
        
        bodyrenderer.createShape(fobstacle);
        bodyrenderer.styleShape(fobstacle, true, color(32), false, color(0), 1f);
      }

      
      // motor
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = groundbody;
      jd.bodyB = bwheel;
      jd.localAnchorA.set(0.0f, 0.0f);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 1000000f;
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
    PApplet.main(new String[] { box2d_TumblerMod.class.getName() });
  }
  
}