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

public class ShapeStyle {

  boolean fill_enabled   = true;
  int     fill_color     = 0xFFC8C8C8;
  boolean stroke_enabled = true;
  int     stroke_color   = 0xFF000000;
  float   stroke_weight  = 1f;
  // TODO join, cap, etc...
  
  public ShapeStyle(){
  }
  
  public ShapeStyle(
      boolean fill_enabled, 
      int fill_color,
      boolean stroke_enabled, 
      int stroke_color, 
      float stroke_weight) 
  {
    set(fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  
  public ShapeStyle set(
      boolean fill_enabled, 
      int     fill_color,
      boolean stroke_enabled, 
      int     stroke_color, 
      float   stroke_weight) 
  {
    this.fill_enabled   = fill_enabled;
    this.fill_color     = fill_color;
    this.stroke_enabled = stroke_enabled;
    this.stroke_color   = stroke_color;
    this.stroke_weight  = stroke_weight;
    return this;
  }
  

  
 
}
