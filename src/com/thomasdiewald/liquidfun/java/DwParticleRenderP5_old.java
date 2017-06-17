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

import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PGraphics2D;


/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. 
 * Using PShape point-sprites for rendering.
 * 
 * Performance is good, but DwParticleRenderGL is certainly the first choice.
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleRenderP5_old {
  
  static public class Param{
    public PImage tex_sprite   = null;
    public float  radius_scale = 1f;
    public float  falloff_exp1 = 1f;
    public float  falloff_exp2 = 1f;
    public float  falloff_mult = 1f;
    public float  color_mult   = 1f;
  }
  
  public Param param = new Param();
  
  public PApplet papplet;

  public World world;
  public DwViewportTransform transform;
  
  public PShape shp_group;
  
  public DwParticleRenderP5_old(PApplet papplet, World world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;

    this.shp_group = papplet.createShape(PConstants.GROUP);
  }
  
  public void release(){
  }

  public void updateShapeTransforms(){
    updateShapeTransforms(false);
  }
  
  public void updateShapeTransforms(boolean update_rotations){
    DwUtils.updateParticleShapeTransforms(world, shp_group, update_rotations);
  }
  
  public void updateShapeBuffer(){
    DwUtils.updateParticleShapeBuffer(world, shp_group);
  }
  

  
  
  public void display(PGraphics2D canvas){
    shp_group.draw(canvas);
  }
  
  
  public void add(ParticleGroup particle_group){

    float           particle_rad = world.getParticleRadius();
    int             particle_num = particle_group.getParticleCount();
    int             particle_idx = particle_group.getBufferIndex();
    ParticleColor[] particle_col = world.getParticleColorBuffer();
    
    particle_rad *= param.radius_scale;
    int radius_screen = (int) (Math.ceil(particle_rad * transform.screen_scale) * 2);
    
    // if no sprite is provided, create a default one
    if(param.tex_sprite == null){
      param.tex_sprite = createDefaultSprite(papplet, radius_screen * 2);
    }

    for(int i = 0; i < particle_num; i++){
      ParticleColor col = particle_col[particle_idx + i];
      int argb = papplet.color(col.r & 0xFF, col.g & 0xFF, col.b & 0xFF, col.a & 0xFF);
      
      float rad = particle_rad * 0.7f; // not sure why particles are overlapping
      
      PShape shp_particle = papplet.createShape();
      shp_particle.beginShape(PConstants.QUAD);
      shp_particle.noFill();
      shp_particle.noStroke();
      shp_particle.tint(argb);
      shp_particle.fill(255);
      shp_particle.textureMode(PConstants.NORMAL);
      shp_particle.texture(param.tex_sprite);
      shp_particle.vertex(-rad, -rad, 0, 0);
      shp_particle.vertex(+rad, -rad, 1, 0);
      shp_particle.vertex(+rad, +rad, 1, 1);
      shp_particle.vertex(-rad, +rad, 0, 1);    
      shp_particle.endShape();    
      shp_group.addChild(shp_particle);
    }
  }
  


  protected PImage createDefaultSprite(PApplet papplet, int size){
    size = Math.max(32, 64);
    
    PImage pimg = papplet.createImage(size, size, PConstants.ARGB);
    pimg.loadPixels();

    
    for(int y = 0; y < size; y++){
      for(int x = 0; x < size; x++){
        int pid = y * size + x;
        
        float xn = (x / (float)size) * 2f - 1f;
        float yn = (y / (float)size) * 2f - 1f;
        float dd = (float) Math.sqrt(xn*xn + yn*yn);
        
        if(dd < 0) dd = 0; else if(dd > 1) dd = 1;
        dd = (float) Math.pow(dd, param.falloff_exp1);
        dd = 1.0f - dd;
        dd = (float) Math.pow(dd, param.falloff_exp2);
        dd *= param.falloff_mult;
        pimg.pixels[pid] = ((int)(dd * 255)) << 24 | 0x00FFFFFF;

      }
    }
    pimg.updatePixels();
 
    return pimg;
  }
  

}
