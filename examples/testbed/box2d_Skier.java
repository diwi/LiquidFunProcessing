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
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.*;
import processing.opengl.PGraphics2D;


public class box2d_Skier extends PApplet {

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
    
    world = new DwWorld(this, 20);
    world.transform.setScreen(width, height, 20, width - 200, height/2);
    
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
    canvas.background(255);
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
  
  float m_platform_width;
  Body m_skier;

  // https://github.com/erincatto/Box2D/blob/master/Box2D/Testbed/Tests/Skier.h
  public void initScene() { 
    
    Body ground = null;
    {
      BodyDef bd = new BodyDef();
      ground = world.createBody(bd);
      float PlatformWidth = 8.0f;
      

      /*
      First angle is from the horizontal and should be negative for a downward slope.
      Second angle is relative to the preceding slope, and should be positive, creating a kind of
      loose 'Z'-shape from the 3 edges.
      If A1 = -10, then A2 <= ~1.5 will result in the collision glitch.
      If A1 = -30, then A2 <= ~10.0 will result in the glitch.
      */
      float Angle1Degrees = -30.0f;
      float Angle2Degrees = 10.0f;
      
      /*
      The larger the value of SlopeLength, the less likely the glitch will show up.
      */
      float SlopeLength = 20.0f;

      float SurfaceFriction = 0.2f;

      // Convert to radians
      float Slope1Incline = -Angle1Degrees * PI / 180.0f;
      float Slope2Incline = Slope1Incline - Angle2Degrees * PI / 180.0f;
      //

      m_platform_width = PlatformWidth;
      
      Vec2[] verts = new Vec2[4];
      verts[0] = new Vec2(-PlatformWidth, 0.0f);
      verts[1] = new Vec2(0.0f, 0.0f);
      verts[2] = new Vec2(  verts[1].x + SlopeLength * cos(Slope1Incline),
                            verts[1].y - SlopeLength * sin(Slope1Incline)  );
      verts[3] = new Vec2(  verts[2].x + SlopeLength * cos(Slope2Incline),
                            verts[2].y - SlopeLength * sin(Slope2Incline)  );
      

      {
        EdgeShape shape = new EdgeShape();
        shape.set(verts[0], verts[1]);
        shape.m_hasVertex3 = true;
        shape.m_vertex3.set(verts[2]);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.0f;
        fd.friction = SurfaceFriction;

        ground.createFixture(fd);
      }

      {
        EdgeShape shape = new EdgeShape();
        shape.set(verts[1], verts[2]);
        shape.m_hasVertex0 = true;
        shape.m_hasVertex3 = true;
        shape.m_vertex0.set(verts[0]);
        shape.m_vertex3.set(verts[3]);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.0f;
        fd.friction = SurfaceFriction;

        ground.createFixture(fd);
      }
      
      {
        EdgeShape shape = new EdgeShape();
        shape.set(verts[2], verts[3]);
        shape.m_hasVertex0 = true;
        shape.m_vertex0.set(verts[1]);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.0f;
        fd.friction = SurfaceFriction;

        ground.createFixture(fd);
      }
    }

    {
      boolean EnableCircularSkiTips = false;

      float BodyWidth = 1.0f;
      float BodyHeight = 2.5f;
      float SkiLength = 3.0f;
      
      //Larger values for this seem to alleviate the issue to some extent.
      float SkiThickness = 0.3f;
      float SkiFriction = 0.0f;
      float SkiRestitution = 0.15f;

      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;

      float initial_y = BodyHeight / 2 + SkiThickness;
      if(EnableCircularSkiTips)
      {
        initial_y += SkiThickness / 6;
      }
      bd.position.set(-m_platform_width / 2, initial_y);

      Body skier = world.createBody(bd);

      PolygonShape body = new PolygonShape();;
      body.setAsBox(BodyWidth / 2, BodyHeight / 2);


      Vec2[] verts = new Vec2[4];
      verts[0] = new Vec2(-SkiLength / 2 - SkiThickness, -BodyHeight / 2);
      verts[1] = new Vec2(-SkiLength / 2, -BodyHeight / 2 - SkiThickness);
      verts[2] = new Vec2( SkiLength / 2, -BodyHeight / 2 - SkiThickness);
      verts[3] = new Vec2( SkiLength / 2 + SkiThickness, -BodyHeight / 2);
      
      PolygonShape ski = new PolygonShape();
      ski.set(verts, verts.length);

      CircleShape ski_back_shape = new CircleShape();
      ski_back_shape.m_p.set(-SkiLength / 2.0f, -BodyHeight / 2 - SkiThickness * (2.0f / 3));
      ski_back_shape.m_radius = SkiThickness / 2;

      CircleShape ski_front_shape = new CircleShape();
      ski_front_shape.m_p.set(SkiLength / 2, -BodyHeight / 2 - SkiThickness * (2.0f / 3));
      ski_front_shape.m_radius = SkiThickness / 2;

      FixtureDef fd = new FixtureDef();
      fd.shape = body;
      fd.density = 1.0f;
      skier.createFixture(fd);

      fd.friction = SkiFriction;
      fd.restitution = SkiRestitution;

      fd.shape = ski;
      skier.createFixture(fd);

      if(EnableCircularSkiTips)
      {
        fd.shape = ski_back_shape;
        skier.createFixture(fd);

        fd.shape = ski_front_shape;
        skier.createFixture(fd);
      }

      skier.setLinearVelocity(new Vec2(0.5f, 0.0f));

      m_skier = skier;
    }

    bodies.createAll();
  }


   
  public static void main(String args[]) {
    PApplet.main(new String[] { box2d_Skier.class.getName() });
  }
  
}