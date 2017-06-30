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
import com.thomasdiewald.liquidfun.java.ParticleEmitter;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_Chain_DistanceJoints_LiquidFx extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;
  boolean APPLY_LIQUID_FX = true;
  
  DwWorld world;
  
  DwPixelFlow pixelflow;
  DwLiquidFX liquidfx;
  
  PGraphics2D pg_particles;
  PGraphics2D pg_bodies;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    
    pixelflow = new DwPixelFlow(this);
    liquidfx = new DwLiquidFX(pixelflow);
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    pg_bodies = (PGraphics2D) createGraphics(width, height, P2D);
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

    world.particles.param.falloff_exp1 = 3;
    world.particles.param.falloff_exp2 = 1;
    world.particles.param.radius_scale = 2;
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
//    world.bodies.addBullet(true, color(128), true, color(0), 1f);
    
    if(UPDATE_PHYSICS){
      addParticles();
      addBodies();
      world.update();
    }
    
    int BACKGROUND = 32;

    if(USE_DEBUG_DRAW){
      PGraphics2D canvas = (PGraphics2D) this.g;
      
      background(BACKGROUND);
      pushMatrix();
      world.applyTransform(canvas);
      world.drawBulletSpawnTrack(canvas);
      world.displayDebugDraw(canvas);
      popMatrix();
    } else {

      pg_particles.beginDraw();
      pg_particles.clear();
      pg_particles.background(BACKGROUND, 0);
      world.applyTransform(pg_particles);
      world.particles.display(pg_particles, 0);
      pg_particles.endDraw();
      
      pg_bodies.beginDraw();
      pg_bodies.clear();
      pg_bodies.background(BACKGROUND, 0);
      world.applyTransform(pg_bodies);
      world.bodies.display(pg_bodies);
      pg_bodies.endDraw();
      
   
      if(APPLY_LIQUID_FX)
      {
        liquidfx.param.base_LoD = 0;
        liquidfx.param.base_blur_radius = 2;
        liquidfx.param.base_threshold = 0.7f;
        liquidfx.param.highlight_enabled = true;
        liquidfx.param.highlight_LoD = 1;
        liquidfx.param.highlight_decay = 0.6f;
        liquidfx.param.sss_enabled = true;
        liquidfx.param.sss_LoD = 2;
        liquidfx.param.sss_decay = 0.5f;
        
        liquidfx.apply(pg_particles);
  
        liquidfx.apply(pg_bodies);
      }
      
      background(BACKGROUND);
      image(pg_particles, 0, 0);
      image(pg_bodies, 0, 0);
      pushMatrix();
      world.applyTransform(this.g);
      world.drawBulletSpawnTrack(this.g);
      popMatrix();
    }
    
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
    if(key == 'g') APPLY_LIQUID_FX = !APPLY_LIQUID_FX;
  }
  
  public void mouseReleased(){
    Vec2 mworld = new Vec2();
    world.transform.getScreen2box(mouseX, mouseY, mworld);
    System.out.println("mouse: "+mouseX+", "+mouseY+", "+mworld);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/BlobTest4.java
  public void initScene() {
    
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;

    Vec2 ankerA = new Vec2();
    Vec2 ankerB = new Vec2();
    
    createChain(ankerA.set(-10, 10), ankerB.set(+15, 10), 0.5f, 0.5f);
    createChain(ankerA.set(-10, 15), ankerB.set(-30, 20), 0.5f, 0.5f);
    createChain(ankerA.set(-24,24), ankerB.set(24,26), 0.5f, 0.5f);
    


    
    emitter0 = new ParticleEmitter(world, world.transform);
    emitter1 = new ParticleEmitter(world, world.transform);
    emitter2 = new ParticleEmitter(world, world.transform);
    emitter3 = new ParticleEmitter(world, world.transform);
    
    
    int flags = 0;
    flags |= ParticleType.b2_waterParticle;
    flags |= ParticleType.b2_viscousParticle;
//    flags |= ParticleType.b2_colorMixingParticle;
    

    float hsb_s = 100;
    float hsb_b = 100;
    colorMode(HSB, 360, 100, 100);
    
    emitter0.setInScreen( 100, 100, 200,  -40, color(  5, hsb_s, hsb_b), flags);
    emitter1.setInScreen( 50,   50, 200,   0, color( 20, hsb_s, hsb_b), flags);
    emitter2.setInScreen(1000, 100, 300, -175, color(115, hsb_s, hsb_b), flags);
    emitter3.setInScreen(1100, 700, 400,  120, color(230, hsb_s, hsb_b), flags);
    
    colorMode(RGB, 255);

  }
  
  
  
  public void createChain(Vec2 ankerA, Vec2 ankerB, float radius, float spacing){
    Vec2 AB = ankerB.sub(ankerA);
    float distance = AB.length();
    
    int count = 1 + (int) (distance / (radius * 2 + spacing));
    float dist = distance / (count-1);
    Vec2 step = AB.mul(dist / distance);

    CircleShape cshape = new CircleShape();
    cshape.m_p.set(0,0);
    cshape.m_radius = radius;
    
    Body body_prev = null;
    for(int i = 0; i < count; i++){
      BodyDef bdef = new BodyDef();
      bdef.position = ankerA.add(step.mul(i));
      
      if(i == 0 || i == count-1 || i % 15 == 0){
        bdef.type = BodyType.STATIC;
      } else {
        bdef.type = BodyType.DYNAMIC;
      }
      
      Body body_curr = world.createBody(bdef);
      body_curr.createFixture(cshape, 0);
      world.bodies.add(body_curr, true, color(255) , true, color(0), 1f);
      
      if(body_prev != null){
        DistanceJointDef djointdef = new DistanceJointDef();      
        djointdef.initialize(body_prev, body_curr, body_prev.m_xf.p, body_curr.m_xf.p);
        djointdef.dampingRatio = 0.3f;
        djointdef.frequencyHz = 15f;
        DistanceJoint djoint = (DistanceJoint) world.createJoint(djointdef);
        world.bodies.add(djoint, true, color(255) , true, color(255), 5);
      }

      body_prev = body_curr;
    }
    
  }
  
  
  ParticleEmitter emitter0;
  ParticleEmitter emitter1;
  ParticleEmitter emitter2;
  ParticleEmitter emitter3;
  
  int particle_counter = 0;
  public void addParticles(){
    
//    emitter2.emit_vel = 25 * (sin(particle_counter/200f + PI) * 0.5f + 0.5f);
//    emitter3.emit_vel = 25 * (sin(particle_counter/200f) * 0.5f  + 0.5f);
    
    if(particle_counter % 1 == 0)
    {
//      emitter0.emitParticles(1);
      emitter1.emitParticles(1);
//      emitter2.emitParticles(1);
      emitter3.emitParticles(1);
    }
    particle_counter++;
  }

 
  
  
  
  
  int body_counter = 0;
  
  int iter = 0;
  public void addBodies(){
    if(iter % 60 == 0){
      addBody();
    }
    iter++;
  }
  
  public void addBody(){
    float b2d_screen_w = world.transform.box2d_dimx;
    float b2d_screen_h = world.transform.box2d_dimy;
    
    float x = random(-0.4f, 0.4f) * b2d_screen_w;
    float y = random(-2f, 2) + b2d_screen_h;
    
    float w = random(0.5f, 2.2f);
    float h = random(0.5f, 1.2f);
    
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(x, y);
    Body body = world.createBody(bd);
    
    Shape shape = null;
    if(random(1) < 0.5){
      PolygonShape pshape = new PolygonShape();
      pshape.setAsBox(w, h, new Vec2(0,0), random(TWO_PI));
      shape = pshape;
    } else {
      CircleShape cshape = new CircleShape();
      cshape.m_p.set(0,0);
      cshape.m_radius = w / 2f;
      shape = cshape;
    }
 
    Fixture fixture = body.createFixture(shape, 2);
    fixture.m_friction = 0.1f;
    fixture.m_restitution = 0.5f;
    
    colorMode(HSB, 360, 100, 100);

    float r = body_counter % 360;
    float g = 100;
    float b = 100;
    
    world.bodies.add(body, true, color(r,g,b), true, color(r, g, b *0.5f), 1);
    colorMode(RGB, 255, 255, 255);
    
    body_counter++;
    
  }
  
  
  
  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_Chain_DistanceJoints_LiquidFx.class.getName() });
  }
  
}