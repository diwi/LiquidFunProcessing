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

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;



/**
 * 
 * Hierarchical PShape SceneGraph, built from all Box2D-World Body/Fixture shapes.<br>
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwFixture {
  
  public DwBody parent;
  
  public Fixture fixture;
  public PShape shape;
  
  // use m_userData here, instead of fixture.m_userData
  public Object m_userData = null;
  
  public DwFixture(DwBody parent, Fixture fixture){
    this.parent = parent;
    this.fixture = fixture;
    
    createShape();
  }
  

  protected void createShape(){
    
    PApplet papplet = parent.parent.papplet;

    shape = null;
    
    final ShapeType type = fixture.getType();
    
    if(ShapeType.POLYGON == type){
      PolygonShape shp_polygon = (PolygonShape) fixture.getShape();
      Vec2[] verts = shp_polygon.m_vertices;
      shape = papplet.createShape();
      shape.beginShape();
      for(int i = 0; i < shp_polygon.m_count; i++){
        shape.vertex(verts[i].x, verts[i].y);
      }
      shape.endShape(PConstants.CLOSE);
    } 
    else if(ShapeType.CIRCLE == type){
      CircleShape shp_circle = (CircleShape) fixture.getShape();
      float radius = shp_circle.m_radius;
      Vec2 vert = shp_circle.m_p;
      shape = papplet.createShape(PConstants.ELLIPSE, vert.x, vert.y, radius*2, radius*2);
    } 
    else if(ShapeType.CHAIN == type){
      ChainShape shp_chain = (ChainShape) fixture.getShape();
      Vec2[] verts = shp_chain.m_vertices;
      shape = papplet.createShape();
      shape.beginShape();
      shape.noFill();
      for(int i = 0; i < shp_chain.m_count; i++){
        shape.vertex(verts[i].x, verts[i].y);
      }
      shape.endShape(PConstants.CLOSE);
    } 
    else if(ShapeType.EDGE == type){
      EdgeShape shp_edge = (EdgeShape) fixture.getShape();
      Vec2 vert1 = shp_edge.m_vertex1;
      Vec2 vert2 = shp_edge.m_vertex2;
      shape = papplet.createShape(PConstants.LINE, vert1.x, vert1.y, vert2.x, vert2.y);
    } 
    else {
      System.out.println("Error: unknown shapetype");
    }

  
    if(shape != null){
      parent.shape.addChild(shape);
      fixture.setUserData(this);
    }
  }
  
  
  public void replaceShape(PShape shape_new){
    int idx = parent.shape.getChildIndex(shape);
    if(idx != -1){
      parent.shape.removeChild(idx);
    }
    shape = shape_new;
    parent.shape.addChild(shape);
  }
  
  
  public void release(){
    // remove PShape from parent shape-children
    int idx = parent.shape.getChildIndex(shape);
    parent.shape.removeChild(idx);
    
    // remove this from parent-children
    parent.children.remove(this);
    
    shape = null;
    parent = null;
    fixture.setUserData(null);
    fixture = null;
  }

  
}
