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
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_LiquidTimer extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;
  DwBodyGroup bodies;
  DwParticleRenderGL particles;
  
//  PImage sprite;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    
//    sprite = loadImage("sprite.png");
    
    reset();
    frameRate(120);
  }
  
  
  public void release(){
    if(bodies != null) bodies.release(); bodies = null;
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 18);
    world.transform.setScreen(width, height, 18, width/2, height);

    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);
    
    particles = new DwParticleRenderGL(this, world, world.transform);
//    particles.param.tex_sprite = sprite;
    particles.param.falloff_exp1 = 1;
    particles.param.falloff_exp2 = 2;
    particles.param.radius_scale = 2f;
    particles.param.falloff_mult = 1;
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
    bodies.addBullet(true, color(200, 0, 0), true, color(0), 1f);
    
    if(UPDATE_PHYSICS){
      world.update();
      particles.update();
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
      particles.display(canvas);
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
 
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/LiquidTimer.java
  public void initScene() {
    
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      ChainShape shape = new ChainShape();
      final Vec2[] vertices = {new Vec2(-20, 0), new Vec2(20, 0), new Vec2(20, 40), new Vec2(-20, 40)};
      shape.createLoop(vertices, 4);
      ground.createFixture(shape, 0.0f);
      
      bodies.add(ground, false, color(0), true, color(255), 1f);
    }


    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-20, 32), new Vec2(-12, 32));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-11, 32), new Vec2(20, 32));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-12, 32), new Vec2(-12, 28));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-11, 32), new Vec2(-11, 28));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-16, 24), new Vec2(8, 20));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(16, 16), new Vec2(-8, 12));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-12, 8), new Vec2(-12, 0));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-4, 8), new Vec2(-4, 0));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(4, 8), new Vec2(4, 0));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }

    {
      BodyDef bd = new BodyDef();
      Body body = world.createBody(bd);
      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(12, 8), new Vec2(12, 0));
      body.createFixture(shape, 0.1f);
      bodies.add(body, false, color(0), true, color(255), 1f);
    }
    
    bodies.addAll();
    
    
    
    world.setParticleRadius(0.15f);
    {
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(20, 4, new Vec2(0, 36), 0);
      ParticleGroupDef pd = new ParticleGroupDef();
      pd.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
      pd.shape = shape;
      pd.setColor(new Color3f(1, 0.2f, 0.05f));
      world.createParticleGroup(pd);
    }
    
  }
  
  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_LiquidTimer.class.getName() });
  }
  
}