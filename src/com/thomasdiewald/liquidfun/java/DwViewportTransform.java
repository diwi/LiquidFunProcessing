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

import org.jbox2d.common.IViewportTransform;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix2D;

/**
 * 
 * 
 * Coordinate transformation box2d-world <-> screen
 * 
 * @author Thomas Diewald
 *
 */
public class DwViewportTransform implements IViewportTransform {

  // flip vertically
  protected boolean flip_y = true;
  
  // box2d stuff
  public final Mat22 mat_world_to_screen = new Mat22(1,0,0,1); // identity
  public final Mat22 mat_screen_to_world = new Mat22(1,0,0,1); // identity
  public final Vec2  center  = new Vec2();
  public final Vec2  extents = new Vec2();
  

  // processing stuff, but more or less the same as above
  public final PMatrix2D mat_box2screen = new PMatrix2D();
  public final PMatrix2D mat_screen2box = new PMatrix2D();
  
  public float screen_scale = 15;
  public float screen_dimx;
  public float screen_dimy;
  public float box2d_dimx;
  public float box2d_dimy;
  
  
  public DwViewportTransform() {
  }
  
  public DwViewportTransform(PApplet papplet) {
    this(papplet.g);
  }
  
  public DwViewportTransform(PGraphics pg) {
    float w = pg.width;
    float h = pg.height;
    float s = screen_scale;
    setScreen(w, h, s, w/2, h);
  }
  
  public DwViewportTransform(float screen_w, int screen_h) {
    float w = screen_w;
    float h = screen_h;
    float s = screen_scale;
    setScreen(w, h, s, w/2, h);
  }
  

  public DwViewportTransform(DwViewportTransform vpt) {
    flip_y = vpt.flip_y;
    setExtents(vpt.extents);
    setCamera(vpt.center.x, vpt.center.y, vpt.screen_scale);
  }
  
  
  public DwViewportTransform setSceneScale(float scale){
    screen_scale = scale;
    updateMatrix();
    return this;
  }
  
  
  public DwViewportTransform setScreen(float dim_x, float dim_y, float scale, float origin_x, float origin_y){
    
    float dim_xh = dim_x / 2;
    float dim_yh = dim_y / 2;
    
    float cam_x = (origin_x - dim_xh) / scale;
    float cam_y = (origin_y - dim_yh) / scale;
    
    setExtents(dim_xh, dim_yh);
    setCamera(cam_x, cam_y, scale);
    return this;
  }
  

  public void setCamera(float x, float y) {
    center.set(x, y);
    updateMatrix();
  }


  
  /**
   * set origin, in world
   */
  @Override
  public void setCamera(float x, float y, float scale) {
    screen_scale = scale;
    center.set(x, y);
    updateMatrix();
  }
  
  @Override
  public Vec2 getExtents() {
    return extents;
  }
  
  /**
   * set origin, in screen coords
   */
  @Override
  public void setExtents(Vec2 argExtents) {
    setExtents(argExtents.x, argExtents.y);
    updateMatrix();
  }
  
  /**
   * set origin, in screen coords
   */
  @Override
  public void setExtents(float halfWidth, float halfHeight) {
    extents.set(halfWidth, halfHeight);
  }
  
  @Override
  public Vec2 getCenter() {
    return center;
  }
  
  @Override
  public void setCenter(Vec2 argPos) {
    center.set(argPos);
  }
  
  @Override
  public void setCenter(float x, float y) {
    center.set(x, y);
  }
  
  @Override
  public boolean isYFlip() {
    return flip_y;
  }
  
  @Override
  public void setYFlip(boolean yFlip) {
    this.flip_y = yFlip;
  }
  
  @Override
  public Mat22 getMat22Representation() {
    return mat_world_to_screen;
  }

  @Override
  public void mulByTransform(Mat22 transform) {
    // mat.mulLocal(transform);
    System.out.println("DwViewportTransform.mulByTransform is not implemented");
  }
  



  
  
  protected void updateMatrix(){
    
    // Box2d Matrix:
    
    // matrix: box2d -> screen
    mat_world_to_screen.ex.x = screen_scale;
    mat_world_to_screen.ex.y = 0;
    mat_world_to_screen.ey.x = 0;
    mat_world_to_screen.ey.y = flip_y ? -screen_scale : screen_scale;
    
    // matrix: screen -> box2d
    mat_world_to_screen.invertToOut(mat_screen_to_world);
    

    // Processing matrix:
    
    // matrix: box2d -> screen
    mat_box2screen.reset();
    mat_box2screen.translate(extents.x, extents.y);
    mat_box2screen.scale(screen_scale, flip_y ? -screen_scale : screen_scale);
    mat_box2screen.translate(-center.x, -center.y);

    // matrix: screen -> box2d
    mat_screen2box.set(mat_box2screen);
    mat_screen2box.invert();
    
    
    // screen size
    screen_dimx = extents.x * 2;
    screen_dimy = extents.y * 2;
    
    // screen size in box world dimensions
    box2d_dimx = screen_dimx / screen_scale;
    box2d_dimy = screen_dimy / screen_scale;
    
  }
  
  
  
  
  
  
  
  
  
  

  
  @Override
  public void getWorldVectorToScreen(Vec2 world, Vec2 screen) {
    mat_world_to_screen.mulToOut(world, screen);
  }
  
  @Override
  public void getScreenVectorToWorld(Vec2 screen, Vec2 world) {
    mat_screen_to_world.mulToOut(screen, world);
  }
  
  @Override
  public void getWorldToScreen(Vec2 world, Vec2 screen) {
    screen.x = world.x - center.x;
    screen.y = world.y - center.y;
    mat_world_to_screen.mulToOut(screen, screen);
    screen.x += extents.x;
    screen.y += extents.y;
  }

  @Override
  public void getScreenToWorld(Vec2 screen, Vec2 world) {
    world.x = screen.x - extents.x;
    world.y = screen.y - extents.y;
    mat_screen_to_world.mulToOut(world, world);
    world.x += center.x;
    world.y += center.y;
  }
  
  
  
  
  

  
  
  
  // just some buffers
  protected float[] xy1 = new float[2];
  protected float[] xy2 = new float[2];
  
  
  public Vec2 getBox2screen(float in_box_x, float in_box_y, Vec2 out_screen){
    xy1[0] = in_box_x;
    xy1[1] = in_box_y;
    mat_box2screen.mult(xy1, xy2);
    if(out_screen == null){
      return new Vec2(xy2[0], xy2[1]);
    }
    return out_screen.set(xy2[0], xy2[1]);
  }
  
  public float[] getBox2screen(float in_box_x, float in_box_y, float[] out_screen){
    xy1[0] = in_box_x;
    xy1[1] = in_box_y;
    if(out_screen == null){
      out_screen = new float[2];
    }
    mat_box2screen.mult(xy1, out_screen);
    return out_screen;
  }
  

  public Vec2 getScreen2box(float in_screen_x, float in_screen_y, Vec2 out_box){
    xy1[0] = in_screen_x;
    xy1[1] = in_screen_y;
    mat_screen2box.mult(xy1, xy2);
    if(out_box == null){
      return new Vec2(xy2[0], xy2[1]);
    }
    return out_box.set(xy2[0], xy2[1]);
  }
  
  
  public float[] getScreen2box(float in_screen_x, float in_screen_y, float[] out_box){
    xy1[0] = in_screen_x;
    xy1[1] = in_screen_y;
    if(out_box == null){
      out_box = new float[2];
    }
    mat_screen2box.mult(xy1, out_box);
    return out_box;
  }

  
  
}