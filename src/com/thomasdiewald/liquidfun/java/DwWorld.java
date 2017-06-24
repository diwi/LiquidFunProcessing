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

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;

import com.thomasdiewald.liquidfun.java.interaction.DwBullet;
import com.thomasdiewald.liquidfun.java.interaction.DwInteractionEvent;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragParticles;
import com.thomasdiewald.liquidfun.java.render.DwBody;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.render.DwFixture;
import com.thomasdiewald.liquidfun.java.render.DwJoint;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class DwWorld extends World{
  
  public PApplet papplet;
  public DwViewportTransform transform;
  public DwDebugDraw debug_draw;
  

  public DwWorld(PApplet papplet){
    this(papplet, 15);
  }
  
  public DwWorld(PApplet papplet, float scale){
    super(new Vec2(0, -10f));
    
    this.papplet = papplet;
    
    // particle settings
    super.setParticleGravityScale(0.4f);
    super.setParticleDensity(1.2f);
    super.setParticleDamping(1.0f);
    super.setParticleRadius(0.25f);
    
    
    int w = papplet.width;
    int h = papplet.height;
    transform = new DwViewportTransform(papplet);
    transform.setScreen(w, h, scale, w/2, h);
    
    
    debug_draw = new DwDebugDraw(papplet, this, transform);
      
    papplet.registerMethod("dispose"   , this);
    papplet.registerMethod("mouseEvent", this);
    papplet.registerMethod("keyEvent"  , this);
    
    DwInteractionEvent mouse_drag_bodies    = new DwMouseDragBodies   (this, transform);
    DwInteractionEvent mouse_drag_particles = new DwMouseDragParticles(this, transform);
    bullet   = new DwBullet            (this, transform);
    
    addMouseAction(mouse_drag_bodies);
    addMouseAction(mouse_drag_particles);
    addMouseAction(bullet);
  }
  
  public DwBullet bullet;
  
  public void dispose(){
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  //
  // World Update Step
  //
  //////////////////////////////////////////////////////////////////////////////
  
 
  public void update(){
    update(1/60f, 8, 4);
  }
  
  
  public void update(float timestep, int iter_velocity, int iter_position){
    mouseUpdateAction();
    super.step(timestep, iter_velocity, iter_position);
    updateBodies();
    updateJoints();
  }


  public void updateBodies(){
    for (Body body = super.getBodyList(); body != null; body = body.getNext()) {
      Transform xf = body.getTransform();
      
      DwBody dwbody = getShape(body);
      if(dwbody != null){
        dwbody.shape.resetMatrix();
        dwbody.shape.rotate(xf.q.getAngle());
        dwbody.shape.translate(xf.p.x, xf.p.y);
      }
      
//      for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
//        PShape shp_fixture = DwBodyRenderP5.getShape(fixture);
//        if(shp_fixture != null){
//          shp_fixture.resetMatrix();
//          shp_fixture.rotate(xf.q.getAngle());
//          shp_fixture.translate(xf.p.x, xf.p.y);
//        }
//      }
    }
  }
  
  
  public void updateJoints(){
    Vec2 ancA = new Vec2();
    Vec2 ancB = new Vec2();
 
    for (Joint joint = super.getJointList(); joint != null; joint = joint.getNext()) {
      
//      Body bodyA = joint.getBodyA();
//      Body bodyB = joint.getBodyB();
//      Transform xfA = bodyA.getTransform();
//      Transform xfB = bodyB.getTransform();
//      Vec2 posA = xfA.p;
//      Vec2 posB = xfB.p;
      
      joint.getAnchorA(ancA);
      joint.getAnchorB(ancB);
      // TODO: different joint types
      
      DwJoint dwjoint = getShape(joint);
      if(dwjoint != null){
        
        Vec2 AB = ancB.sub(ancA);
        
        // https://github.com/processing/processing/blob/master/core/src/processing/opengl/PShapeOpenGL.java#L1348
        float ab_len = AB.length() + 0.0000001f; 
        
        dwjoint.shape.resetMatrix();
        dwjoint.shape.scale(ab_len, 1);
        dwjoint.shape.rotate((float) Math.atan2(AB.y, AB.x));
        dwjoint.shape.translate(ancA.x, ancA.y);
      }
      
    }

  }
  
  

  //////////////////////////////////////////////////////////////////////////////
  //
  // DebugDraw
  //
  //////////////////////////////////////////////////////////////////////////////
  
  
  public void displayDebugDraw(PGraphics canvas){
    debug_draw.display(canvas);
  }
  
  
  public void applyTransform(PGraphics canvas){
    canvas.applyMatrix(transform.mat_box2screen);
  }
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Bullet
  //
  //////////////////////////////////////////////////////////////////////////////
  public void drawBulletSpawnTrack(PGraphics canvas){
    bullet.drawSpawnTrack(canvas);
  }
  

  

  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Interactions
  //
  //////////////////////////////////////////////////////////////////////////////
  
  
  protected ArrayList<DwInteractionEvent> mouse_actions = new ArrayList<>();

  public void mouseEvent(MouseEvent event){
    for(DwInteractionEvent maction : mouse_actions){
      maction.mouseEvent(event);
    }
  }
  
  public void keyEvent(KeyEvent event){
    for(DwInteractionEvent maction : mouse_actions){
      maction.keyEvent(event);
    }
  }
  
  protected void mouseUpdateAction(){
    for(DwInteractionEvent maction : mouse_actions){
      maction.updateEvent();
    }
  }
  
  public void addMouseAction(DwInteractionEvent mouse_action){
    if(!mouse_actions.contains(mouse_action)){
      mouse_actions.add(mouse_action);
    }
  }
  
  public void removeMouseAction(DwInteractionEvent mouse_action){
    mouse_actions.remove(mouse_action);
  }
  
  public void removeMouseAction(Class<?> obj_class){
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        mouse_actions.remove(i);
      }
    }
  }
  
  public boolean hasMouseAction(DwInteractionEvent mouse_action){
    return mouse_actions.contains(mouse_action);
  }
  
  public boolean hasMouseAction(Class<?> obj_class){
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        return true;
      }
    }
    return false;
  }
  

  public List<DwInteractionEvent> getMouseAction(Class<?> obj_class){
    List<DwInteractionEvent> list = new ArrayList<>();
    
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        list.add(maction);
      }
    }
    return list;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Body Shape Utilities
  //
  //////////////////////////////////////////////////////////////////////////////
  
  public void destroyBody(Body body){
    release(body);
    super.destroyBody(body);
  }
  
  public void destroyJoint(Joint joint){
    release(joint);
    super.destroyJoint(joint);
  }
  
  
  

  
  static public void release(Body body){
    DwBody dwbody = getShape(body);
    if(dwbody != null){
      dwbody.parent.release(body);
    }
  }
  
  static public void release(Joint joint){
    DwJoint dwjoint = getShape(joint);
    if(dwjoint != null){
      dwjoint.parent.release(joint);
    }
  }
  
