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





package com.thomasdiewald.liquidfun.java.render;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.particle.ParticleColor;

import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.DwWorld;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;




/**
 * 
 * Box2D debug draw, utilizing processings basic draw calls.<br>
 * Very useful to test things, but not optimized for performance.<br>
 * 
 * @author Thomas Diewald
 *
 */
public class DwDebugDraw extends DebugDraw{
  
  
  public PApplet papplet;
  public PGraphics canvas;
  public PFont font;
  public World world;

  public DwViewportTransform transform;
  
  public float stroke_weight = 1f;
  
  public static PImage PARTICLE_SPRITE;
  public static float  PARTICLE_RADIUS_SCALE = 1f;
  
  
  
  public DwDebugDraw(PApplet papplet, World world, DwViewportTransform transform){
    this(papplet, world, transform, papplet.g);
  }
  
  public DwDebugDraw(PApplet papplet, World world, DwViewportTransform transform, PGraphics canvas){
    super(transform);
    this.papplet = papplet;
    this.canvas = canvas;
    this.transform = transform;
    this.world = world;
    
    // default flags
    this.m_drawFlags = 0;
    this.m_drawFlags |= DwDebugDraw.e_shapeBit;            // m_bodyList
//    this.m_drawFlags |= DwDebugDraw.e_wireframeDrawingBit; // m_bodyList
//    this.m_drawFlags |= DwDebugDraw.e_aabbBit;             // m_bodyList AABB
    this.m_drawFlags |= DwDebugDraw.e_centerOfMassBit;     // m_bodyList xf
    this.m_drawFlags |= DwDebugDraw.e_pairBit;             // m_contactManager
    this.m_drawFlags |= DwDebugDraw.e_jointBit;            // m_jointList
//    this.m_drawFlags |= DwDebugDraw.e_dynamicTreeBit;    // m_contactManager

    setStrokeWeight(1f);
    
    // Font for rendering Strings
    font = papplet.createFont("Calibri", 12);
    
    // Sprite for rendering Particles
    float particle_rad = world.getParticleRadius() * 2 * PARTICLE_RADIUS_SCALE;
    int radius_screen = (int) (Math.ceil(particle_rad * transform.screen_scale) * 2);
    PARTICLE_SPRITE = DwUtils.createSprite(papplet, radius_screen, 2, 1, 1);
    world.setDebugDraw(this);
  }
  
  
  public void display(PGraphics canvas){
    setCanvas(canvas);
    world.setDebugDraw(this);
    world.drawDebugData();
  }
  
  
  public void setCanvas(PGraphics canvas){
    this.canvas = canvas;
  }

  public void setStrokeWeight(float sw){
    stroke_weight = 1f / transform.screen_scale;
  }

  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // inherited debug draw functions
  //
  //////////////////////////////////////////////////////////////////////////////
  
  
  
  
  @Override
  public void drawPoint(Vec2 center, float radius, Color3f color) {
    canvas.noStroke();
    canvas.fill(color.x * 255f, color.y * 255f, color.z * 255f);
    canvas.ellipse(center.x, center.y, radius*2, radius*2);
  }

