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

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_MobileBalanced extends PApplet {

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
    
    world = new DwWorld(this, 22);
    world.transform.setCamera(0, 18, 50);
    world.debug_draw.setStrokeWeight(1f);
    
    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    if(UPDATE_PHYSICS){
      world.update();
    }
    
    bodies.addBullet(true, color(200, 0, 0), true, color(0), 1f);

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


  int e_depth = 6;
  
  // https://github.com/erincatto/Box2D/blob/master/Box2D/Testbed/Tests/MobileBalanced.h
  public void initScene()   {
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      bd.position.set(0.0f, 20.0f);
      ground = world.createBody(bd);
    }

    float a = 0.5f;
    Vec2 h = new Vec2(0.0f, a);

    colorMode(HSB, 1f);
    
    Body root = AddNode(ground, new Vec2(), 0, 3.0f, a);
    
    colorMode(RGB, 255);

    RevoluteJointDef jointDef = new RevoluteJointDef();
    jointDef.bodyA = ground;
    jointDef.bodyB = root;
    jointDef.localAnchorA.setZero();
    jointDef.localAnchorB = h;
    world.createJoint(jointDef);
    
    bodies.createAll();
  }

  
  Body AddNode(Body parent, Vec2 localAnchor, int depth, float offset, float a){
    
    a *= 0.90f;
    
    float fdepth = 1 - depth / (float) (e_depth+1);
    
    float density = 20.0f;
    Vec2 h = new Vec2(0.0f, a);
   
    Vec2 p = parent.getPosition().add(localAnchor).sub(h);

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = p;
    Body body = world.createBody(bodyDef);
    bodies.add(body, true, color(0), true, color(0), 1f);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(0.25f * a, a);
    Fixture fixture1 = body.createFixture(shape, density);
    bodies.add(fixture1, true, color(fdepth, 1, 1), true, color(0), 1f);

    if (depth == e_depth){
      return body;
    }

    shape.setAsBox(offset, 0.25f * a, new Vec2(0, -a), 0.0f);
    Fixture fixture2 = body.createFixture(shape, density);
    bodies.add(fixture2, true, color(fdepth, 1, 1), true, color(0), 1f);
    
    Vec2 a1 = new Vec2(offset, -a);
    Vec2 a2 = new Vec2(-offset, -a);
    Body body1 = AddNode(body, a1, depth + 1, 0.5f * offset, a);
    Body body2 = AddNode(body, a2, depth + 1, 0.5f * offset, a);

    RevoluteJointDef jointDef = new RevoluteJointDef();
    jointDef.bodyA = body;
    jointDef.localAnchorB = h;

    jointDef.localAnchorA = a1;
    jointDef.bodyB = body1;
    world.createJoint(jointDef);

    jointDef.localAnchorA = a2;
    jointDef.bodyB = body2;
    world.createJoint(jointDef);

    return body;
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_MobileBalanced.class.getName() });
  }
  
}