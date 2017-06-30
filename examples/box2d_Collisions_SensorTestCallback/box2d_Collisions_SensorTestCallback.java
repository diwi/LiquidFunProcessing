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



package box2d_Collisions_SensorTestCallback;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.render.DwBody;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Collisions_SensorTestCallback extends PApplet {
  
  
  //
  // Collision Example.
  // A static circle body acts as a sensor and acts as a gravity center. 
  // When other bodies get in contact with the sensor they are attracted to
  // the sensors center.
  //
  // A contact test is done by implementing a ContactListener.
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
    
    world = new DwWorld(this, 25);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){

    if(UPDATE_PHYSICS){
      updateSensor();
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
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  


  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  Fixture m_sensor;

  // mod of the original vesion
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/SensorTest.java
  public void initScene() {

    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      { // floor
        EdgeShape shape = new EdgeShape();
        shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
        ground.createFixture(shape, 0.0f);
      }

      { // sensor
        CircleShape shape = new CircleShape();
        shape.m_radius = 5.0f;
        shape.m_p.set(0.0f, 10.0f);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = true;
        m_sensor = ground.createFixture(fd);
      }
      
      world.bodies.add(ground, true, color(255), true, color(0), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      
      CircleShape shape = new CircleShape();
      shape.m_radius = 1.0f;

      for (int i = 0; i < 10; ++i) {
        bd.position.set(-10.0f + 3.0f * i, 20.0f);
        Body body = world.createBody(bd);
        body.createFixture(shape, 1.0f);
        
        DwBody dwbody = world.bodies.add(body, true, color(0,255,0), true, color(0), 1f);
        dwbody.m_userData = (Boolean) false;
      }
    }
    
    world.setContactListener(new MyContactListener());
  }
  
  
  class MyContactListener implements ContactListener{
    
    void enableSensorAttraction(Fixture fixture, boolean enable){
      DwBody dwbody = DwWorld.getShape(fixture.getBody());
      if(dwbody != null){
        if(dwbody.m_userData instanceof Boolean){
          dwbody.m_userData = enable;
        }
      }
    }
    
    @Override
    public void beginContact(Contact contact) {
      Fixture fixtureA = contact.getFixtureA();
      Fixture fixtureB = contact.getFixtureB();
      if (fixtureA == m_sensor) enableSensorAttraction(fixtureB, true);
      if (fixtureB == m_sensor) enableSensorAttraction(fixtureA, true);
    }
  
    @Override
    public void endContact(Contact contact) {
      Fixture fixtureA = contact.getFixtureA();
      Fixture fixtureB = contact.getFixtureB();
      if (fixtureA == m_sensor) enableSensorAttraction(fixtureB, false);
      if (fixtureB == m_sensor) enableSensorAttraction(fixtureA, false);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
    
  }


  
  public void updateSensor() {
    
    Body bsensor = m_sensor.getBody();
    CircleShape circle = (CircleShape) m_sensor.getShape();
    Vec2 sensor_pos = bsensor.getWorldPoint(circle.m_p);
    
    // Traverse the bodies. Apply a force on shapes that overlap the sensor.
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      if(body == bsensor){
        continue;
      }
      
      boolean is_touching = false;
      DwBody dwbody = DwWorld.getShape(body);
      if(dwbody != null){
        if(dwbody.m_userData instanceof Boolean){
          is_touching = (Boolean) dwbody.m_userData;
        }
      }
      
      
      if (is_touching) {
        Vec2 body_pos = body.getPosition();
  
        Vec2 dist = sensor_pos.sub(body_pos);
        if (dist.lengthSquared() > Settings.EPSILON * Settings.EPSILON) {
          dist.normalize();
          Vec2 force = dist.mulLocal(200f);
          body.applyForce(force, body_pos);
        }
      }
    }
  }
  
  
  

  

  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Collisions_SensorTestCallback.class.getName() });
  }
  
}