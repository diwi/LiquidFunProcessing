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



package box2d_Breakable;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.render.DwFixture;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Breakable extends PApplet {
  
  //
  // This examples demonstrates how bodies (compounds) can break.
  // A Body is built of two fixtures. 
  // A ContactListener is implemented to find the max collision impulse at which
  // the body will break.
  // In case the body should break, one fixture is destroyed and recreated as a
  // new body. The previous velocity (angular, linear) are applied to both remains. 
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
      updateBreak();
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
  
  Body m_body1;
  Vec2 m_velocity = new Vec2();
  float m_angularVelocity;
  PolygonShape m_shape1;
  PolygonShape m_shape2;
  Fixture m_piece1;
  Fixture m_piece2;

  boolean m_broke;
  boolean m_break;
  
  
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Breakable.java
  public void initScene() {
    // Ground body
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
      ground.createFixture(shape, 0.0f);
      
      world.bodies.add(ground, true, color(255), true, color(0), 1);
    }

    // Breakable dynamic body
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(0.0f, 20.0f);
      bd.linearVelocity.set(0, -10);
      bd.angle = 0.25f * MathUtils.PI;
      m_body1 = world.createBody(bd);

      m_shape1 = new PolygonShape();
      m_shape1.setAsBox(0.5f, 0.5f, new Vec2(-0.5f, 0.0f), 0.0f);
      m_piece1 = m_body1.createFixture(m_shape1, 1.0f);

      m_shape2 = new PolygonShape();
      m_shape2.setAsBox(0.5f, 0.5f, new Vec2(0.5f, 0.0f), 0.0f);
      m_piece2 = m_body1.createFixture(m_shape2, 1.0f);
      
      world.bodies.add(m_piece1, true, color(255,0,0), true, color(0), 1);
      world.bodies.add(m_piece2, true, color(0,255,0), true, color(0), 1);
    }

    m_break = false;
    m_broke = false;

    world.setContactListener(new MyContactListener());
  }
  
  
  
  
  class MyContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
      if (m_broke) {
        // The body already broke.
        return;
      }

      // Should the body break?
      int count = contact.getManifold().pointCount;

      float maxImpulse = 0.0f;
      for (int i = 0; i < count; ++i) {
        maxImpulse = MathUtils.max(maxImpulse, impulse.normalImpulses[i]);
      }
      
      if (maxImpulse > 30.0f) {
        // Flag the body for breaking.
        m_break = true;
      }
    }
    
  }
  

  
  
  
  void applyBreak() {
    // Create two bodies from one.
    Body body1 = m_piece1.getBody();
    Vec2 center = body1.getWorldCenter();

    // before completely destroying the shape, copy its style (fill, stroke)
    DwFixture dwfixture = DwWorld.getShape(m_piece2);
    int col_fill = dwfixture.shape.getFill(0);
    int col_stroke = dwfixture.shape.getStroke(0);
    
//    body1.destroyFixture(m_piece2);
    world.destroyFixture(m_piece2); // also destroys the PShape
    
    m_piece2 = null;

    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position = body1.getPosition();
    bd.angle = body1.getAngle();

    Body body2 = world.createBody(bd);
    m_piece2 = body2.createFixture(m_shape2, 1.0f);
    
    // new fixture, with the same style
    world.bodies.add(m_piece2, true, col_fill, true, col_stroke, 1);

    // Compute consistent velocities for new bodies based on cached velocity.
    Vec2 center1 = body1.getWorldCenter();
    Vec2 center2 = body2.getWorldCenter();

    Vec2 velocity1 = m_velocity.add(Vec2.cross(m_angularVelocity, center1.sub(center)));
    Vec2 velocity2 = m_velocity.add(Vec2.cross(m_angularVelocity, center2.sub(center)));

    body1.setAngularVelocity(m_angularVelocity);
    body1.setLinearVelocity(velocity1);

    body2.setAngularVelocity(m_angularVelocity);
    body2.setLinearVelocity(velocity2);
  }


  public void updateBreak() {
    if (m_break) {
      applyBreak();
      m_broke = true;
      m_break = false;
    }

    // Cache velocities to improve movement on breakage.
    if (m_broke == false) {
      m_velocity.set(m_body1.getLinearVelocity());
      m_angularVelocity = m_body1.getAngularVelocity();
    }
  }
  

  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Breakable.class.getName() });
  }
  
}