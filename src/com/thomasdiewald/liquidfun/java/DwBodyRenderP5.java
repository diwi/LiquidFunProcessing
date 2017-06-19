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





package com.thomasdiewald.liquidfun.java;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.opengl.PGraphics2D;




/**
 * 
 * Hierarchical PShape SceneGraph, built from all Box2D-World Body/Fixture shapes.
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwBodyRenderP5 implements DwRender {
  
  
  static public class ShapeStyle{
    public boolean fill_enabled   = true;
    public int     fill_color     = 0xFFBBBBBB;
    public boolean stroke_enabled = true;
    public int     stroke_color   = 0xFF000000;
    public float   stroke_weight  = 1f;
  }
  
  
  public PApplet papplet;
  public World world;
  public DwViewportTransform transform;
  

  // default style, that is used during shape creation.
  // this just inits the most basic stuff, for detailed style, get the PShape
  // and manipulate it directly
  public ShapeStyle style = new ShapeStyle();
  
  public PShape shp_bodies;
  
  public DwBodyRenderP5(PApplet papplet, World world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;

    this.shp_bodies = papplet.createShape(PConstants.GROUP);
  }
  


  
  /**
   * 
   * renders the PShapes to the given canvas
   *
   */
  @Override
  public void display(PGraphics2D canvas){
//    shp_bodies.draw(canvas);

    canvas.shape(shp_bodies);
  }
  
  
 
  /**
   * 
   * Updates for every body in the world, the attached PShapes transformation matrix.
   * Must be called after every physics update step.
   * 
   */
  @Override
  public void update(){
 
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      Transform xf = body.getTransform();
      
      PShape shp_body = getShape(body);
      if(shp_body != null){
        shp_body.resetMatrix();
        shp_body.rotate(xf.q.getAngle());
        shp_body.translate(xf.p.x, xf.p.y);
      }
      
//      for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
//        PShape shp_fixture = getShape(fixture);
//        if(shp_fixture != null){
//          shp_fixture.resetMatrix();
//          shp_fixture.rotate(xf.q.getAngle());
//          shp_fixture.translate(xf.p.x, xf.p.y);
//        }
//      }
    }