  @Override
  public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
    // specifying the type can speed up things a bit
    canvas.beginShape(getPolygonShapeType(vertexCount));
    canvas.strokeWeight(stroke_weight);
    canvas.stroke(0);
    canvas.fill(color.x * 255f, color.y * 255f, color.z * 255f);
    for(int i = 0; i < vertexCount; i++){
      Vec2 pos = vertices[i];
      canvas.vertex(pos.x, pos.y);
    }
    canvas.endShape(PConstants.CLOSE);
  }

  @Override
  public void drawCircle(Vec2 center, float radius, Color3f color) {
    canvas.strokeWeight(stroke_weight);
    canvas.stroke(color.x * 255f, color.y * 255f, color.z * 255f);
    canvas.noFill();
    canvas.ellipse(center.x, center.y, radius*2, radius*2);
  }

  @Override
  public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
    canvas.strokeWeight(stroke_weight);
    canvas.stroke(0);
    canvas.fill(color.x * 255f, color.y * 255f, color.z * 255f);
    canvas.ellipse(center.x, center.y, radius*2, radius*2);
  }

  @Override
  public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
    canvas.strokeWeight(stroke_weight);
    canvas.stroke(color.x * 255f, color.y * 255f, color.z * 255f);
    canvas.line(p1.x, p1.y, p2.x, p2.y);
  }
  
  
  
  public float transform_axis_size = 1f;
  protected Vec2 temp1 = new Vec2();
  protected Vec2 temp2 = new Vec2();
  protected Vec2 temp3 = new Vec2();
  

  @Override
  public void drawTransform(Transform xf) {
    temp2.x = xf.p.x + xf.q.c * transform_axis_size;
    temp2.y = xf.p.y + xf.q.s * transform_axis_size;

    temp3.x = xf.p.x - xf.q.s * transform_axis_size;
    temp3.y = xf.p.y + xf.q.c * transform_axis_size;

    drawSegment(xf.p, temp2, Color3f.RED);
    drawSegment(xf.p, temp3, Color3f.GREEN);
    drawPoint(xf.p, 1.5f/transform.screen_scale, Color3f.BLACK);
  }
  

  public boolean DRAW_STRING = !false;

  @Override
  public void drawString(float x, float y, String s, Color3f color) {
    if(DRAW_STRING){
      canvas.pushMatrix();
      canvas.applyMatrix(transform.mat_screen2box);
      canvas.textFont(font);
      canvas.textAlign(PConstants.RIGHT);
      canvas.fill(color.x * 255f, color.y * 255f, color.z * 255f);
      canvas.text(s, x, y-2);
      canvas.popMatrix();
    }
//    System.out.println("DwDebugDraw.drawString: "+s);
  }

  

  @Override
  public void drawParticles(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
    
    radius *= PARTICLE_RADIUS_SCALE;
    
    canvas.noStroke();
    canvas.noFill();
    canvas.beginShape(PConstants.QUADS);
    canvas.textureMode(PConstants.NORMAL);
    canvas.texture(PARTICLE_SPRITE);
    for(int i = 0; i < count; i++){
      Vec2 pos = centers[i];
      if(colors != null){
        ParticleColor col = colors[i];
        canvas.tint(col.r & 0xFF, col.g & 0xFF, col.b & 0xFF, col.a & 0xFF);
      }
      canvas.vertex(pos.x - radius, pos.y - radius, 0, 0);
      canvas.vertex(pos.x + radius, pos.y - radius, 1, 0);
      canvas.vertex(pos.x + radius, pos.y + radius, 1, 1);
      canvas.vertex(pos.x - radius, pos.y + radius, 0, 1);
      
    }
    canvas.endShape();
    canvas.tint(255);
  }

  @Override
  public void drawParticlesWireframe(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
    canvas.noFill();
    canvas.strokeWeight(stroke_weight);
    canvas.stroke(0);
    for(int i = 0; i < count; i++){
      Vec2 pos = centers[i];
      if(colors != null){
        ParticleColor col = colors[i];
        canvas.stroke(col.r & 0xFF, col.g & 0xFF, col.b & 0xFF, col.a & 0xFF);
      }
      canvas.ellipse(pos.x, pos.y, radius*2, radius*2);
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  


  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // static debug draw functions
  //
  //////////////////////////////////////////////////////////////////////////////

  static public void display(PGraphics pg, DwWorld world){
    display(pg, world, true, true, true);
  }
  
  
  static public void display(PGraphics pg, DwWorld world, boolean bodies, boolean particles, boolean joints){
    pg.fill(200);
    pg.tint(255);
    pg.stroke(0);
    pg.strokeWeight(1f/world.transform.screen_scale);

    if(bodies   ) displayBodies   (pg, world);
    if(particles) displayParticles(pg, world);
    if(joints   ) displayJoints   (pg, world);
  }
  
  



  static public void displayBodies(PGraphics pg, World world){
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      display(pg, body);
    }
  }
  

  static public void displayJoints(PGraphics pg, World world){
    pg.beginShape(PConstants.LINES);
    for (Joint joint = world.getJointList(); joint != null; joint = joint.getNext()) {
      display(pg, joint);
    }
    pg.endShape();
  }

  
  static public void displayParticles(PGraphics pg, World world) {
    int particle_num = world.getParticleCount();
    if (particle_num != 0) {
      float radius = world.getParticleRadius();
      radius *= PARTICLE_RADIUS_SCALE;
      Vec2[] particle_pos = world.getParticlePositionBuffer();
      pg.beginShape(PConstants.QUADS);
      pg.noFill();
      pg.noStroke();
      pg.textureMode(PConstants.NORMAL);
      pg.texture(PARTICLE_SPRITE);
      for (int i = 0; i < particle_num; i++) {
        Vec2 pos = particle_pos[i];
        pg.vertex(pos.x - radius, pos.y - radius, 0, 0);
        pg.vertex(pos.x + radius, pos.y - radius, 1, 0);
        pg.vertex(pos.x + radius, pos.y + radius, 1, 1);
        pg.vertex(pos.x - radius, pos.y + radius, 0, 1);
      }
      pg.endShape();
    }
  }
  
  
  
  
  static public void display(PGraphics pg, Body body){
    for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
      display(pg, fixture);
//      if (body.isActive() == false) {
//        display(pg, fixture);
//      } else if (body.getType() == BodyType.STATIC) {
//        display(pg, fixture);
//      } else if (body.getType() == BodyType.KINEMATIC) {
//        display(pg, fixture);
//      } else if (body.isAwake() == false) {
//        display(pg, fixture);
//      } else {
//        display(pg, fixture);
//      }
    }
  }
  
  
 
  static public void display(PGraphics pg, Fixture fixture){
    
    Body body = fixture.getBody();
    Transform xf = body.getTransform();
    Vec2 verts_t = new Vec2();

    switch(fixture.getType()){
      case POLYGON: {
        PolygonShape shp_polygon = (PolygonShape) fixture.getShape();
        Vec2[] verts = shp_polygon.m_vertices;
        pg.beginShape(getPolygonShapeType(shp_polygon.m_count));
        for(int i = 0; i < shp_polygon.m_count; i++){
          Transform.mulToOutUnsafe(xf, verts[i], verts_t);
          pg.vertex(verts_t.x, verts_t.y);
        }
        pg.endShape(PConstants.CLOSE);
        break;
      }
      case CIRCLE: {
        CircleShape shp_circle = (CircleShape) fixture.getShape();
        float radius = shp_circle.m_radius;
        Transform.mulToOutUnsafe(xf, shp_circle.m_p, verts_t);
        pg.ellipse(verts_t.x, verts_t.y, radius*2, radius*2);
        break;
      }
      case CHAIN: {
        ChainShape shp_chain = (ChainShape) fixture.getShape();
        Vec2[] verts = shp_chain.m_vertices;
        pg.beginShape(getPolygonShapeType(shp_chain.m_count));
        pg.noFill();
        for(int i = 0; i < shp_chain.m_count; i++){
          Transform.mulToOutUnsafe(xf, verts[i], verts_t);
          pg.vertex(verts_t.x, verts_t.y);
        }
        pg.endShape(PConstants.CLOSE);
        break;
      }
      case EDGE: {
        EdgeShape shp_edge = (EdgeShape) fixture.getShape();
        Vec2 vert1 = shp_edge.m_vertex1;
        Vec2 vert2 = shp_edge.m_vertex2;
        pg.line(vert1.x, vert1.y, vert2.x, vert2.y);
      } 
      default: break;
    }
  }

  
  
  static public void display(PGraphics pg, Joint joint){
    Body bodyA = joint.getBodyA();
    Body bodyB = joint.getBodyB();
    Transform xfA = bodyA.getTransform();
    Transform xfB = bodyB.getTransform();
    Vec2 posA = xfA.p;
    Vec2 posB = xfB.p;
    Vec2 ancA = new Vec2();
    Vec2 ancB = new Vec2();
    joint.getAnchorA(ancA);
    joint.getAnchorB(ancB);

    JointType type = joint.getType();
    
    switch (type) {

      case DISTANCE:
        line(pg, ancA, ancB);
        break;

      case PULLEY: 
        PulleyJoint pulley = (PulleyJoint) joint;
        Vec2 gancA = pulley.getGroundAnchorA();
        Vec2 gancB = pulley.getGroundAnchorB();
        line(pg, gancA, ancA);
        line(pg, gancB, ancB);
        line(pg, gancA, gancB);
        break;
        
      case CONSTANT_VOLUME:
        line(pg, ancA, ancB);
        break;
        
      case MOUSE:
        line(pg, ancA, ancB);
        break;
        
      default:
        line(pg, posA, ancA);
        line(pg, ancA, ancB);
        line(pg, posB, ancB);
    }
  }
  
  
  static public void line(PGraphics pg, Vec2 p1, Vec2 p2){
    pg.vertex(p1.x, p1.y);
    pg.vertex(p2.x, p2.y);
  }
  
  
  static public int getPolygonShapeType(int vertex_count){
    int type = PConstants.POLYGON;
    switch(vertex_count){
      case 1:  type = PConstants.POINTS   ; break;
      case 2:  type = PConstants.LINES    ; break;
      case 3:  type = PConstants.TRIANGLES; break;
      case 4:  type = PConstants.QUADS    ; break;
      default: type = PConstants.POLYGON  ; break;
    }
    return type;
  }
  

}
