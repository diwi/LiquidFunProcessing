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

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleContact;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PGraphics2D;


/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. 
 * Using low-level OpenGL calls to improve performance.
 * 
 * 
 * @author Thomas Diewald
 *
 */
public abstract class DwParticleRender implements DwRender {
  
  static public class Param{
    public PImage tex_sprite   = null;
    public float  radius_scale = 1f;
    public float  falloff_exp1 = 1f;
    public float  falloff_exp2 = 1f;
    public float  falloff_mult = 1f;
    public float  color_mult   = 1f;
  }
  
  
  public PApplet papplet;
  public World world;
  public DwViewportTransform transform;
  
  // particle render parameters
  public Param param = new Param();

  // buffer data
  public int   particle_num = 0;
  public float particle_rad = 0f;
  
  public int buf_pos_len = 0;
  public int buf_vel_len = 0;
  public int buf_col_len = 0;
  public int buf_con_len = 0;
  
  public float[] buf_pos = new float[0];
  public float[] buf_vel = new float[0];
  public byte [] buf_col = new byte [0];
  public float[] buf_con = new float[0];
  
  public DwParticleRender(PApplet papplet, World world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;
  }
  

  public void updateBuffers(){
    // particle data
    particle_rad = world.getParticleRadius();
    particle_num = world.getParticleCount();
    Vec2[]            particle_pos = world.getParticlePositionBuffer();
    Vec2[]            particle_vel = world.getParticleVelocityBuffer();
    ParticleColor[]   particle_col = world.getParticleColorBuffer();
    ParticleContact[] particle_con = world.getParticleContacts();
    int particle_con_count         = world.getParticleContactCount();

    // buffer capacity
    buf_pos_len =  particle_num * 2; // [posx, posy]
    buf_vel_len =  particle_num * 2; // [velx, vely]
    buf_col_len =  particle_num * 4; // [r, g, b, a]
    buf_con_len =  particle_num * 2; // [weight, count]
    
    // resize buffers
    buf_pos = DwUtils.resizeBuffer(buf_pos, buf_pos_len);
    buf_vel = DwUtils.resizeBuffer(buf_vel, buf_vel_len);
    buf_col = DwUtils.resizeBuffer(buf_col, buf_col_len);
    buf_con = DwUtils.resizeBuffer(buf_con, buf_con_len);

    // fill buffers    
    for(int i = 0, ipos = 0, ivel = 0, icol = 0, icon = 0; i < particle_num; i++){
      // position
      Vec2 pos = particle_pos[i];
      buf_pos[ipos++] = pos.x;
      buf_pos[ipos++] = pos.y;
      // velocity
      Vec2 vel = particle_vel[i];
      buf_vel[ivel++] = vel.x;
      buf_vel[ivel++] = vel.y;
      // color
      ParticleColor col = particle_col[i];
      buf_col[icol++] = col.r;
      buf_col[icol++] = col.g;
      buf_col[icol++] = col.b;
      buf_col[icol++] = col.a;
      // contacts
      buf_con[icon++] = 0;
      buf_con[icon++] = 0;
    }
    
    // set contact data
    for(int i = 0; i < particle_con_count; i++){
      ParticleContact con = particle_con[i];
      
      int ia = con.indexA * 2;
      int ib = con.indexB * 2;
      
      buf_con[ia+0] += con.weight;
      buf_con[ib+0] += con.weight;

      buf_con[ia+1] += 1;
      buf_con[ib+1] += 1;
    }
    
//    for(int i = 0; i < particle_con_count; i++){
//      ParticleContact con = particle_con[i];
//      
//      int ia = con.indexA * 2;
//      int ib = con.indexB * 2;
//      
//      Vec2 norm = con.normal;
//      Vec2 vela = particle_vel[ia];
//      Vec2 velb = particle_vel[ib];
//
//
//      float velx = velb.x - vela.x;
//      float vely = velb.y - vela.y;
//      float veln = velx * norm.x + vely * norm.y; // dot(dvel, norm)
//      if (veln < 0) {
////        sum_v2 += veln * veln;
//      }
//      
//      buf_con[ia+0] += con.weight;
//      buf_con[ib+0] += con.weight;
//      
//      buf_con_weight_max = Math.max(buf_con_weight_max, buf_con[ia+0]);
//      buf_con_weight_max = Math.max(buf_con_weight_max, buf_con[ia+0]);
//      
//      buf_con[ia+1] += 1;
//      buf_con[ib+1] += 1;
//    }
    
    
    float particle_rad = world.getParticleRadius() * param.radius_scale;
    int radius_screen = (int) (Math.ceil(particle_rad * transform.screen_scale) * 2);
    
    // if no sprite is provided, create a default one
    if(param.tex_sprite == null){
      param.tex_sprite = createDefaultSprite(papplet, radius_screen * 2);
    }
    
  }
  
  
  
  public PImage createDefaultSprite(PApplet papplet, int size){
    size = Math.max(32, size);
    
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
  
  
  
  
  public abstract void update();
  public abstract void release();
  public abstract void display(PGraphics2D canvas);
  
  
  
}
