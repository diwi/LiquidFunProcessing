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
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import com.thomasdiewald.liquidfun.java.DwWorld;

import processing.core.PConstants;
import processing.core.PShape;



/**
 * 
 * Hierarchical PShape SceneGraph, built from all Box2D-World Body/Fixture shapes.<br>
 * 
 * @author Thomas Diewald
 *
 */
public class DwBody{

  public DwBodyGroup parent;
  
  public Body body;
  public PShape shape;
  
  public ArrayList<DwFixture> children = new ArrayList<DwFixture>();
  
  public ShapeStyle style = new ShapeStyle();
  
  // use m_userData here, instead of body.m_userData
  public Object m_userData = null;

  public DwBody(DwBodyGroup parent, Body body){
    this.parent = parent;
    this.body = body;
    
    // create PShape
    this.shape = parent.papplet.createShape(PConstants.GROUP);
    // link PShapes
    parent.shape.addChild(shape);
    
    body.setUserData(this);
    
    // create children
    for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
      add(fixture);
    }
  }
  
  
  public DwFixture add(Fixture fixture){
    DwFixture dwfixture = DwWorld.getShape(fixture);
    if(dwfixture != null){
      dwfixture.release();
    }
    dwfixture = new DwFixture(this, fixture);
    children.add(dwfixture);
    return dwfixture;
  }
  
  
  
  public void release(){
    for(int i = children.size()-1; i>= 0; i--){
      children.get(i).release();
    }
//    for(DwFixture child : children){
//      child.release();
//    }
    children.clear(); // should already be cleared
    children = null;
    
    int idx = parent.shape.getChildIndex(shape);
    parent.shape.removeChild(idx);
    
    parent.childrenB.remove(this);

    shape = null;
    parent = null;
    body.setUserData(null);
    body = null;
  }

}
