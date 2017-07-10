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

import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;

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
public class DwJoint{

  public DwBodyGroup parent;
  
  public Joint joint;
  public PShape shape;
  
  // use m_userData here, instead of joint.m_userData
  public Object m_userData = null;
  

  public DwJoint(DwBodyGroup parent, Joint joint){
    this.parent = parent;
    this.joint = joint;
    
    JointType type = joint.getType();

    // create PShape
//    this.shape = parent.papplet.createShape(PConstants.LINE, 0, 0, 1, 0);
//    this.shape.setStrokeJoin(PConstants.ROUND);
//    this.shape.setStrokeCap(PConstants.ROUND);
//    shape = parent.papplet.createShape();
//    shape.beginShape(PConstants.QUADS);
//    shape.fill(255);
//    shape.vertex(0,0);
//    shape.vertex(1,0);
//    shape.vertex(1,1);
//    shape.vertex(0,1);
//    shape.endShape();
    

    if(type == JointType.PULLEY){
      shape = parent.papplet.createShape(PConstants.GROUP);
      shape.addChild(parent.papplet.createShape(PConstants.LINE, 0, 0, 1, 0));
      shape.addChild(parent.papplet.createShape(PConstants.LINE, 0, 0, 1, 0));
      shape.addChild(parent.papplet.createShape(PConstants.LINE, 0, 0, 1, 0));
    } else {
      shape = parent.papplet.createShape(PConstants.LINE, 0, 0, 1, 0);
    }
    
    
    
    // link PShapes
    parent.shape.addChild(shape);
    
    joint.setUserData(this);
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
    int idx = parent.shape.getChildIndex(shape);
    parent.shape.removeChild(idx);
    
    parent.childrenJ.remove(this);
    
    shape = null;
    parent = null;
    joint.setUserData(null);
    joint = null;
  }

  

}