//  static public void release(Fixture fixture){
//    DwFixtureShape dwfixture = getShape(fixture);
//    if(dwfixture != null){
//      dwfixture.parent.release(fixture);
//    }
//  }

  
  
  static public boolean hasShape(Joint joint){
    return (joint.m_userData != null) && (joint.m_userData instanceof DwJoint);
  }
  
  static public DwJoint getShape(Joint joint){
    if(hasShape(joint)){
      return (DwJoint) joint.m_userData;
    }
    return null;
  }

  static public boolean hasShape(Fixture fixture){
    return (fixture.m_userData != null) && (fixture.m_userData instanceof DwFixture);
  }
  
  static public DwFixture getShape(Fixture fixture){
    if(hasShape(fixture)){
      return (DwFixture) fixture.m_userData;
    }
    return null;
  }
  
  static public boolean hasShape(Body body){
    return (body.m_userData != null) && (body.m_userData instanceof DwBody);
  }
  
  static public DwBody getShape(Body body){
    if(hasShape(body)){
      return (DwBody) body.m_userData;
    }
    return null;
  }
  
  
  

  public PShape setStyle(Body body
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwBody dwbody = getShape(body);
    return setStyle(dwbody.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(Fixture fixture
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwFixture dwfixture = getShape(fixture);
    return setStyle(dwfixture.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(Joint joint
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwJoint dwjoint = getShape(joint);
    return setStyle(dwjoint.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(PShape shp
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

  
  
  
}
