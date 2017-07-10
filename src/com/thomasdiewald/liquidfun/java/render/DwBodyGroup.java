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

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.Joint;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;
import com.thomasdiewald.liquidfun.java.DwWorld;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.opengl.PGraphics2D;



/**
 * 
 * Hierarchical PShape SceneGraph, built from all Box2D-World Body/Fixture shapes.<br>
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwBodyGroup {

  public PApplet papplet;
  public DwWorld world;
  public DwViewportTransform transform;
  
  public PShape shape;
  public ArrayList<DwBody> childrenB = new ArrayList<DwBody>();
  public ArrayList<DwJoint> childrenJ = new ArrayList<DwJoint>();
  
  public DwBodyGroup(PApplet papplet, DwWorld world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;

    this.shape = papplet.createShape(PConstants.GROUP);
  }
  
  
  /**
   * 
   * Add/create body-shape, using a new default shape-style.<br>
   * If this body-shape already exists it is destroyed first.<br>
   * 
   */
  public DwBody add(Body body){
    return add(body, new ShapeStyle());
  }
  
  /**
   * 
   * Add/create body-shape, using the given style params.<br>
   * If this body-shape already exists it is destroyed first.<br>
   * 
   */
  public DwBody add(Body body, boolean fill_enabled, int fill_color, boolean stroke_enabled, int stroke_color, float stroke_weight){
    return add(body, new ShapeStyle(fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight));
  }
  
  /**
   * 
   * Add/create body-shape, using the given style<br>
   * If this body-shape already exists it is destroyed first.<br>
   * 
   */
  public DwBody add(Body body, ShapeStyle style){
    if(body == null) return null;
    DwBody child = DwWorld.getShape(body);
    if(child != null){
      child.release();
    }
    child = new DwBody(this, body);
    childrenB.add(child);
    world.setStyle(body, style.fill_enabled, style.fill_color, style.stroke_enabled, style.stroke_color, style.stroke_weight);
    return child;
  }
  
  
  
  
  /**
   * 
   * Add/create fixture-shape, using a new default shape-style, or parents body style.<br>
   * If this fixture-shape already exists it is destroyed first.<br>
   * 
   */
  public DwFixture add(Fixture fixture){
    ShapeStyle style = new ShapeStyle();
    DwBody dwbody = DwWorld.getShape(fixture.m_body);
    if(dwbody != null){
      style = dwbody.style;
    }
    return add(fixture, style);
  }
  
  /**
   * 
   * Add/create fixture-shape, using the given style params.<br>
   * If this fixture-shape already exists it is destroyed first.<br>
   * 
   */
  public DwFixture add(Fixture fixture, boolean fill_enabled, int fill_color, boolean stroke_enabled, int stroke_color, float stroke_weight){
    return add(fixture, new ShapeStyle(fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight));
  }
  
  
  /**
   * 
   * Add/create fixture-shape, using the given style.<br>
   * If this fixture-shape already exists it is destroyed first.<br>
   * 
   */
  public DwFixture add(Fixture fixture, ShapeStyle style){
    if(fixture == null) return null;
    DwBody dwbody = DwWorld.getShape(fixture.m_body);
    if(dwbody != null){
      dwbody.add(fixture);
    } else {
      add(fixture.m_body);
    }
    world.setStyle(fixture, style.fill_enabled, style.fill_color, style.stroke_enabled, style.stroke_color, style.stroke_weight);
    return DwWorld.getShape(fixture);
  }
  

  
  

  
  
  
  
  
  /**
   * 
   * Add/create joint-shape, using a new default shape-style.<br>
   * If this joint-shape already exists it is destroyed first.<br>
   * 
   */
  public DwJoint add(Joint joint){
    ShapeStyle style = new ShapeStyle();
    style.fill_enabled = false;
    return add(joint, style);
  }
  
  /**
   * 
   * Add/create joint-shape, using the given style params.<br>
   * If this joint-shape already exists it is destroyed first.<br>
   * 
   */
  public DwJoint add(Joint joint, boolean fill_enabled, int fill_color, boolean stroke_enabled, int stroke_color, float stroke_weight) {
    return add(joint, new ShapeStyle(fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight));
  }
  
  /**
   * 
   * Add/create joint-shape, using the given style.<br>
   * If this joint-shape already exists it is destroyed first.<br>
   * 
   */
  public DwJoint add(Joint joint, ShapeStyle style){
    if(joint == null) return null;
    DwJoint child = DwWorld.getShape(joint);
    if(child != null){
      child.release();
    }
    child = new DwJoint(this, joint);
    childrenJ.add(child);
    world.setStyle(joint, style.fill_enabled, style.fill_color, style.stroke_enabled, style.stroke_color, style.stroke_weight);
    return child;
  }
  
  
  
  
  /**
   * 
   * Add/create shapes for all bodies/joints that have not attached shapes.<br>
   * 
   */
  public void addAll(){
    addAll(true, true);
  }
  
  /**
   * 
   * Add/create shapes for all bodies/joints that have not attached shapes.<br>
   * 
   */
  public void addAll(boolean create_bodies, boolean create_joints){
    
    if(create_bodies){
      for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
        DwBody dwbody = DwWorld.getShape(body);
        if(dwbody == null){
          add(body);
        }
      }
    }
    
    if(create_joints){
      for (Joint joint = world.getJointList(); joint != null; joint = joint.getNext()) {
        DwJoint dwjoint = DwWorld.getShape(joint);
        if(dwjoint == null){
          add(joint);
        }
      }
    }
    
  }
  
  
  
  
  
  
  
  /**
   * 
   * If a new bullet is available (= was recently shot) it is added to this body tree.
   * 
   */
  public DwBody addBullet(boolean fill_enabled, int fill_color, boolean stroke_enabled, int stroke_color, float stroke_weight){
    return add(world.mouse_shoot_bullet.popBullet(), fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }

  
  
  
  
  
  
  

  
  public void release(){
    for(int i = childrenB.size()-1; i>= 0; i--){
      childrenB.get(i).release();
    }
    childrenB.clear(); // should already be cleared
    childrenB = null;
    
    for(int i = childrenJ.size()-1; i>= 0; i--){
      childrenJ.get(i).release();
    }
    childrenJ.clear(); // should already be cleared
    childrenJ = null;
    
    shape = null;
  }
  

  
  /**
   * 
   * Displays this shape tree.<br>
   * 
   */
  public void display(PGraphics2D canvas){
    canvas.shape(shape);
  }
  
  
  
  
  
   
  /**
   * 
   * @return returns 0, if everything is setup correctly
   * 
   */
  public int debug_countBodiesWithoutShape(){
    int count = 0;
    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      if(!DwWorld.hasShape(body)){
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
        if(!DwWorld.hasShape(fixture)){
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
      if(!DwWorld.hasShape(body)){
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
        if(!DwWorld.hasShape(fixture)){
          list.add(fixture);
        }
      }
    }
    return list;
  }

}
