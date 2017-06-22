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
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.render.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.render.DwJointRenderP5;

import processing.core.PApplet;
import processing.opengl.PGraphics2D;


public class box2d_PistonStressTest extends PApplet {

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
  DwJointRenderP5 jointrenderer;
  
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
    if(jointrenderer != null){
      jointrenderer.release();
      jointrenderer = null;
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
    transform.setScreen(width, height, 18, width/2, height);

    // Renderer
    debugdraw = new DwDebugDraw(this, world, transform);
    bodyrenderer = new DwBodyRenderP5(this, world, transform);
    jointrenderer = new DwJointRenderP5(this, world, transform);
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
      jointrenderer.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.pushMatrix();
    canvas.applyMatrix(transform.mat_box2screen);
    canvas.background(255);
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
      jointrenderer.display(canvas);
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
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/PistonTest.java
  public void initScene() {
    
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(5.0f, 100.0f);
      bd = new BodyDef();
      bd.position.set(0, 0);
      bd.type = BodyType.STATIC;
      FixtureDef sides = new FixtureDef();
      sides.shape = shape;
      sides.density = 0;
      sides.friction = 0;
      sides.restitution = .8f;
      sides.filter.categoryBits = 4;
      sides.filter.maskBits = 2;

//      bd.position.set(-10.01f, 50.0f);
//      Body bleft = world.createBody(bd);
//      bleft.createFixture(sides);
//      bd.position.set(10.01f, 50.0f);
//      Body bright = world.createBody(bd);
//      bright.createFixture(sides);

      Body bsides = world.createBody(bd);
      shape.setAsBox(5.0f, 100.0f, new Vec2(-10.15f, 50.0f), 0);
      bsides.createFixture(sides);
      shape.setAsBox(5.0f, 100.0f, new Vec2(+10.15f, 50.0f), 0);
      bsides.createFixture(sides);
      
      bodyrenderer.createShape(bsides);
      bodyrenderer.styleShape(bsides, true, color(32), true, color(0), 1f);

    }

    // turney
    {
      CircleShape cd;
      FixtureDef fd = new FixtureDef();
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      int numPieces = 5;
      float radius = 4f;
      bd.position = new Vec2(0.0f, 25.0f);
      Body body = world.createBody(bd);
      for (int i = 0; i < numPieces; i++) {
        cd = new CircleShape();
        cd.m_radius = .5f;
        fd.shape = cd;
        fd.density = 25;
        fd.friction = .1f;
        fd.restitution = .9f;
        float xPos = radius * (float) Math.cos(2f * Math.PI * (i / (float) (numPieces)));
        float yPos = radius * (float) Math.sin(2f * Math.PI * (i / (float) (numPieces)));
        cd.m_p.set(xPos, yPos);

        body.createFixture(fd);
      }
      bodyrenderer.createShape(body);
      bodyrenderer.styleShape(body, true, color(255,64,0), true, color(0), 1f);
      

      RevoluteJointDef rjd = new RevoluteJointDef();
      rjd.initialize(body, ground, body.getPosition());
      rjd.motorSpeed = MathUtils.PI * 0.8f;
      rjd.maxMotorTorque = 1000000.0f;
      rjd.enableMotor = true;
      world.createJoint(rjd);
    }


    {
      Body prevBody = ground;

      // Define crank.
      {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 2.0f);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0.0f, 7.0f);
        Body body = world.createBody(bd);
        body.createFixture(shape, 2.0f);

        RevoluteJointDef rjd = new RevoluteJointDef();
        rjd.initialize(prevBody, body, new Vec2(0.0f, 5.0f));
        rjd.motorSpeed = 1.0f * MathUtils.PI;
        rjd.maxMotorTorque = 20000;
        rjd.enableMotor = true;
        world.createJoint(rjd);

        prevBody = body;
        
        bodyrenderer.createShape(prevBody);
        bodyrenderer.styleShape(prevBody, true, color(200,32,0), true, color(0), 1f);
      }

