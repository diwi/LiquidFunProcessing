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

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_CompoundShapes extends PApplet {

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
    
    world = new DwWorld(this, 25);
    world.transform.setScreen(width, height, 25, width/2, height-10);

    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
    bodies.addBullet(true, color(200, 0, 0), true, color(0), 1f);
    
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
      bodies.display(canvas);
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
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/CompoundShapes.java
  public void initScene() {
    
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    float thick = 20f /world.transform.screen_scale;
    {
      BodyDef bd = new BodyDef();
      bd.position.set(0.0f, 0.0f);
      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(dimx, thick);

      body.createFixture(shape, 0.0f);
      
      bodies.add(body, true, color(0), false, color(0), 1);
    }

    {
      CircleShape circle1 = new CircleShape();
      circle1.m_radius = 0.5f;
      circle1.m_p.set(-0.5f, 0.5f);

      CircleShape circle2 = new CircleShape();;
      circle2.m_radius = 0.5f;
      circle2.m_p.set(0.5f, 0.5f);

      for (int i = 0; i < 10; ++i) {
        float x = MathUtils.randomFloat(-0.1f, 0.1f);
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(x + 5.0f, 1.05f + 2.5f * i);
        bd.angle = MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI);
        Body body = world.createBody(bd);
        body.createFixture(circle1, 2.0f);
        body.createFixture(circle2, 0.0f);
        
        float r = random(128, 255);
        float g = r/2;
        float b = 0;
        bodies.add(body, true, color(r,g,b), true, color(0), 1);
      }
    }

    {
      PolygonShape polygon1 = new PolygonShape();
      polygon1.setAsBox(0.25f, 0.5f);

      PolygonShape polygon2 = new PolygonShape();
      polygon2.setAsBox(0.25f, 0.5f, new Vec2(0.0f, -0.5f), 0.5f * MathUtils.PI);

      for (int i = 0; i < 10; ++i) {
        float x = MathUtils.randomFloat(-0.1f, 0.1f);
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(x - 5.0f, 1.05f + 2.5f * i);
        bd.angle = MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI);
        Body body = world.createBody(bd);
        body.createFixture(polygon1, 2.0f);
        body.createFixture(polygon2, 2.0f);
        
        float b = random(128, 255);
        float g = b/2;
        float r = 0;
        bodies.add(body, true, color(r,g,b), true, color(0), 1);
      }
    }

    {
      Transform xf1 = new Transform();
      xf1.q.set(0.3524f * MathUtils.PI);
      Rot.mulToOut(xf1.q, new Vec2(1.0f, 0.0f), xf1.p);

      Vec2[] vertices = new Vec2[3];

      PolygonShape triangle1 = new PolygonShape();
      vertices[0] = Transform.mul(xf1, new Vec2(-1.0f, 0.0f));
      vertices[1] = Transform.mul(xf1, new Vec2(1.0f, 0.0f));
      vertices[2] = Transform.mul(xf1, new Vec2(0.0f, 0.5f));
      triangle1.set(vertices, 3);

      Transform xf2 = new Transform();
      xf2.q.set(-0.3524f * MathUtils.PI);
      Rot.mulToOut(xf2.q, new Vec2(-1.0f, 0.0f), xf2.p);

      PolygonShape triangle2 = new PolygonShape();
      vertices[0] = Transform.mul(xf2, new Vec2(-1.0f, 0.0f));
      vertices[1] = Transform.mul(xf2, new Vec2(1.0f, 0.0f));
      vertices[2] = Transform.mul(xf2, new Vec2(0.0f, 0.5f));
      triangle2.set(vertices, 3);

      for (int i = 0; i < 10; ++i) {
        float x = MathUtils.randomFloat(-0.1f, 0.1f);
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(x, 2.05f + 2.5f * i);
        bd.angle = 0.0f;
        Body body = world.createBody(bd);
        body.createFixture(triangle1, 2.0f);
        body.createFixture(triangle2, 2.0f);
        
        float g = random(180, 255);
        float r = g * 0.9f;
        float b = 0;
        bodies.add(body, true, color(r,g,b), true, color(0), 1);
      }
    }

    {
      PolygonShape bottom = new PolygonShape();
      bottom.setAsBox(1.5f, 0.15f);

      PolygonShape left = new PolygonShape();
      left.setAsBox(0.15f, 2.7f, new Vec2(-1.45f, 2.35f), 0.2f);

      PolygonShape right = new PolygonShape();
      right.setAsBox(0.15f, 2.7f, new Vec2(1.45f, 2.35f), -0.2f);

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(0.0f, 2.0f);
      Body body = world.createBody(bd);
      Fixture fixture1 = body.createFixture(bottom, 4.0f);
      Fixture fixture2 = body.createFixture(left, 4.0f);
      Fixture fixture3 = body.createFixture(right, 4.0f);
      
      bodies.add(fixture1, true, color(255,0,0), true, color(0), 1);
      bodies.add(fixture2, true, color(0,255,0), true, color(0), 1);
      bodies.add(fixture3, true, color(0,0,255), true, color(0), 1);
    }
    
    
    bodies.addAll();
  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_CompoundShapes.class.getName() });
  }
  
}