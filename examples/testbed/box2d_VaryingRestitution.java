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
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_VaryingRestitution extends PApplet {

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
    world.transform.setScreen(width, height, 15, width/2, height-100);
    
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

  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/VaryingRestitution.java
  public void initScene() {
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
      ground.createFixture(shape, 0.0f);
      
      bodies.add(ground, false, color(0), true, color(200), 1f);
    }

    {
      CircleShape shape = new CircleShape();
      shape.m_radius = 0.7f;

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 1.0f;


      int count = 40;
      for (int i = 0; i < count; ++i) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(-count * 0.8f + 1.6f * i, 20.0f);

        Body body = world.createBody(bd);

        fd.restitution = i / (float)count;
        body.createFixture(fd);
        
        float base = 65;
        float col = (255 - base) * fd.restitution;
        
        bodies.add(body, true, color(base + col, base, base), false, color(200), 1f);
      }
    }

  }
  
  
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_VaryingRestitution.class.getName() });
  }
  
}