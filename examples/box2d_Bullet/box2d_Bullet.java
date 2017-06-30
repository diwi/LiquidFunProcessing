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



package box2d_Bullet;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseShootBullet;
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Bullet extends PApplet {
  
  
  //
  // Bullet demo.
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
  DwBodyGroup bodies;
  DwMouseShootBullet bullet;
  
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
    if(bodies    != null) bodies   .release(); bodies    = null;
//    if(particles != null) particles.release(); particles = null;
    if(world     != null) world    .release(); world     = null;  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 22);
    world.transform.setScreen(width, height, 40, width/2, height-50);
    
    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);
    
    bullet = new DwMouseShootBullet(world, world.transform);
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    if(UPDATE_PHYSICS){
      if(frameCount % 120 == 0){
        launch();
      }
      bullet.updateSpawn(mouseX, mouseY);
      world.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.background(32);
    canvas.pushMatrix();
    world.applyTransform(canvas);
    bullet.drawSpawnTrack(canvas);
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
  
  public void mousePressed(){
    bullet.beginSpawn(mouseX, mouseY);
  }
  
  public void mouseReleased(){
    bullet.endSpawn(mouseX, mouseY);
    bodies.add(bullet.popBullet(), true, color(255,32,0), true, color(0), 1f);
  }
  

  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////
  
  Body m_body;
  Body m_bullet;
  float m_x;
  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/BulletTest.java
  public void initScene() {
    {
      BodyDef bd = new BodyDef();
      bd.position.set(0.0f, 0.0f);
      Body body = world.createBody(bd);

      EdgeShape edge = new EdgeShape();

      edge.set(new Vec2(-10.0f, 0.0f), new Vec2(10.0f, 0.0f));
      body.createFixture(edge, 0.0f);

      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.2f, 1.0f, new Vec2(0.5f, 1.0f), 0.0f);
      body.createFixture(shape, 0.0f);
    }

    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(0.0f, 4.0f);

      PolygonShape box = new PolygonShape();
      box.setAsBox(2.0f, 0.1f);

      m_body = world.createBody(bd);
      m_body.createFixture(box, 1.0f);

      box.setAsBox(0.25f, 0.25f);

      // m_x = RandomFloat(-1.0f, 1.0f);
      m_x = -0.06530577f;
      bd.position.set(m_x, 10.0f);
      bd.bullet = true;

      m_bullet = world.createBody(bd);
      m_bullet.createFixture(box, 100.0f);

      m_bullet.setLinearVelocity(new Vec2(0.0f, -50.0f));
    }
    

    bodies.addAll(); 
  }

  
  
  public void launch() {
    m_body.setTransform(new Vec2(0.0f, 4.0f), 0.0f);
    m_body.setLinearVelocity(new Vec2());
    m_body.setAngularVelocity(0.0f);

    m_x = MathUtils.randomFloat(-1.0f, 1.0f);
    m_bullet.setTransform(new Vec2(m_x, 10.0f), 0.0f);
    m_bullet.setLinearVelocity(new Vec2(0.0f, -50.0f));
    m_bullet.setAngularVelocity(0.0f);
  }


   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Bullet.class.getName() });
  }
  
}