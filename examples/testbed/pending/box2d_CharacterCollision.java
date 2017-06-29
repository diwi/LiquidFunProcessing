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



package testbed.pending;

import com.thomasdiewald.liquidfun.java.DwWorld;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_CharacterCollision extends PApplet {

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
    
    world = new DwWorld(this, 30);

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
    if(key == 'r') reset();
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  private Body m_character;
  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/CharacterCollision.java
  public void initScene() {
    
    // Ground body
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.set(new Vec2(-20.0f, 0.0f), new Vec2(20.0f, 0.0f));
      ground.createFixture(shape, 0.0f);
    }

    // Collinear edges
    // This shows the problematic case where a box shape can hit
    // an internal vertex.
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      EdgeShape shape = new EdgeShape();
      shape.m_radius = 0.0f;
      shape.set(new Vec2(-8.0f, 1.0f), new Vec2(-6.0f, 1.0f));
      ground.createFixture(shape, 0.0f);
      shape.set(new Vec2(-6.0f, 1.0f), new Vec2(-4.0f, 1.0f));
      ground.createFixture(shape, 0.0f);
      shape.set(new Vec2(-4.0f, 1.0f), new Vec2(-2.0f, 1.0f));
      ground.createFixture(shape, 0.0f);
    }

    // Chain shape
    {
      BodyDef bd = new BodyDef();
      bd.angle = 0.25f * MathUtils.PI;
      Body ground = world.createBody(bd);

      Vec2[] vs = new Vec2[4];
      vs[0] = new Vec2(5.0f, 7.0f);
      vs[1] = new Vec2(6.0f, 8.0f);
      vs[2] = new Vec2(7.0f, 8.0f);
      vs[3] = new Vec2(8.0f, 7.0f);
      ChainShape shape = new ChainShape();
      shape.createChain(vs, 4);
      ground.createFixture(shape, 0.0f);
    }

    // Square tiles. This shows that adjacency shapes may
    // have non-smooth collision. There is no solution
    // to this problem.
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(1.0f, 1.0f, new Vec2(4.0f, 3.0f), 0.0f);
      ground.createFixture(shape, 0.0f);
      shape.setAsBox(1.0f, 1.0f, new Vec2(6.0f, 3.0f), 0.0f);
      ground.createFixture(shape, 0.0f);
      shape.setAsBox(1.0f, 1.0f, new Vec2(8.0f, 3.0f), 0.0f);
      ground.createFixture(shape, 0.0f);
    }

    // Square made from an edge loop. Collision should be smooth.
    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      Vec2[] vs = new Vec2[4];
      vs[0] = new Vec2(-1.0f, 3.0f);
      vs[1] = new Vec2(1.0f, 3.0f);
      vs[2] = new Vec2(1.0f, 5.0f);
      vs[3] = new Vec2(-1.0f, 5.0f);
      ChainShape shape = new ChainShape();
      shape.createLoop(vs, 4);
      ground.createFixture(shape, 0.0f);
    }

    // Edge loop. Collision should be smooth.
    {
      BodyDef bd = new BodyDef();
      bd.position.set(-10.0f, 4.0f);
      Body ground = world.createBody(bd);

      Vec2[] vs = new Vec2[10];
      vs[0] = new Vec2(0.0f, 0.0f);
      vs[1] = new Vec2(6.0f, 0.0f);
      vs[2] = new Vec2(6.0f, 2.0f);
      vs[3] = new Vec2(4.0f, 1.0f);
      vs[4] = new Vec2(2.0f, 2.0f);
      vs[5] = new Vec2(0.0f, 2.0f);
      vs[6] = new Vec2(-2.0f, 2.0f);
      vs[7] = new Vec2(-4.0f, 3.0f);
      vs[8] = new Vec2(-6.0f, 2.0f);
      vs[9] = new Vec2(-6.0f, 0.0f);
      ChainShape shape = new ChainShape();
      shape.createLoop(vs, 10);
      ground.createFixture(shape, 0.0f);
    }

    // Square character 1
    {
      BodyDef bd = new BodyDef();
      bd.position.set(-3.0f, 8.0f);
      bd.type = BodyType.DYNAMIC;
      bd.fixedRotation = true;
      bd.allowSleep = false;

      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 0.5f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;
      body.createFixture(fd);
    }

    // Square character 2
    {
      BodyDef bd = new BodyDef();
      bd.position.set(-5.0f, 5.0f);
      bd.type = BodyType.DYNAMIC;
      bd.fixedRotation = true;
      bd.allowSleep = false;

      Body body = world.createBody(bd);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.25f, 0.25f);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;
      body.createFixture(fd);
    }

    // Hexagon character
    {
      BodyDef bd = new BodyDef();
      bd.position.set(-5.0f, 8.0f);
      bd.type = BodyType.DYNAMIC;
      bd.fixedRotation = true;
      bd.allowSleep = false;

      Body body = world.createBody(bd);

      float angle = 0.0f;
      float delta = MathUtils.PI / 3.0f;
      Vec2 vertices[] = new Vec2[6];
      for (int i = 0; i < 6; ++i) {
        vertices[i] = new Vec2(0.5f * MathUtils.cos(angle), 0.5f * MathUtils.sin(angle));
        angle += delta;
      }

      PolygonShape shape = new PolygonShape();
      shape.set(vertices, 6);

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;
      body.createFixture(fd);
    }

    // Circle character
    {
      BodyDef bd = new BodyDef();
      bd.position.set(3.0f, 5.0f);
      bd.type = BodyType.DYNAMIC;
      bd.fixedRotation = true;
      bd.allowSleep = false;

      Body body = world.createBody(bd);

      CircleShape shape = new CircleShape();
      shape.m_radius = 0.5f;

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;
      body.createFixture(fd);
    }

    // Circle character
    {
      BodyDef bd = new BodyDef();
      bd.position.set(-7.0f, 6.0f);
      bd.type = BodyType.DYNAMIC;
      bd.allowSleep = false;

      m_character = world.createBody(bd);

      CircleShape shape = new CircleShape();
      shape.m_radius = 0.25f;

      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = 20.0f;
      fd.friction = 1;
      m_character.createFixture(fd);
    }
    
    
    world.bodies.addAll();
  }

  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_CharacterCollision.class.getName() });
  }
  
}