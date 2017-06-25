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
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;
import com.thomasdiewald.liquidfun.java.render.DwJoint;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Collisions extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;
  DwBodyGroup bodies;
  
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
    if(bodies != null) bodies.release(); bodies = null;
  }
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 20);
    world.transform.setScreen(width, height, 20, width/2, height);
    world.setGravity(new Vec2(0, -5));
    
    
    world.mouse_shoot_bullet.density_mult = 0.001f;
    world.mouse_shoot_bullet.cirlce_shape.m_radius = 1;
    world.mouse_shoot_bullet.velocity_mult = 0.6f;
    
    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  
  public void draw(){
    
    bodies.addBullet(true, color(200, 0, 0), true, color(0), 1f);
    
    if(UPDATE_PHYSICS){
      if(frameCount % 10 == 0){
        addBodies();
      }
      world.update();
      
      doSomethingWithCollisionContactsAndDistanceJoints();
      
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
      bodies.display(canvas);
    }
    canvas.popMatrix();
    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  

  
  
  
  
  
  public void doSomethingWithCollisionContactsAndDistanceJoints(){
    
    DistanceJointDef djd = new DistanceJointDef();
    djd.dampingRatio = 0.5f;
    djd.frequencyHz = 20.0f;
    djd.collideConnected = false; // default anyways
 
    // iterate through all contacts since the previous world-update-step
    for(Contact contact = world.getContactList(); contact != null; contact = contact.m_next){
      
      // both bodies of the contact
      Body bodyA = contact.m_fixtureA.getBody();
      Body bodyB = contact.m_fixtureB.getBody();
      
      // ignore contaxt with static bodies
      if(bodyA.m_type == BodyType.STATIC || bodyB.m_type == BodyType.STATIC ){
        continue;
      }
      
      // body world positions
      Vec2 posA = bodyA.getTransform().p;
      Vec2 posB = bodyB.getTransform().p;
      
      // create joint bodyA <-> bodyB
      djd.initialize(bodyA, bodyB, posA, posB);
      Joint joint = world.createJoint(djd);
      
      // add joint shape and a style
      DwJoint dwjoint = bodies.add(joint, false, color(0), true, color(255, 160), 1.0f);
      

//      // replace the line shape with a rectangle shape
//      // to apply a color transition for the joint.
//      // unfortunately this doesnt work for a line, only for a Polygon, like a rectangle.
//      DwFixture fA = DwWorld.getShape(contact.m_fixtureA);
//      DwFixture fB = DwWorld.getShape(contact.m_fixtureB);
//      
//      float h = 1f / world.transform.screen_scale;
//      
//      PShape shape_rect = createShape();
//      shape_rect.beginShape(QUADS);
//      shape_rect.noStroke();
//      shape_rect.fill(fA.shape.getFill(0));
//      shape_rect.vertex(0,-h);
//      shape_rect.vertex(0,+h);
//      shape_rect.fill(fB.shape.getFill(0));
//      shape_rect.vertex(1,+h);
//      shape_rect.vertex(1,-h);
//      shape_rect.endShape();
//
//      dwjoint.replaceShape(shape_rect);

    }
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
  
  int MAX_NUM = 200;
  int m_count = 0;
  
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
      fixture_def.density = 1;
      fixture_def.friction = 0.10f;
      fixture_def.restitution = 0.70f;
      
      BodyDef body_def = new BodyDef();
      body_def.type = BodyType.DYNAMIC;
      body_def.angle = 0.0f;
      body_def.position.x = -1/screen_scale;
      body_def.position.y = b2d_screen_h - 10;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      circle_body.createFixture(fixture_def);
      
      bodies.add(circle_body, true, color(64, 125, 255), true, color(0), 1f);
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


      // LEFT
      x = -b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h;
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      // RIGHT
      x = +b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h;
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);

      bodies.add(ground, true, color(0), !true, color(0), 1f);
    }
    
    addBodies();
  }
  
  
  public void addBodies(){
    if (world.getBodyCount() < MAX_NUM) {

      float b2d_screen_w = world.transform.box2d_dimx;
      float b2d_screen_h = world.transform.box2d_dimy;
      
      float x = random(-0.4f, 0.4f) * b2d_screen_w;
      float y = random(-2f, 2) + b2d_screen_h * 1.2f;
      
      float w = random(0.5f, 1.2f);
      float h = random(0.5f, 1.2f);
      
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(x, y);
      bd.linearVelocity = new Vec2(random(-1f, +1) * 5, 0);
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
      fixture.m_restitution = 0.1f;
      
      colorMode(HSB, 360, 100, 100);

      float r = (360 * m_count /(float)MAX_NUM) % 360;
      float g = 100;
      float b = 100;
      
      bodies.add(body, true, color(r,g,b), true, color(r, g, b *0.5f), 1f);
      colorMode(RGB, 255, 255, 255);
      
      m_count++;
    }
  }

  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Collisions.class.getName() });
  }
  
}