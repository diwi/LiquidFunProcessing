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



package box2d_Car;

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
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WheelJoint;
import org.jbox2d.dynamics.joints.WheelJointDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Car extends PApplet {
  
  //
  // A little gaming world.
  // The car-wheels are attached via WheelJointDef's which  allow for some damping.
  // 
  // Keyboard keys or the mouse can be used to control the car.
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
  // 'a'         ... move backwards 
  // 's'         ... stop
  // 'd'         ... move forwards
  // 'q'         ... dec damping
  // 'e'         ... inc damping

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
    world.mouse_drag_bodies.mult_dragforce = 100f;
    world.zombie_aabb.lowerBound.x = -Float.MAX_VALUE;
    world.zombie_aabb.upperBound.x = +Float.MAX_VALUE;

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  

  public void draw(){
 
    // set camera
    Vec2 pos = m_car.m_xf.p;
    world.transform.setCamera(pos.x, pos.y/2 + 5);
    
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
    text("'q' dec damping"   , tx, ty+=gy);
    text("'e' inc damping"   , tx, ty+=gy);
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
    
    switch (key) {
      case 'a':
      case 's':
      case 'd':
        m_spring1.enableMotor(false);
        break;
    }
  }
  
  public void keyPressed() {
    switch (key) {
      case 'a':
        m_spring1.enableMotor(true);
        m_spring1.setMotorSpeed(m_speed);
        break;
      case 's':
        m_spring1.enableMotor(true);
        m_spring1.setMotorSpeed(0.0f);
        break;
      case 'd':
        m_spring1.enableMotor(true);
        m_spring1.setMotorSpeed(-m_speed);
        break;
      case 'q':
        m_hz = MathUtils.max(0.0f, m_hz - 1.0f);
        m_spring1.setSpringFrequencyHz(m_hz);
        m_spring2.setSpringFrequencyHz(m_hz);
        break;
      case 'e':
        m_hz += 1.0f;
        m_spring1.setSpringFrequencyHz(m_hz);
        m_spring2.setSpringFrequencyHz(m_hz);
        break;
    }
  }


  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  
  private Body m_car;
  private Body m_wheel1;
  private Body m_wheel2;

  private float m_hz;
  private float m_zeta;
  private float m_speed;
  private WheelJoint m_spring1;
  private WheelJoint m_spring2;
  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Car.java
  public void initScene() {
    
    m_hz = 4.0f;
    m_zeta = 0.7f;
    m_speed = 50.0f;

    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 0.0f;
      fd.friction = 0.6f;

      shape.set(new Vec2(-20.0f, 0.0f), new Vec2(20.0f, 0.0f));
      ground.createFixture(fd);

      float hs[] = {0.25f, 1.0f, 4.0f, 0.0f, 0.0f, -1.0f, -2.0f, -2.0f, -1.25f, 0.0f};

      float x = 20.0f, y1 = 0.0f, dx = 5.0f;

      for (int i = 0; i < 10; ++i) {
        float y2 = hs[i];
        shape.set(new Vec2(x, y1), new Vec2(x + dx, y2));
        ground.createFixture(fd);
        y1 = y2;
        x += dx;
      }

      for (int i = 0; i < 10; ++i) {
        float y2 = hs[i];
        shape.set(new Vec2(x, y1), new Vec2(x + dx, y2));
        ground.createFixture(fd);
        y1 = y2;
        x += dx;
      }

      shape.set(new Vec2(x, 0.0f), new Vec2(x + 40.0f, 0.0f));
      ground.createFixture(fd);

      x += 80.0f;
      shape.set(new Vec2(x, 0.0f), new Vec2(x + 40.0f, 0.0f));
      ground.createFixture(fd);

      x += 40.0f;
      shape.set(new Vec2(x, 0.0f), new Vec2(x + 10.0f, 5.0f));
      ground.createFixture(fd);

      x += 20.0f;
      shape.set(new Vec2(x, 0.0f), new Vec2(x + 40.0f, 0.0f));
      ground.createFixture(fd);

      x += 40.0f;
      shape.set(new Vec2(x, 0.0f), new Vec2(x, 20.0f));
      ground.createFixture(fd);
    }

    // Teeter
    {
      BodyDef bd = new BodyDef();
      bd.position.set(140.0f, 1.0f);
      bd.type = BodyType.DYNAMIC;
      Body body = world.createBody(bd);

      PolygonShape box = new PolygonShape();
      box.setAsBox(10.0f, 0.25f);
      body.createFixture(box, 1.0f);

      RevoluteJointDef jd = new RevoluteJointDef();
      jd.initialize(ground, body, body.getPosition());
      jd.lowerAngle = -8.0f * MathUtils.PI / 180.0f;
      jd.upperAngle = 8.0f * MathUtils.PI / 180.0f;
      jd.enableLimit = true;
      world.createJoint(jd);

      body.applyAngularImpulse(100.0f);
    }

    // Bridge
    {
      int N = 20;
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(1.0f, 0.125f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 1.0f;
      fd.friction = 0.6f;

      RevoluteJointDef jd = new RevoluteJointDef();

      Body prevBody = ground;
      for (int i = 0; i < N; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(161.0f + 2.0f * i, -0.125f);
        Body body = world.createBody(bd);
        body.createFixture(fd);

        Vec2 anchor = new Vec2(160.0f + 2.0f * i, -0.125f);
        jd.initialize(prevBody, body, anchor);
        world.createJoint(jd);

        prevBody = body;
      }

      Vec2 anchor = new Vec2(160.0f + 2.0f * N, -0.125f);
      jd.initialize(prevBody, ground, anchor);
      world.createJoint(jd);
    }

    // Boxes
    {
      PolygonShape box = new PolygonShape();
      box.setAsBox(0.5f, 0.5f);

      Body body = null;
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;

      bd.position.set(230.0f, 0.5f);
      body = world.createBody(bd);
      body.createFixture(box, 0.5f);

      bd.position.set(230.0f, 1.5f);
      body = world.createBody(bd);
      body.createFixture(box, 0.5f);

      bd.position.set(230.0f, 2.5f);
      body = world.createBody(bd);
      body.createFixture(box, 0.5f);

      bd.position.set(230.0f, 3.5f);
      body = world.createBody(bd);
      body.createFixture(box, 0.5f);

      bd.position.set(230.0f, 4.5f);
      body = world.createBody(bd);
      body.createFixture(box, 0.5f);
    }

    // Car
    {
      PolygonShape chassis = new PolygonShape();
      Vec2 vertices[] = new Vec2[8];
      vertices[0] = new Vec2(-1.5f, -0.5f);
      vertices[1] = new Vec2(1.5f, -0.5f);
      vertices[2] = new Vec2(1.5f, 0.0f);
      vertices[3] = new Vec2(0.0f, 0.9f);
      vertices[4] = new Vec2(-1.15f, 0.9f);
      vertices[5] = new Vec2(-1.5f, 0.2f);
      chassis.set(vertices, 6);

      CircleShape circle = new CircleShape();
      circle.m_radius = 0.5f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(0.0f, 1.0f);
      m_car = world.createBody(bd);
      m_car.createFixture(chassis, 1.0f);

      FixtureDef fd = new FixtureDef();
      fd.shape = circle;
      fd.density = 1.0f;
      fd.friction = 0.9f;

      bd.position.set(-1.0f, 0.35f);
      m_wheel1 = world.createBody(bd);
      m_wheel1.createFixture(fd);

      bd.position.set(1.0f, 0.4f);
      m_wheel2 = world.createBody(bd);
      m_wheel2.createFixture(fd);

      WheelJointDef jd = new WheelJointDef();
      Vec2 axis = new Vec2(0.0f, 1.0f);

      jd.initialize(m_car, m_wheel1, m_wheel1.getPosition(), axis);
      jd.motorSpeed = 0.0f;
      jd.maxMotorTorque = 20.0f;
      jd.enableMotor = true;
      jd.frequencyHz = m_hz;
      jd.dampingRatio = m_zeta;
      m_spring1 = (WheelJoint) world.createJoint(jd);

      jd.initialize(m_car, m_wheel2, m_wheel2.getPosition(), axis);
      jd.motorSpeed = 0.0f;
      jd.maxMotorTorque = 10.0f;
      jd.enableMotor = false;
      jd.frequencyHz = m_hz;
      jd.dampingRatio = m_zeta;
      m_spring2 = (WheelJoint) world.createJoint(jd);
    }
    
   

    world.bodies.add(m_car, true, color(0,128,255, 160), true, color(0), 1f);
    world.bodies.add(m_wheel1, true, color(255,128,0), true, color(0), 1f);
    world.bodies.add(m_wheel2, true, color(128,255,0), true, color(0), 1f);
    world.bodies.add(ground, false, color(128,255,0), true, color(0), 2f);
  
    
    // add wheel hub for rendering, no need to create extra box2d bodies for that.
    float diam = 0.2f;
    
    {
      PShape shp_cross = createShape();
      shp_cross.beginShape(LINES);
      shp_cross.stroke(0);
      shp_cross.strokeWeight(1f / world.transform.screen_scale);
      shp_cross.vertex(-diam, 0); shp_cross.vertex(+diam, 0);
      shp_cross.vertex(0, -diam); shp_cross.vertex(0, +diam);
      shp_cross.endShape();
      DwWorld.getShape(m_wheel1).shape.addChild(shp_cross);
    }
    
    {
      PShape shp_cross = createShape();
      shp_cross.beginShape(LINES);
      shp_cross.stroke(0);
      shp_cross.strokeWeight(1f / world.transform.screen_scale);
      shp_cross.vertex(-diam, 0); shp_cross.vertex(+diam, 0);
      shp_cross.vertex(0, -diam); shp_cross.vertex(0, +diam);
      shp_cross.endShape();
      DwWorld.getShape(m_wheel2).shape.addChild(shp_cross);
    }
    
    
    world.bodies.addAll();
    
  }
  
  
  
  
  
  
  
  
  
  

  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Car.class.getName() });
  }
  
}