      // Define follower.
      {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 4.0f);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0.0f, 13.0f);
        Body body = world.createBody(bd);
        body.createFixture(shape, 2.0f);

        RevoluteJointDef rjd = new RevoluteJointDef();
        rjd.initialize(prevBody, body, new Vec2(0.0f, 9.0f));
        rjd.enableMotor = false;
        world.createJoint(rjd);

        prevBody = body;
        bodyrenderer.createShape(prevBody);
        bodyrenderer.styleShape(prevBody, true, color(200,32,0), true, color(0), 1f);
      }

      // Define piston
      {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(5f, 2f);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0.0f, 17.0f);
        Body body = world.createBody(bd);
        FixtureDef piston = new FixtureDef();
        piston.shape = shape;
        piston.density = 2;
        piston.filter.categoryBits = 1;
        piston.filter.maskBits = 2;
        body.createFixture(piston);
        body.setBullet(false);
        
        bodyrenderer.createShape(body);
        bodyrenderer.styleShape(body, true, color(200,32,0), true, color(0), 1f);
        
        
        RevoluteJointDef rjd = new RevoluteJointDef();
        rjd.initialize(prevBody, body, new Vec2(0.0f, 17.0f));
        world.createJoint(rjd);

        PrismaticJointDef pjd = new PrismaticJointDef();
        pjd.initialize(ground, body, new Vec2(0.0f, 17.0f), new Vec2(0.0f, 1.0f));

        pjd.maxMotorForce = 1000.0f;
        pjd.enableMotor = true;

        world.createJoint(pjd);
      }

      // Create a payload
      {
        PolygonShape sd = new PolygonShape();
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        FixtureDef fixture = new FixtureDef();

        for (int i = 0; i < 100; ++i) {
          sd.setAsBox(0.4f, 0.3f);
          bd.position.set(-1.0f, 23.0f + i);
          bd.bullet = false;
          Body body = world.createBody(bd);
          fixture.shape = sd;
          fixture.density = .1f;
          fixture.filter.categoryBits = 2;
          fixture.filter.maskBits = 1 | 4 | 2;
          body.createFixture(fixture);
          
          bodyrenderer.createShape(body);
          bodyrenderer.styleShape(body, true, color(32,128,255), true, color(0), 1f);
        }

        
        CircleShape cd = new CircleShape();
        cd.m_radius = 0.36f;
        for (int i = 0; i < 100; ++i) {
          bd.position.set(1.0f, 23.0f + i);
          bd.bullet = false;
          fixture.shape = cd;
          fixture.density = 2f;
          fixture.filter.categoryBits = 2;
          fixture.filter.maskBits = 1 | 4 | 2;
          Body body = world.createBody(bd);
          body.createFixture(fixture);
          
          bodyrenderer.createShape(body);
          bodyrenderer.styleShape(body, true, color(32,255,128), true, color(0), 1f);
        }
        
        float angle = 0.0f;
        float delta = MathUtils.PI / 3.0f;
        Vec2 vertices[] = new Vec2[6];
        for (int i = 0; i < 6; ++i) {
          vertices[i] = new Vec2(0.3f * MathUtils.cos(angle), 0.3f * MathUtils.sin(angle));
          angle += delta;
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices, 6);

        for (int i = 0; i < 100; ++i) {
          bd.position.set(0f, 23.0f + i);
          bd.type = BodyType.DYNAMIC;
          bd.fixedRotation = true;
          bd.bullet = false;
          fixture.shape = shape;
          fixture.density = 1f;
          fixture.filter.categoryBits = 2;
          fixture.filter.maskBits = 1 | 4 | 2;
          Body body = world.createBody(bd);
          body.createFixture(fixture);
          
          bodyrenderer.createShape(body);
          bodyrenderer.styleShape(body, true, color(255,255,32), true, color(0), 1f);
        }
      }
    }

    
    bodyrenderer.createShape();
//    jointrenderer.createShape();
  
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_PistonStressTest.class.getName() });
  }
  
}