//     DwUtils.updateBodyShapeTransforms(world);

  }
  
  
  
  
  
 
 
  /**
   * 
   * creates new PShapes for every body in the world. 
   * Does not overwrites existing body shapes.
   * 
   */
  public void createShape(){
    createShape(false);
    
  }
  
  
  /**
   * 
   * creates new PShapes for every body in the world. 
   * This also overwrites existing body shapes if replace_existing_shapes == true.
   * 
   */
  public void createShape(boolean replace_existing_shapes){
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      if(replace_existing_shapes || !hasShape(body)){
        createShape(body);
      }
    }
  }
  

  /**
   * 
   * creates a new PShape for this body, as well as for this body's fixtures
   * 
   */
  public PShape createShape(Body body){
    
    // destroy any old PShapes of this body
    releaseShape(body);
    
    // shit, if not null, this means, m_userData is used for something else
    if(body.m_userData != null){
      System.out.println("ERROR: DwBodyRenderP5.createNewShape(Body body): body.m_userData != null");
      return null;
    }

    // create new PShape
    PShape shp_body = papplet.createShape(PConstants.GROUP);
    shp_bodies.addChild(shp_body);
    body.setUserData(shp_body);
        
    // create Fixture PShapes as children
    for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
      createShape(fixture);
    }
    
    return shp_body;
  }
  
  
  /**
   * 
   * creates a new PShape for this fixture
   * 
   */
  public PShape createShape(Fixture fixture){
    
  
    
    // assure a valid parent body shape
    PShape shp_body = getShape(fixture.m_body);
    if(shp_body == null){
      System.out.println("ERROR: DwBodyRenderP5.createNewShape(Fixture fixture): shp_body == null, crate a body shape first.");
      return null;
    }
    
    // destroy any old PShapes of this fixture
    releaseShape(fixture);
    
    // shit, if not null, this means, m_userData is used for something else
    if(fixture.m_userData != null){
      System.out.println("ERROR: DwBodyRenderP5.createNewShape(Fixture fixture): fixture.m_userData != null");
      return null;
    }
    

    PShape shp_fixture = null;
    
    final ShapeType type = fixture.getType();
    
    if(ShapeType.POLYGON == type){
      PolygonShape shp_polygon = (PolygonShape) fixture.getShape();
      Vec2[] verts = shp_polygon.m_vertices;
      shp_fixture = papplet.createShape();
      shp_fixture.beginShape();
      for(int i = 0; i < shp_polygon.m_count; i++){
        shp_fixture.vertex(verts[i].x, verts[i].y);
      }
      shp_fixture.endShape(PConstants.CLOSE);
    } 
    else if(ShapeType.CIRCLE == type){
      CircleShape shp_circle = (CircleShape) fixture.getShape();
      float radius = shp_circle.m_radius;
      Vec2 vert = shp_circle.m_p;
      shp_fixture = papplet.createShape(PConstants.ELLIPSE, vert.x, vert.y, radius*2, radius*2);
    } 
    else if(ShapeType.CHAIN == type){
      ChainShape shp_chain = (ChainShape) fixture.getShape();
      Vec2[] verts = shp_chain.m_vertices;
      shp_fixture = papplet.createShape();
      shp_fixture.beginShape();
      shp_fixture.noFill();
      for(int i = 0; i < shp_chain.m_count; i++){
        shp_fixture.vertex(verts[i].x, verts[i].y);
      }
      shp_fixture.endShape(PConstants.CLOSE);
    } 
    else if(ShapeType.EDGE == type){
      EdgeShape shp_edge = (EdgeShape) fixture.getShape();
      Vec2 vert1 = shp_edge.m_vertex1;
      Vec2 vert2 = shp_edge.m_vertex2;
      shp_fixture = papplet.createShape(PConstants.LINE, vert1.x, vert1.y, vert2.x, vert2.y);
    } 
    else {
      System.out.println("Error: unknown shapetype");
    }

  
    if(shp_fixture != null){
      
      shp_fixture.setFill        (style.fill_enabled);
      shp_fixture.setFill        (style.fill_color);
      shp_fixture.setStroke      (style.stroke_enabled);
      shp_fixture.setStroke      (style.stroke_color);
      shp_fixture.setStrokeWeight(style.stroke_weight / transform.screen_scale);

      shp_body.addChild(shp_fixture);
      fixture.setUserData(shp_fixture);
    }
    
    return shp_fixture;
  }
  


  
  
  
  
  
  
  
  
  
  
  
  
  // some utility functions
  
  
  /**
   * @return true, if m_userData is a valid PShape
   */
  static public boolean hasShape(Body body){
    return (body.m_userData != null) && (body.m_userData instanceof PShape);
  }
  
  /**
   * @return true, if m_userData is a valid PShape
   */
  static public boolean hasShape(Fixture fixture){
    return (fixture.m_userData != null) && (fixture.m_userData instanceof PShape);
  }
  
  /**
   * @return if available, the PShape of this body, otherwise null
   */
  static public PShape getShape(Body body){
    if(hasShape(body)){
      return (PShape) body.m_userData;
    } else {
      return null;
    }
  }
  
  /**
   * @return if available, the PShape of this fixture, otherwise null
   */
  static public PShape getShape(Fixture fixture){
    if(hasShape(fixture)){
      return (PShape) fixture.m_userData;
    } else {
      return null;
    }
  }
  
  
  
  /**
   * Deletes all references to this Body's PShape and all children
   * 
   */
  public void releaseShape(Body body){
    releaseShape(body, true);
  }
  
  /**
   * Deletes all references to this Body's PShape and all children
   * 
   */
  public void releaseShape(Body body, boolean release_parent){

    
    if(release_parent){
      PShape shp_body = getShape(body);
      removeChild(shp_bodies, shp_body);
    }

    for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
      releaseShape(fixture);
    }
    
    // make sure, to not delete any other m_userData
    if(hasShape(body)){
      body.m_userData = null;
    }
  }
  
  
  /**
   * 
   * deletes all references to this Fixtures PShape. 
   * 
   */
  public void releaseShape(Fixture fixture){
    releaseShape(fixture, true);
  }
  
  /**
   * 
   * deletes all references to this Fixtures PShape. 
   * 
   */
  public void releaseShape(Fixture fixture, boolean release_parent){
    if(release_parent){
      PShape shp_fixture = getShape(fixture);
      PShape shp_body    = getShape(fixture.m_body);
      removeChild(shp_body, shp_fixture);
    }
    
    // make sure, to not delete any other m_userData
    if(hasShape(fixture)){
      fixture.m_userData = null;
    }
  }
  
  
  
  
  /**
   * removes this fixture from the box2d world and completely destroys any 
   * references to this fixtures PShapes
   */
  public void releaseFixture(Fixture fixture){
    releaseShape(fixture);
    fixture.m_body.destroyFixture(fixture);
  }
  
  /**
   * removes this body from the box2d world and completely destroys any 
   * references to this body's PShapes (and its children)
   */
  public void releaseBody(Body body){
    releaseShape(body);
    world.destroyBody(body);
  }
  
  
  
  /**
   * 
   * release all bodies of this world, one by one
   * 
   */
  public void release(){
    
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      releaseBody(body);
    }
  }
  
  
  /**
   * removes a list of bodies from the box2d world and completely destroys any 
   * references to these bodies PShapes (and its children)
   */
  public void releaseBodies(Body ... bodies){
    releaseShapes(bodies);
    for(Body body : bodies){
      world.destroyBody(body);
    }
  }
  
  protected void releaseShapes(Body ... bodies){
    
    int num_bodies = bodies.length;
    
    // check if the body has a PShape and add it to the list
    int num_bshapes = 0;
    PShape[] bshapes = new PShape[num_bodies];
    Body  [] bbodies = new Body  [num_bodies];
    for(int i = 0; i < num_bodies; i++){
      Body   bbody = bodies[i];
      PShape bshape = getShape(bbody);
      if(bshape != null){
        bshapes[num_bshapes] = bshape;
        bbodies[num_bshapes] = bbody;
        num_bshapes++;
      }
    }

    int num_children_new = 0;
    int num_children_old = shp_bodies.getChildCount();
    PShape[] children = shp_bodies.getChildren();
    
    // remove bodies from the group / remap the group at the same time
    __LOOP_CHILDREN__:
    for(int i = 0; i < num_children_old; i++){
      PShape child = children[i];
      
      for(int j = 0; j < num_bshapes; j++){
        Body   bbody  = bbodies[j];
        PShape bshape = bshapes[j];
        if(bshape == child){

          releaseShape(bbody, false);

          num_bshapes--;
          // swap shape
          bshapes[j] = bshapes[num_bshapes];
          bshapes[num_bshapes] = bshape;
          // swap body
          bbodies[j] = bbodies[num_bshapes];
          bbodies[num_bshapes] = bbody;
          continue __LOOP_CHILDREN__;
        }
      }
      
      children[num_children_new++] = child;
    }
    
    
    // remove number of zombies by triming the buffer to the new size
    // unfortunately PShape offers no better way to do this
    for(int i = num_children_old - 1; i >= num_children_new; i--){
      shp_bodies.removeChild(i);
    }

  }
  
  
 
  static protected void removeChild(PShape parent, PShape child){
    if(parent != null && child != null){
      int idx = parent.getChildIndex(child);
      if(idx != -1){
        parent.removeChild(idx);
      }
    }
  }
  
  
  
  
  
  
  
  
  
  // style stuff

  public PShape styleShape(Body body
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    return styleShape(getShape(body), fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape styleShape(Fixture fixture
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    return styleShape(getShape(fixture), fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape styleShape(PShape shp
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    if(shp == null) return null;
    shp.setFill        (fill_enabled);
    shp.setFill        (fill_color);
    shp.setStroke      (stroke_enabled);
    shp.setStroke      (stroke_color);
    shp.setStrokeWeight(stroke_weight / transform.screen_scale);
    return shp;
  }
  
  
  
  
  
  
  
  
  
  


  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  /**
   * 
   * @return returns 0, if everything is setup correctly
   * 
   */
  public int debug_countBodiesWithoutShape(){
    int count = 0;
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      if(!hasShape(body)){
        count++;
      }
    }
    return count;
  }
  
  /**
   * 
   * @return returns 0, if everything is setup correctly
   * 
   */
  public int debug_countFixturesWithoutShape(){
    int count = 0;
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
        if(!hasShape(fixture)){
          count++;
        }
      }
    }
    return count;
  }

  /**
   * 
   * @return returns an empty list, if everything is setup correctly
   * 
   */
  public List<Body> debug_returnBodiesWithoutShape(){
    List<Body> list = new ArrayList<>();
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      if(!hasShape(body)){
        list.add(body);
      }
    }
    return list;
  }
  
  /**
   * 
   * @return returns an empty list, if everything is setup correctly
   * 
   */
  public List<Fixture> debug_returnFixturesWithoutShape(){
    List<Fixture> list = new ArrayList<>();
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
        if(!hasShape(fixture)){
          list.add(fixture);
        }
      }
    }
    return list;
  }
  
  
  
}
