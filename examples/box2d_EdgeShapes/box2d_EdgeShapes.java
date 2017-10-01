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



package box2d_EdgeShapes;


import com.thomasdiewald.liquidfun.java.DwWorld;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_EdgeShapes extends PApplet {

  // Example to demonstrate how to use the RayCastCallback
  
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
    world.transform.setCamera(0, 10, 25);
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  int id = 0;
  public void draw(){
    
    // create a new body every second
    if((frameCount%120) == 0){
      Create(id++%5);
    }

    if(UPDATE_PHYSICS){
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
    step();
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
    if(key >= '1' && key <= '5') Create(key-'1');
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  
  

  int e_maxBodies = 256;
  int m_bodyIndex;
  Body m_bodies[] = new Body[e_maxBodies];
  PolygonShape m_polygons[] = new PolygonShape[4];
  CircleShape m_circle;

  float m_angle;
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/EdgeShapes.java
  public void initScene() {
    for (int i = 0; i < m_bodies.length; i++) {
      m_bodies[i] = null;
    }
    // Ground body
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      float x1 = -20.0f;
      float y1 = 2.0f * MathUtils.cos(x1 / 10.0f * MathUtils.PI);
      for (int i = 0; i < 80; ++i) {
        float x2 = x1 + 0.5f;
        float y2 = 2.0f * MathUtils.cos(x2 / 10.0f * MathUtils.PI);

        EdgeShape shape = new EdgeShape();
        shape.set(new Vec2(x1, y1), new Vec2(x2, y2));
        ground.createFixture(shape, 0.0f);

        x1 = x2;
        y1 = y2;
      }
      world.bodies.add(ground, false, color(0), true, color(255,255,0), 1f);
    }

    {
      Vec2 vertices[] = new Vec2[3];
      vertices[0] = new Vec2(-0.5f, 0.0f);
      vertices[1] = new Vec2(0.5f, 0.0f);
      vertices[2] = new Vec2(0.0f, 1.5f);
      m_polygons[0] = new PolygonShape();
      m_polygons[0].set(vertices, 3);
    }

    {
      Vec2 vertices[] = new Vec2[3];
      vertices[0] = new Vec2(-0.1f, 0.0f);
      vertices[1] = new Vec2(0.1f, 0.0f);
      vertices[2] = new Vec2(0.0f, 1.5f);
      m_polygons[1] = new PolygonShape();
      m_polygons[1].set(vertices, 3);
    }

    {
      float w = 1.0f;
      float b = w / (2.0f + MathUtils.sqrt(2.0f));
      float s = MathUtils.sqrt(2.0f) * b;

      Vec2 vertices[] = new Vec2[8];
      vertices[0] = new Vec2(0.5f * s, 0.0f);
      vertices[1] = new Vec2(0.5f * w, b);
      vertices[2] = new Vec2(0.5f * w, b + s);
      vertices[3] = new Vec2(0.5f * s, w);
      vertices[4] = new Vec2(-0.5f * s, w);
      vertices[5] = new Vec2(-0.5f * w, b + s);
      vertices[6] = new Vec2(-0.5f * w, b);
      vertices[7] = new Vec2(-0.5f * s, 0.0f);

      m_polygons[2] = new PolygonShape();
      m_polygons[2].set(vertices, 8);
    }

    {
      m_polygons[3] = new PolygonShape();
      m_polygons[3].setAsBox(0.5f, 0.5f);
    }

    {
      m_circle = new CircleShape();
      m_circle.m_radius = 0.5f;
    }

    m_bodyIndex = 0;
    m_angle = 0.0f;
  }
  
  
  
  
  
  void Create(int index) {
    if (m_bodies[m_bodyIndex] != null) {
      world.destroyBody(m_bodies[m_bodyIndex]);
      m_bodies[m_bodyIndex] = null;
    }

    BodyDef bd = new BodyDef();

    float x = MathUtils.randomFloat(-10.0f, 10.0f);
    float y = MathUtils.randomFloat(10.0f, 20.0f);
    bd.position.set(x, y);
    bd.angle = MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI);
    bd.type = BodyType.DYNAMIC;

    if (index == 4) {
      bd.angularDamping = 0.02f;
    }

    m_bodies[m_bodyIndex] = world.createBody(bd);

    if (index < 4) {
      FixtureDef fd = new FixtureDef();
      fd.shape = m_polygons[index];
      fd.friction = 0.3f;
      fd.density = 20.0f;
      m_bodies[m_bodyIndex].createFixture(fd);
    } else {
      FixtureDef fd = new FixtureDef();
      fd.shape = m_circle;
      fd.friction = 0.3f;
      fd.density = 20.0f;
      m_bodies[m_bodyIndex].createFixture(fd);
    }
    
    world.bodies.add(m_bodies[m_bodyIndex], true, color(255,0,0), true, color(0), 1f);

    m_bodyIndex = (m_bodyIndex + 1) % e_maxBodies;
  }
  
  
  
 
  EdgeShapesCallback callback = new EdgeShapesCallback();


  public void step(){
    
    m_angle += 0.25f * PI / 180.0f;

    float radius = 25.0f;
    Vec2 point1 = new Vec2(0.0f, 10.0f);
    Vec2 p1p2 = new Vec2(radius * cos(m_angle), -radius * abs(sin(m_angle)));
    Vec2 point2 = point1.add(p1p2);

    callback.m_fixture = null;
    world.raycast(callback, point1, point2);

    if (callback.m_fixture != null) {
      world.debug_draw.drawPoint(callback.m_point, 5.0f/world.transform.screen_scale, new Color3f(0.4f, 0.9f, 0.4f));
      world.debug_draw.drawSegment(point1, callback.m_point, new Color3f(0.8f, 0.8f, 0.8f));
      Vec2 head = callback.m_normal.mul(.5f).addLocal(callback.m_point);
      world.debug_draw.drawSegment(callback.m_point, head, new Color3f(0.9f, 0.9f, 0.4f));
    } else {
      world.debug_draw.drawSegment(point1, point2, new Color3f(0.8f, 0.8f, 0.8f));
    }

  }

  
  static class EdgeShapesCallback implements RayCastCallback {

    Fixture m_fixture = null;
    Vec2 m_point;
    Vec2 m_normal;

    public float reportFixture(Fixture fixture, final Vec2 point, final Vec2 normal, float fraction) {
      m_fixture = fixture;
      m_point = point;
      m_normal = normal;
      return fraction;
    }
  }


  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_EdgeShapes.class.getName() });
  }
  
}