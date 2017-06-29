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
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;
import com.thomasdiewald.pixelflow.java.sampling.DwSampling;

import java.util.Random;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_Scene extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;
  boolean APPLY_LIQUID_FX = true;

  DwWorld world;  
//  PImage sprite;

  
  DwPixelFlow pixelflow;
  DwLiquidFX liquidfx;
  
  PGraphics2D pg_particles;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
//    sprite = loadImage("sprite.png");
    
    
    pixelflow = new DwPixelFlow(this);
    liquidfx = new DwLiquidFX(pixelflow);
    
    pg_particles = (PGraphics2D) createGraphics(width, height, P2D);
    
    
    
    reset();
//    frameRate(1000);
    frameRate(120);
  }
  
  
  public void release(){
    if(world != null) world.release(); world = null;
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this, 20);
    
//    world.particles.param.falloff_exp1 = 3;
//    world.particles.param.falloff_exp2 = 1;
//    world.particles.param.radius_scale = 2;
    
    world.particles.param.falloff_exp1 = 1;
    world.particles.param.falloff_exp2 = 2;
    world.particles.param.radius_scale = 4;

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  

  public void draw(){
    
    if(UPDATE_PHYSICS){
      wave();
//      if(frameCount % 2 == 0){
      for(int i = 0; i < 5; i++){
        addParticles();
      }
//      }
      world.update();
    }
    
    int BACKGROUND = 32;

    if(USE_DEBUG_DRAW){
      PGraphics2D canvas = (PGraphics2D) this.g;
      
      canvas.background(BACKGROUND);
      canvas.pushMatrix();
      world.applyTransform(canvas);
      world.drawBulletSpawnTrack(canvas);
      world.displayDebugDraw(canvas);
      canvas.popMatrix();
    } else {
      PGraphics2D canvas = (PGraphics2D) pg_particles;

      canvas.beginDraw();
      canvas.clear();
      canvas.background(BACKGROUND, 0);
      world.applyTransform(canvas);
//      world.bodies.display(canvas);
      world.particles.display(canvas, 0);
      canvas.endDraw();
      
      if(APPLY_LIQUID_FX)
      {
        liquidfx.param.base_LoD = 0;
        liquidfx.param.base_blur_radius = 2;
        liquidfx.param.base_threshold = 0.7f;
        liquidfx.param.highlight_enabled = true;
        liquidfx.param.highlight_LoD = 1;
        liquidfx.param.highlight_decay = 0.6f;
        liquidfx.param.sss_enabled = !true;
        liquidfx.param.sss_LoD = 4;
        liquidfx.param.sss_decay = 0.8f;
        liquidfx.apply(canvas);
      }
      
      background(BACKGROUND);
      image(canvas, 0, 0);
      pushMatrix();
      world.applyTransform(this.g);
      world.bodies.display((PGraphics2D) this.g);
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
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
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
  
  RevoluteJoint m_joint;
  float m_time;
  
  
  public void wave() {
//    m_time += 1 / 120f;
//    m_joint.setMotorSpeed(0.1f * MathUtils.cos(m_time) * MathUtils.PI);
  }
 
  public void initScene() {
    
    
    float screenscale =  world.transform.screen_scale;
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    float thick = 10 / screenscale;
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);
      
//      bd.type = BodyType.DYNAMIC;
//      bd.allowSleep = false;
//      bd.position.set(0.0f, 0.0f);
//      Body body = world.createBody(bd);

//      PolygonShape shape = new PolygonShape();
//      shape.setAsBox(1, 9.5f, new Vec2(19.0f, 0.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(1, 9.5f, new Vec2(-19.0f, 0.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(20.0f, 1, new Vec2(0.0f, 11.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      shape.setAsBox(20.0f, 1, new Vec2(0.0f, -11.0f), 0.0f);
//      body.createFixture(shape, 5.0f);
//      world.bodies.add(body, true, color(0), true, color(0), 1f);
//      
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(dimx/4, thick/2, new Vec2(0, 4), 10 * PI/180);
      ground.createFixture(shape, 0);
      
      shape.setAsBox(dimx/6, thick/2, new Vec2(-20, 8), -15 * PI/180);
      ground.createFixture(shape, 0);
      
      
      world.bodies.add(ground, true, color(160), true, color(0), 1f);
    }
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-14, 24);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 5.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(5, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = 0.1f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = !true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-4, 22);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 5.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(5, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = -0.2f * MathUtils.PI;
      jd.maxMotorTorque = 1e7f;
      jd.enableMotor = !true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    
    
    
    
    
    
    {
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(-2,3);
      Body body = world.createBody(bd);
    
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(0.5f, 8.0f);
      body.createFixture(shape, 1);
      
      shape.setAsBox(8, 0.5f);
      body.createFixture(shape, 0.5f);
      
      world.bodies.add(body, true, color(220), true, color(0), 1f);
      
      RevoluteJointDef jd = new RevoluteJointDef();
      jd.bodyA = ground;
      jd.bodyB = body;
      jd.localAnchorA.set(bd.position);
      jd.localAnchorB.set(0.0f, 0.0f);
      jd.referenceAngle = 0.0f;
      jd.motorSpeed = -0.2f * MathUtils.PI;
      jd.maxMotorTorque = 100000;
      jd.enableMotor = true;
      m_joint = (RevoluteJoint) world.createJoint(jd);
    }
    
    
    

    
    world.setParticleRadius(0.15f);
    world.setParticleDamping(0.6f);

    {
      ParticleGroupDef pgroup_def = new ParticleGroupDef();
      pgroup_def.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
      pgroup_def.setColor(new Color3f(1, 0.15f, 0.05f));
      pgroup_def.linearVelocity.set(0,0);
      pgroup_def.angularVelocity = 0f;
      
      PolygonShape shape = new PolygonShape();
      shape.setAsBox(17.0f, 9.0f, new Vec2(0.0f, dimy/2), 0.0f);

      pgroup_def.shape = shape;
      world.createParticleGroup(pgroup_def);
    }

    m_time = 0;
    
    int flags = 0;
    flags |= ParticleType.b2_waterParticle;
    flags |= ParticleType.b2_viscousParticle;
//    flags |= ParticleType.b2_colorMixingParticle;
    
    emitter0 = new ParticleEmitter(world, 800, 100);
    emitter0.emit_angle = -170 * TO_RAD;
    emitter0.emit_velocity = 15;
    emitter0.pdef.color.set(color(32,255,0));
    emitter0.pdef.flags = flags;
    
    emitter1 = new ParticleEmitter(world, 100, 100);
    emitter1.emit_angle = 0;
    emitter1.emit_velocity = 10;
    emitter1.pdef.color.set(color(255,16,0));
    emitter1.pdef.flags = flags;
    
    emitter2 = new ParticleEmitter(world, 131, 315);
    emitter2.emit_angle = +40 * TO_RAD;
    emitter2.emit_velocity = 15;
    emitter2.pdef.color.set(color(255,64,0));
    emitter2.pdef.flags = flags;
  }
  
  
  ParticleEmitter emitter0;
  ParticleEmitter emitter1;
  ParticleEmitter emitter2;

  int counter = 0;
  public void addParticles(){
    if(counter % 2 == 0){
      emitter0.emitParticles(1);
      emitter1.emitParticles(1);
      emitter2.emitParticles(1);
    }
    counter++;
  }

  
  static final float TO_RAD = (float) (Math.PI / 180.0);
  
  
  
  
  static class ParticleEmitter {
    public PApplet papplet;
    public DwWorld world;

    public ParticleDef pdef = new ParticleDef();
    
    public float emit_angle = (float) (Math.PI * 0.5f);
    public float emit_velocity = 20;
    
    public float mult_jitter_radius = 1.0f; // radius, world-space
    public float mult_jitter_velmag = 0.5f; // noise mult
    public float mult_jitter_velrot = 0.2f; // [0, PI*2]


    protected Random rand = new Random();
    protected int counter = 0;
    
    protected Vec2 emit_pos_screen = new Vec2();
    protected Vec2 emit_pos_world = new Vec2();
    
    
    public ParticleEmitter(DwWorld world, float screen_x, float screen_y){
      this.papplet = world.papplet;
      this.world = world;
      
      pdef.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
      pdef.setColor(new Color3f(1, 0.15f, 0.05f));
      
      setPosition(screen_x, screen_y);
    }

    
    public void emitParticles(int count){
      for(int i = 0; i < count; i++){
        emitSingleParticle(i);
      }
    }

    public void setPosition(float screen_x, float screen_y){
      world.transform.getScreen2box(screen_x, screen_y, emit_pos_world);
    }
    
    protected void emitSingleParticle(int idx){

      // velocity (noise)
      float srandnoise = papplet.noise(counter / 100f) * 2 - 1;
      
      float rot_angle = emit_angle     + srandnoise * mult_jitter_velrot;
      float vel_mag   = emit_velocity  + srandnoise * mult_jitter_velmag;
      
      float vel_x = (float) (Math.cos(rot_angle) * vel_mag);
      float vel_y = (float) (Math.sin(rot_angle) * vel_mag);
      
      // position
      float[] jitter = DwSampling.sampleDisk_Halton(counter, 0.5f);

      float pos_x = emit_pos_world.x + jitter[0] * mult_jitter_radius;
      float pos_y = emit_pos_world.y + jitter[1] * mult_jitter_radius;


      // create Particle
      pdef.position.set(pos_x, pos_y);
      pdef.velocity.set(vel_x, vel_y);
      world.createParticle(pdef);
      
      counter++;
    }
    
    
  }


  

  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_Scene.class.getName() });
  }
  
}