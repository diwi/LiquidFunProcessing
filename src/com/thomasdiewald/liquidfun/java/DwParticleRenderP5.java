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
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.opengl.PGraphics2D;

/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. 
 * Using PShape point-sprites for rendering.
 * 
 * Performance is good, but DwParticleRenderGL is a lot faster.
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleRenderP5 extends DwParticleRender{
 
  public PShape shp_particles;
  
  public DwParticleRenderP5(PApplet papplet, World world, DwViewportTransform transform){
    super(papplet, world, transform);
    shp_particles = papplet.createShape(PConstants.GROUP);
  }
  
  @Override
  public void release(){
  }

  
  @Override
  public void update(){
    updateBuffers();
    updateShapesCount();
    updateShapesData();
  }
  
  
  @Override
  public void display(PGraphics2D canvas){
    shp_particles.draw(canvas);
  }
  
  
  
  protected void updateShapesCount(){
    if(particle_num == 0){
      return;
    }
    
    // synchronize buffer size
    int shp_count = shp_particles.getChildCount();
    if(shp_count != particle_num){
      
      float particle_rad = world.getParticleRadius() * param.radius_scale;
      // remove shapes, in case group is bigger than actual number
      for(int i = shp_count - 1; i >= particle_num; i--){
        shp_particles.removeChild(i);
      }
      
      // add shapes, in case group is smaller then actual number
      for(int i = shp_count; i < particle_num; i++){
        PShape shp_particle = papplet.createShape();
        shp_particle.beginShape(PConstants.QUAD);
        shp_particle.noFill();
        shp_particle.noStroke();
        shp_particle.textureMode(PConstants.NORMAL);
        shp_particle.texture(param.tex_sprite);
        shp_particle.vertex(-particle_rad, -particle_rad, 0, 0);
        shp_particle.vertex(+particle_rad, -particle_rad, 1, 0);
        shp_particle.vertex(+particle_rad, +particle_rad, 1, 1);
        shp_particle.vertex(-particle_rad, +particle_rad, 0, 1);    
        shp_particle.endShape();    
        shp_particles.addChild(shp_particle);
      }
    }
  }
  
  
  protected void updateShapesData(){
    int idx_vel = 0;
    int idx_pos = 0;
    int idx_col = 0;
    int idx_con = 0;
    
    for (int i = 0; i < particle_num; i++) {
      
      float pos_x = buf_pos[idx_pos++];
      float pos_y = buf_pos[idx_pos++];

      float vel_x = buf_vel[idx_vel++];
      float vel_y = buf_vel[idx_vel++];
  
      int col_r = buf_col[idx_col++] & 0xFF;
      int col_g = buf_col[idx_col++] & 0xFF;
      int col_b = buf_col[idx_col++] & 0xFF;
      int col_a = buf_col[idx_col++] & 0xFF;
      
      float con_x = buf_con[idx_con++];
      float con_y = buf_con[idx_con++];
      
      float vel_mag = (float) Math.sqrt(vel_x * vel_x + vel_y * vel_y) / 100.0f;
      float con_mult = con_x / 50.0f;
      
      float sum = 1.0f + (con_mult + vel_mag);
      sum *= sum;
      sum *= sum;
      sum *= sum;

      col_r = Math.round(DwUtils.clamp(col_r * sum, 0, 255));
      col_g = Math.round(DwUtils.clamp(col_g * sum, 0, 255));
      col_b = Math.round(DwUtils.clamp(col_b * sum, 0, 255));
 
      int col = col_a << 24 | col_r << 16 | col_g << 8 | col_b;
      
      PShape shp = shp_particles.getChild(i);
      shp.resetMatrix();
//      shp.rotate((float)Math.atan2(vel_y, vel_x));
      shp.translate(pos_x, pos_y);
      shp.setTint(col);
    }
    
  }
  
  
  
  
  

}
