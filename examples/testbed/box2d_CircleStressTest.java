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


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.render.DwBodyRenderP5;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.render.DwJointRenderP5;

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PGraphics2D;


public class box2d_CircleStressTest extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  World world;
  DwViewportTransform transform;
  
  DwDebugDraw debugdraw;
  DwBodyRenderP5 bodyrenderer;
  DwJointRenderP5 jointrenderer;
  
  DwMouseDragBodies mouse_drag_bodies;
  
  
  PFont font;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    font = createFont("SourceCodePro-Regular.ttf", 12);
    reset();
    frameRate(120);
  }
  
  
  public void release(){
    if(bodyrenderer != null){
      bodyrenderer.release();
      bodyrenderer = null;
    }
    if(jointrenderer != null){
      jointrenderer.release();
      jointrenderer = null;
    }
    
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    // Box2d world
    world = new World(new Vec2(0, -50f));
    
    // particle settings
    world.setParticleGravityScale(0.4f);
    world.setParticleDensity(1.2f);
    world.setParticleDamping(1.0f);
    world.setParticleRadius(0.25f);
    
    // Transformation: world <-> screen
    transform = new DwViewportTransform(this);
    transform.setScreen(width, height, 10, width/2, height-10);

    // Renderer
    debugdraw = new DwDebugDraw(this, world, transform);
    bodyrenderer = new DwBodyRenderP5(this, world, transform);
    jointrenderer = new DwJointRenderP5(this, world, transform);
    // mouse interaction
    mouse_drag_bodies = new DwMouseDragBodies(world, transform);

    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  


  //////////////////////////////////////////////////////////////////////////////
  // draw
  //////////////////////////////////////////////////////////////////////////////
  
  
  public void draw(){
    
    if(UPDATE_PHYSICS){
      mouseDrawAction();
      world.step(1f/60, 8, 4);
      bodyrenderer.update();
      jointrenderer.update();
    }
    
    PGraphics2D canvas = (PGraphics2D) this.g;
    
    canvas.pushMatrix();
    canvas.applyMatrix(transform.mat_box2screen);
    canvas.background(255);
    canvas.fill(200);
    canvas.tint(255);
    canvas.stroke(0);
    canvas.strokeWeight(1f/transform.screen_scale);

//    DwDebugDraw.displayBodies   (canvas, world);
//    DwDebugDraw.displayParticles(canvas, world);
//    DwDebugDraw.displayJoints   (canvas, world);

    if(USE_DEBUG_DRAW){
      debugdraw.display(canvas);
    } else {
      jointrenderer.display(canvas);
      bodyrenderer.display(canvas);
    }
    canvas.popMatrix();
    
    int tx = 16;
    int ty = 16;
    int gy = 14;
    textFont(font);
    fill(0);
    text("'0-9' motor speed", tx, ty+=gy);
    text("'r'   reset"      , tx, ty+=gy);
    text("'t'   pause"      , tx, ty+=gy);
    text("'f'   debugdraw"  , tx, ty+=gy);
  
    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  

  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////

  public void mousePressed() {
    if(mouseButton == LEFT){
      mouse_drag_bodies.press(mouseX, mouseY);
    }
  }

  public void mouseDrawAction(){
    mouse_drag_bodies   .update(mouseX, mouseY);
  }
  
  public void mouseDragged() {
  }
  
  public void mouseReleased() {
    mouse_drag_bodies   .release(mouseX, mouseY);
  }
  
  public void keyReleased(){
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
    if(key >= '0' && key <= '9') joint.setMotorSpeed(PI * (key - '0'));
  }

  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////


  private RevoluteJoint joint;
  
  // https://github.com/jbox2d/jbox2d/blob/master/jbox2d-testbed/src/main/java/org/jbox2d/testbed/tests/CircleStress.java
  public void initScene() {

    Body ground = world.createBody(new BodyDef());
    

    {
      Body bwall_L;
      Body bwall_R;
      Body bwall_B;
      Body bwall_T;
      Body bchamfer_L;
      Body bchamfer_R;
      
      // Ground
      PolygonShape sd = new PolygonShape();
      sd.setAsBox(50.0f, 10.0f);
      BodyDef bd = new BodyDef();
      bd.type = BodyType.STATIC;
      bd.position = new Vec2(0.0f, -10.0f);
      bwall_B = world.createBody(bd);
      FixtureDef fd = new FixtureDef();
      fd.shape = sd;
      fd.friction = 1.0f;
      bwall_B.createFixture(fd);

      // Walls
      sd.setAsBox(3.0f, 50.0f);
      bd = new BodyDef();
      bd.position = new Vec2(45.0f, 25.0f);
      bwall_R = world.createBody(bd);
      bwall_R.createFixture(sd, 0);
      bd.position = new Vec2(-45.0f, 25.0f);
      bwall_L = world.createBody(bd);
      bwall_L.createFixture(sd, 0);

      // Corners
      bd = new BodyDef();
      sd.setAsBox(20.0f, 3.0f);
      bd.angle = (float) (-Math.PI / 4.0);
      bd.position = new Vec2(-35f, 8.0f);
      bchamfer_L = world.createBody(bd);
      bchamfer_L.createFixture(sd, 0);
      bd.angle = (float) (Math.PI / 4.0);
      bd.position = new Vec2(35f, 8.0f);
      bchamfer_R = world.createBody(bd);
      bchamfer_R.createFixture(sd, 0);

      // top
      sd.setAsBox(50.0f, 10.0f);
      bd.type = BodyType.STATIC;
      bd.angle = 0;
      bd.position = new Vec2(0.0f, 75.0f);
      bwall_T = world.createBody(bd);
      fd.shape = sd;
      fd.friction = 1.0f;
      bwall_T.createFixture(fd);
      
      
      bodyrenderer.createShape(bwall_L);
      bodyrenderer.styleShape (bwall_L, true, color(32), true, color(0), 1f);
      bodyrenderer.createShape(bwall_R);
      bodyrenderer.styleShape (bwall_R, true, color(32), true, color(0), 1f);
      
      bodyrenderer.createShape(bwall_B);
      bodyrenderer.styleShape (bwall_B, true, color(32), true, color(0), 1f);
      bodyrenderer.createShape(bwall_T);
      bodyrenderer.styleShape (bwall_T, true, color(32), true, color(0), 1f);
      
      bodyrenderer.createShape(bchamfer_L);
      bodyrenderer.styleShape (bchamfer_L, true, color(32), true, color(0), 1f);
      bodyrenderer.createShape(bchamfer_R);
      bodyrenderer.styleShape (bchamfer_R, true, color(32), true, color(0), 1f);
    }

    CircleShape cd;
    FixtureDef fd = new FixtureDef();

    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    int numPieces = 5;
    float radius = 6f;
    bd.position = new Vec2(0.0f, 10.0f);
    Body body = world.createBody(bd);
    for (int i = 0; i < numPieces; i++) {
      cd = new CircleShape();
      cd.m_radius = 1.2f;
      fd.shape = cd;
      fd.density = 25;
      fd.friction = .1f;
      fd.restitution = .9f;
      float xPos = radius * (float) Math.cos(2f * Math.PI * (i / (float) (numPieces)));
      float yPos = radius * (float) Math.sin(2f * Math.PI * (i / (float) (numPieces)));
      cd.m_p.set(xPos, yPos);

      body.createFixture(fd);
    }

    body.setBullet(false);

    RevoluteJointDef rjd = new RevoluteJointDef();
    rjd.initialize(body, ground, body.getPosition());
    rjd.motorSpeed = MathUtils.PI * 2;
    rjd.maxMotorTorque = 1000000.0f;
    rjd.enableMotor = true;
    joint = (RevoluteJoint) world.createJoint(rjd);
    
    bodyrenderer.createShape(body);
    bodyrenderer.styleShape (body, true, color(255,64,32), true, color(0), 1f);
    

    {
      int loadSize = 41;

      for (int j = 0; j < 15; j++) {
        for (int i = 0; i < loadSize; i++) {
          CircleShape circ = new CircleShape();
          BodyDef bod = new BodyDef();
          bod.type = BodyType.DYNAMIC;
          circ.m_radius = 1.0f + (i % 2 == 0 ? 1.0f : -1.0f) * .5f * MathUtils.randomFloat(.5f, 1f);
          FixtureDef fd2 = new FixtureDef();
          fd2.shape = circ;
          fd2.density = circ.m_radius * 1.5f;
          fd2.friction = 0.5f;
          fd2.restitution = 0.7f;
          float xPos = -39f + 2 * i;
          float yPos = 50f + j;
          bod.position = new Vec2(xPos, yPos);
          Body myBody = world.createBody(bod);
          myBody.createFixture(fd2);

          bodyrenderer.createShape(myBody);
          bodyrenderer.styleShape (myBody, true, color(32,128,255,180), true, color(0), 1f);
        }
      }

    }

    bodyrenderer.createShape();
//    jointrenderer.createShape();
  
  }
  
  
 
   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_CircleStressTest.class.getName() });
  }
  
}