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
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.WeldJointDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Cantilever_WeldJoint extends PApplet {

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
    
    world = new DwWorld(this, 40);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){

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
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/Cantilever.java
  public void initScene() {
    
    int e_count = 8;
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
      ground.createFixture(shape, 0.0f);
    }

    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 0.125f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;

      WeldJointDef jd = new WeldJointDef();

      Body prevBody = ground;
      for (int i = 0; i < e_count; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(-14.5f + 1.0f * i, 5.0f);
        Body body = world.createBody(bd);
        body.createFixture(fd);

        Vec2 anchor = new Vec2(-15.0f + 1.0f * i, 5.0f);
        jd.initialize(prevBody, body, anchor);
        world.createJoint(jd);
        

        prevBody = body;
      }
    }

    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(1f, 0.125f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;

      WeldJointDef jd = new WeldJointDef();
      jd.frequencyHz = 5f;
      jd.dampingRatio = .7f;

      Body prevBody = ground;
      for (int i = 0; i < 3; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(-14.0f + 2.0f * i, 15.0f);
        Body body = world.createBody(bd);
        body.createFixture(fd);

        Vec2 anchor = new Vec2(-15.0f + 2.0f * i, 15.0f);
        jd.initialize(prevBody, body, anchor);
        world.createJoint(jd);

        prevBody = body;
      }
    }

    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 0.125f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;

      WeldJointDef jd = new WeldJointDef();

      Body prevBody = ground;
      for (int i = 0; i < e_count; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(-4.5f + 1.0f * i, 5.0f);
        Body body = world.createBody(bd);
        body.createFixture(fd);

        if (i > 0) {
          Vec2 anchor = new Vec2(-5.0f + 1.0f * i, 5.0f);
          jd.initialize(prevBody, body, anchor);
          world.createJoint(jd);
        }

        prevBody = body;
      }
    }

    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 0.125f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;

      WeldJointDef jd = new WeldJointDef();
      jd.frequencyHz = 8f;
      jd.dampingRatio = .7f;

      Body prevBody = ground;
      for (int i = 0; i < e_count; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(5.5f + 1.0f * i, 10.0f);
        Body body = world.createBody(bd);
        body.createFixture(fd);

        if (i > 0) {
          Vec2 anchor = new Vec2(5.0f + 1.0f * i, 10.0f);
          jd.initialize(prevBody, body, anchor);
          world.createJoint(jd);
        }

        prevBody = body;
      }
    }

    for (int i = 0; i < 2; ++i) {
      Vec2 vertices[] = new Vec2[3];
      vertices[0] = new Vec2(-0.5f, 0.0f);
      vertices[1] = new Vec2(0.5f, 0.0f);
      vertices[2] = new Vec2(0.0f, 1.5f);

      PolygonShape shape = new PolygonShape();
      shape.set(vertices, 3);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 1.0f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-8.0f + 8.0f * i, 12.0f);
      Body body = world.createBody(bd);
      body.createFixture(fd);
    }

    for (int i = 0; i < 2; ++i) {
      CircleShape shape = new CircleShape();
      shape.m_radius = 0.5f;

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 1.0f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-6.0f + 6.0f * i, 10.0f);
      Body body = world.createBody(bd);
      body.createFixture(fd);
    }
    
    
    world.bodies.addAll();
  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Cantilever_WeldJoint.class.getName() });
  }
  
}