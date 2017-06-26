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
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.ConstantVolumeJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointEdge;
import org.jbox2d.dynamics.joints.JointType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_BlobJoint extends PApplet {

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
    
    world = new DwWorld(this, 20);
    world.transform.setScreen(width, height, 25, width/2, height);
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  
  
  public void draw(){
    
//    world.bodies.addBullet(true, color(128), true, color(0), 1f);
    
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
      world.bodies.display(canvas);
      world.particles.display(canvas);
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

  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/BlobTest4.java
  public void initScene() {
    
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;

    { // boundary
      BodyDef bd = new BodyDef();
      bd.position.set(0.0f, 0.0f);
      Body ground = world.createBody(bd);

      PolygonShape sd = new PolygonShape();
      
      // bottom
      sd.setAsBox(dimx, 0.4f);
      ground.createFixture(sd, 0f);

      // left
      sd.setAsBox(0.4f, dimy, new Vec2(-dimx/2, 0.0f), 0.0f);
      ground.createFixture(sd, 0f);
      
      // right
      sd.setAsBox(0.4f, dimy, new Vec2(+dimx/2, 0.0f), 0.0f);
      ground.createFixture(sd, 0f);
      
      // top
      sd.setAsBox(dimx, 0.4f, new Vec2(0, dimy), 0.0f);
      Fixture ftop = ground.createFixture(sd, 0f);
      
      world.bodies.add(ground, true, color(0), false, color(0), 1f);
    }

    { // falling box
      BodyDef bd2 = new BodyDef();
      bd2.type = BodyType.DYNAMIC;
      PolygonShape psd = new PolygonShape();
      psd.setAsBox(3.0f, 1.5f, new Vec2(0, dimy-2), 0.0f);
      Body fallingBox = world.createBody(bd2);
      
      FixtureDef fd = new FixtureDef();
      fd.shape = psd;
      fd.density = 1.0f;
      fd.restitution = 0.2f;
      fd.friction = 0.1f;
      fallingBox.createFixture(fd);
      
      
      world.bodies.add(fallingBox, true, color(0), false, color(0), 1f);
    }
    
    
    // createBlobJoint(count, radius, posx, posy, radiusx, radiusy,   hue)
    createBlobJoint(20, 0.6f,     0, 10, 5, 5,     0 / 360f);
    createBlobJoint(20, 0.6f,   +15, 16, 5, 5,   170 / 360f);
    createBlobJoint(20, 0.6f,   -15, 20, 5, 5,    80 / 360f);
  }
  
  
  
  void createBlobJoint(int nBodies, float bodyRadius, float cx, float cy, float rx, float ry, float hue_tone){

    ConstantVolumeJointDef cvjd = new ConstantVolumeJointDef();
    Body[] bodylist = new Body[nBodies];
    
    for (int i = 0; i < nBodies; ++i) {
      float angle = map(i, 0, nBodies, 0, TWO_PI);
      float x = cx + rx * (float) Math.sin(angle);
      float y = cy + ry * (float) Math.cos(angle);
      
      BodyDef bd = new BodyDef();
      // bd.isBullet = true;
      bd.fixedRotation = true;
      bd.position.set(new Vec2(x, y));
      bd.type = BodyType.DYNAMIC;
      Body body = world.createBody(bd);
      
      FixtureDef fd = new FixtureDef();
      CircleShape cd = new CircleShape();
      cd.m_radius = bodyRadius;
      fd.shape = cd;
      fd.density = 1.0f;
      body.createFixture(fd);
      
      cvjd.addBody(body);
      
      bodylist[i] = body;
    }

    
    cvjd.frequencyHz = 10.0f;
    cvjd.dampingRatio = 1.0f;
    cvjd.collideConnected = false;
    world.createJoint(cvjd);

    

    colorMode(HSB, 1);
    float hue_range = 60 / 360f;
    
    // create circular body joint shapes
    for (int i = 0; i < nBodies; ++i) {
      
      Body body_cur = bodylist[(i+0) % nBodies];
      Body body_nxt = bodylist[(i+1) % nBodies];
      
      // whatever joints are attached to this body, find the only one, that
      // connects this body with the next body.
      Joint joint = null;
      for(JointEdge jedge = body_cur.m_jointList; jedge != null; jedge = jedge.next){
        if(jedge.other == body_nxt){
          // only distance joints are useful to display
          if(jedge.joint.getType() == JointType.DISTANCE){
            joint = jedge.joint;
            break;
          }
        }
      }
      
      // check if a joint was found
      if(joint != null){
        float inorm =  i / (float)nBodies;
        float hue = abs(2 * inorm - 1) * hue_range + hue_tone;
        hue = hue - (int)hue; // [0, 1]
        
        world.bodies.add(joint, false, color(255), true, color(hue, 1, 0.5f), 5);
      }
    }
    
    // create shapes for bodies
    for (int i = 0; i < nBodies; ++i) {
      
      float inorm =  i / (float)nBodies;
      float hue = abs(2 * inorm - 1) * hue_range + hue_tone;
      hue = hue - (int)hue; // [0, 1]
      
      world.bodies.add(bodylist[i], true, color(hue, 1, 1), false, color(0), 1f);
    }
    colorMode(RGB, 255);

  }

 
  
  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_BlobJoint.class.getName() });
  }
  
}