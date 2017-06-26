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
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_TheoJansenWalker extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;

  PFont font;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    font = createFont("SourceCodePro-Regular.ttf", 12);
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
    
    // set camera
    Vec2 pos = m_chassis.m_xf.p;
    world.transform.setCamera(pos.x/2, pos.y/2 + 10);
    
    if(UPDATE_PHYSICS){
      world.update();
    }

    
    PGraphics2D canvas = (PGraphics2D) this.g;
    canvas.background(255);
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
    
    
    int tx = 16;
    int ty = 16;
    int gy = 14;
    textFont(font);
    fill(0);
    text("'a' move backwards", tx, ty+=gy);
    text("'s' stop"          , tx, ty+=gy);
    text("'d' move forwards" , tx, ty+=gy);
    text("'m' toggle motor"  , tx, ty+=gy);
    text("'r' reset"         , tx, ty+=gy);
    text("'t' pause"         , tx, ty+=gy);
    text("'f' debugdraw"     , tx, ty+=gy);
    
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
  
  public void keyPressed() {
    switch (key) {
      case 'a': m_motorJoint.setMotorSpeed(-m_motorSpeed); break;
      case 's': m_motorJoint.setMotorSpeed(0.0f); break;
      case 'd': m_motorJoint.setMotorSpeed(m_motorSpeed); break;
      case 'm': m_motorJoint.enableMotor(!m_motorJoint.isMotorEnabled()); break;
    }
  }


  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  
  
  Vec2 m_offset = new Vec2();
  Body m_chassis;
  Body m_wheel;
  RevoluteJoint m_motorJoint;
  boolean m_motorOn;
  float m_motorSpeed;
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/TheoJansen.java
  public void initScene() {
    
    m_offset.set(0.0f, 8.0f);
    m_motorSpeed = 2.0f;
    m_motorOn = true;
    Vec2 pivot = new Vec2(0.0f, 0.8f);

    // Ground
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-50.0f, 0.0f), new Vec2(50.0f, 0.0f));
      ground.createFixture(shape, 0.0f);

      shape.set(new Vec2(-50.0f, 0.0f), new Vec2(-50.0f, 10.0f));
      ground.createFixture(shape, 0.0f);

      shape.set(new Vec2(50.0f, 0.0f), new Vec2(50.0f, 10.0f));
      ground.createFixture(shape, 0.0f);

      world.bodies.add(ground, true, color(0), true, color(0), 2f);
    }

    // Balls
    for (int i = 0; i < 40; ++i) {
      CircleShape shape = new CircleShape();
      shape.m_radius = 0.25f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-40.0f + 2.0f * i, 0.5f);

      Body body = world.createBody(bd);
      body.createFixture(shape, 1.0f);
      
      world.bodies.add(body, true, color(0), true, color(0), 1f);
    }

    // Chassis
    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(2.5f, 1.0f);

      FixtureDef sd = new FixtureDef();
      sd.density = 1.0f;
      sd.shape = shape;
      sd.filter.groupIndex = -1;
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(pivot).addLocal(m_offset);
      m_chassis = world.createBody(bd);
      m_chassis.createFixture(sd);
      
      world.bodies.add(m_chassis, true, color(0, 255), true, color(0), 1f);
    }

    {
      CircleShape shape = new CircleShape();
      shape.m_radius = 1.6f;

      FixtureDef sd = new FixtureDef();
      sd.density = 1.0f;
      sd.shape = shape;
      sd.filter.groupIndex = -1;
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(pivot).addLocal(m_offset);
      m_wheel = world.createBody(bd);
      m_wheel.createFixture(sd);
      
      world.bodies.add(m_wheel, true, color(64), true, color(0), 4f);
    }

    {
      RevoluteJointDef jd = new RevoluteJointDef();

      jd.initialize(m_wheel, m_chassis, pivot.add(m_offset));
      jd.collideConnected = false;
      jd.motorSpeed = m_motorSpeed;
      jd.maxMotorTorque = 400.0f;
      jd.enableMotor = m_motorOn;
      m_motorJoint = (RevoluteJoint) world.createJoint(jd);
    }

    Vec2 wheelAnchor;

    wheelAnchor = pivot.add(new Vec2(0.0f, -0.8f));

    createLeg(-1.0f, wheelAnchor, color(255,96,32, 255));
    createLeg(+1.0f, wheelAnchor, color(255,96,32, 255));

    m_wheel.setTransform(m_wheel.getPosition(), 120.0f * MathUtils.PI / 180.0f);
    createLeg(-1.0f, wheelAnchor, color(32,255,96, 255));
    createLeg(+1.0f, wheelAnchor, color(32,255,96, 255));

    m_wheel.setTransform(m_wheel.getPosition(), -120.0f * MathUtils.PI / 180.0f);
    createLeg(-1.0f, wheelAnchor, color(32,96,255, 255));
    createLeg(+1.0f, wheelAnchor, color(32,96,255, 255));
    
    
    world.bodies.addAll();
    
    int bcount = world.bodies.debug_countBodiesWithoutShape();
    System.out.println("bodies w/o shape: "+bcount);
    int fcount = world.bodies.debug_countFixturesWithoutShape();
    System.out.println("fixtures w/o shape: "+fcount);
  }

  
  void createLeg(float s, Vec2 wheelAnchor, int col) {
    Vec2 p1 = new Vec2(5.4f * s, -6.1f);
    Vec2 p2 = new Vec2(7.2f * s, -1.2f);
    Vec2 p3 = new Vec2(4.3f * s, -1.9f);
    Vec2 p4 = new Vec2(3.1f * s, 0.8f);
    Vec2 p5 = new Vec2(6.0f * s, 1.5f);
    Vec2 p6 = new Vec2(2.5f * s, 3.7f);

    FixtureDef fd1 = new FixtureDef();
    FixtureDef fd2 = new FixtureDef();
    fd1.filter.groupIndex = -1;
    fd2.filter.groupIndex = -1;
    fd1.density = 1.0f;
    fd2.density = 1.0f;

    PolygonShape poly1 = new PolygonShape();
    PolygonShape poly2 = new PolygonShape();

    if (s > 0.0f) {
      Vec2[] vertices = new Vec2[3];

      vertices[0] = p1;
      vertices[1] = p2;
      vertices[2] = p3;
      poly1.set(vertices, 3);

      vertices[0] = new Vec2();
      vertices[1] = p5.sub(p4);
      vertices[2] = p6.sub(p4);
      poly2.set(vertices, 3);
    } else {
      Vec2[] vertices = new Vec2[3];

      vertices[0] = p1;
      vertices[1] = p3;
      vertices[2] = p2;
      poly1.set(vertices, 3);

      vertices[0] = new Vec2();
      vertices[1] = p6.sub(p4);
      vertices[2] = p5.sub(p4);
      poly2.set(vertices, 3);
    }

    fd1.shape = poly1;
    fd2.shape = poly2;

    BodyDef bd1 = new BodyDef(), bd2 = new BodyDef();
    bd1.type = BodyType.DYNAMIC;
    bd2.type = BodyType.DYNAMIC;
    bd1.position = m_offset;
    bd2.position = p4.add(m_offset);

    bd1.angularDamping = 10.0f;
    bd2.angularDamping = 10.0f;

    Body body1 = world.createBody(bd1);
    Body body2 = world.createBody(bd2);
    
    body1.createFixture(fd1);
    body2.createFixture(fd2);

    world.bodies.add(body1, true, col, true, color(0), 1f);
    world.bodies.add(body2, true, col, true, color(0), 1f);

    DistanceJointDef djd = new DistanceJointDef();

    // Using a soft distance constraint can reduce some jitter.
    // It also makes the structure seem a bit more fluid by
    // acting like a suspension system.
    djd.dampingRatio = 0.5f;
    djd.frequencyHz = 10.0f;

    djd.initialize(body1, body2, p2.add(m_offset), p5.add(m_offset));
    Joint joint1 = world.createJoint(djd);

    djd.initialize(body1, body2, p3.add(m_offset), p4.add(m_offset));
    Joint joint2 = world.createJoint(djd);

    djd.initialize(body1, m_wheel, p3.add(m_offset), wheelAnchor.add(m_offset));
    Joint joint3 = world.createJoint(djd);

    djd.initialize(body2, m_wheel, p6.add(m_offset), wheelAnchor.add(m_offset));
    Joint joint4 = world.createJoint(djd);
    
    RevoluteJointDef rjd = new RevoluteJointDef();

    rjd.initialize(body2, m_chassis, p4.add(m_offset));
    world.createJoint(rjd);
    
    world.bodies.add(joint1, false, col, true, col, 2f);
    world.bodies.add(joint2, false, col, true, col, 2f);
    world.bodies.add(joint3, false, col, true, col, 2f);
    world.bodies.add(joint4, false, col, true, col, 2f);
  
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_TheoJansenWalker.class.getName() });
  }
  
}