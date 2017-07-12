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

import java.util.Arrays;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleContact;
import org.jbox2d.particle.ParticleGroup;

import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphics2D;


/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. <br>
 * updates render-buffers: position, colors, velocity, contacts, etc...<br>
 * 
 * 
 * @author Thomas Diewald
 *
 */
public abstract class DwParticleRender{
  
  static public class Param{
    public PImage tex_sprite   = null;
    public float  falloff_exp1 = 1f;
    public float  falloff_exp2 = 2f;
    public float  falloff_mult = 1f;
    public float  radius_scale = 2f;
    public float  color_mult   = 1f;
  }
  
  
  public PApplet papplet;
  public World world;
  public DwViewportTransform transform;
  
  // particle render parameters
  public Param param = new Param();

  // buffer data
  public int   particle_num = 0;
  public float particle_rad_world  = 0f;
  public float particle_rad_screen = 0f;
  
  public int buf_pos_len = 0;
  public int buf_vel_len = 0;
  public int buf_col_len = 0;
  public int buf_con_len = 0;
  
  public float[] buf_pos = new float[0];
  public float[] buf_vel = new float[0];
  public byte [] buf_col = new byte [0];
  public float[] buf_con = new float[0];
  
  public int   buf_idx_len = 0;
  public int[] buf_idx = new int[0];
  
  
  public DwParticleRender(PApplet papplet, World world, DwViewportTransform transform){
    this.papplet = papplet;
    this.world = world;
    this.transform = transform;
  }
  

  public void updateBuffers(){
    // particle data
                      particle_num        = world.getParticleCount();
                      particle_rad_world  = world.getParticleRadius();
    Vec2[]            particle_pos        = world.getParticlePositionBuffer();
    Vec2[]            particle_vel        = world.getParticleVelocityBuffer();
    ParticleColor[]   particle_col        = world.getParticleColorBuffer();
    ParticleContact[] particle_con        = world.getParticleContacts();
    int               particle_con_count  = world.getParticleContactCount();
    
    
    particle_rad_world *= param.radius_scale;
    particle_rad_screen = (float) Math.ceil(particle_rad_world * transform.screen_scale);

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
    
    // contacts, reset
    for(int i = 0; i < buf_con_len; i++){
      buf_con[i] = 0;
    }
       
    // contacts
    for(int i = 0; i < particle_con_count; i++){
      ParticleContact con = particle_con[i];
      int ia = con.indexA * 2;
      int ib = con.indexB * 2;
      buf_con[ia+0] += con.weight;
      buf_con[ib+0] += con.weight;
      buf_con[ia+1] += 1;
      buf_con[ib+1] += 1;
    }
     
    // position, velocity, colors
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
      int col_r = col.r & 0xFF;
      int col_g = col.g & 0xFF;
      int col_b = col.b & 0xFF;
      int col_a = col.a & 0xFF;
      
      float con_x = buf_con[icon++];
      float con_y = buf_con[icon++];
      
//      float vel_mag = (float) Math.sqrt(vel.x * vel.x + vel.y * vel.y) * 0.01f;
//      float con_mult = con_x / 40.0f;
//      
//      float sum = 1.0f + (con_mult + vel_mag);
//      sum *= sum; sum *= sum; sum *= sum; 
      
      float vel_fac =(float) Math.sqrt(vel.x * vel.x + vel.y * vel.y) * 0.02f;
      float con_fac = con_x * 0.15f;
      
      float sum = 1.0f + (con_fac + vel_fac);
      sum *= sum; 
      sum *= sum; 

      col_r = Math.round(DwUtils.clamp(col_r * sum, 0, 255));
      col_g = Math.round(DwUtils.clamp(col_g * sum, 0, 255));
      col_b = Math.round(DwUtils.clamp(col_b * sum, 0, 255));
      
      buf_col[icol++] = (byte) col_r;
      buf_col[icol++] = (byte) col_g;
      buf_col[icol++] = (byte) col_b;
      buf_col[icol++] = (byte) col_a;
    }
    

    // sprite texture
    if(param.tex_sprite == null){
      param.tex_sprite = DwUtils.createSprite(papplet, (int) particle_rad_screen * 4, param.falloff_exp1, param.falloff_exp2, param.falloff_mult);
    }
    
  }
  
  
  public void release(){
    particle_num = 0;
    particle_rad_world  = 0f;
    particle_rad_screen = 0f;
    
    buf_pos_len = 0;
    buf_vel_len = 0;
    buf_col_len = 0;
    buf_con_len = 0;
    
    buf_pos = new float[0];
    buf_vel = new float[0];
    buf_col = new byte [0];
    buf_con = new float[0];
  }
  
  
  public abstract void update();
  public abstract void display(PGraphics2D canvas);
  
  public void display(PGraphics2D canvas, int ... render_group_ids){
    setRenderGroupIds(render_group_ids);
    display(canvas);
  }
  
  /**
   * 
   * @param group_ids
   */
  public void setRenderGroupIds(int ... group_ids){
    this.group_ids = group_ids;
  }
  

  
  
  protected boolean USE_GROUPS = !true;
  protected int   group_count;
  protected int[] group_offsets;
  protected int[] group_lengths;
  protected int[] group_ids = new int[0];
  
  public void useGroups(boolean use_groups){
     this.USE_GROUPS = use_groups; 
  }
  
  public void setIndexBuffer(int[] indices, int indices_len){
    buf_idx = indices;
    buf_idx_len = indices_len;
  }
  
  public void setIndexGroups(int[] group_offsets, int[] group_lengths, int group_count){
    this.group_count = group_count;
    this.group_offsets = group_offsets;
    this.group_lengths = group_lengths;
  }


  
  
  
  
  
  
  

  

  
  
  public void generateParticleGroups(){
    if(!USE_GROUPS){
      return;
    }
    if(prgc == null){
      System.out.println("Error - DwParticleRender.generateParticleGroups: call setParticleRenderGroupCallback(callback) first");
      return;
    }
    
    final int[] particle_flags = world.getParticleFlagsBuffer();

    group_count = -1;
    group_lengths = new int[100];

    // 1) compute number of particles per render-group
    for(ParticleGroup pg = world.getParticleGroupList(); pg != null; pg = pg.getNext()){
      int pg_start = pg.getBufferIndex();
      int pg_end   = pg_start + pg.getParticleCount();
      for(int i = pg_start; i < pg_end; i++){
        int idx = prgc.getRenderGroupIndex(i, pg, particle_flags[i]);
        // dynamically resize
        if(group_lengths.length <= idx){
          group_lengths = Arrays.copyOf(group_lengths, group_lengths.length * 2);
        }
        // increase counter for this group-index
        group_lengths[idx]++;
        group_count = Math.max(group_count, idx);
      }
    }
    ++group_count;
    group_lengths = Arrays.copyOf(group_lengths, group_count); // trim to real size
    
    
    
    // 2) allocate render-groups
    group_offsets = new int[group_count];
    buf_idx_len = 0;
    for(int i = 0; i < group_count; i++){
      group_offsets[i] = buf_idx_len;
      buf_idx_len += group_lengths[i];
      group_lengths[i] = 0; // reset
    }

    
    
    // 3) assign particle-indices to groups
    buf_idx = DwUtils.resizeBuffer(buf_idx, buf_idx_len);
    
    for(ParticleGroup pg = world.getParticleGroupList(); pg != null; pg = pg.getNext()){
      int pg_start = pg.getBufferIndex();
      int pg_end   = pg_start + pg.getParticleCount();
      for(int i = pg_start; i < pg_end; i++){
        int idx = prgc.getRenderGroupIndex(i, pg, particle_flags[i]);
        int pos = group_offsets[idx] + group_lengths[idx];
        group_lengths[idx]++;
        buf_idx[pos] = i;
      }
    }
    

    // 4) 
//    particlerender_gl.setIndexBuffer(buf_idx, buf_idx_len);
//    particlerender_gl.setIndexGroups(group_offsets, group_lengths, group_count);
  }
  
  
  
  
  protected DwParticleRenderGroupCallback prgc = null;
  
  public void setParticleRenderGroupCallback(DwParticleRenderGroupCallback callback){
    this.prgc = callback;
  }
  

  
  
  
}
