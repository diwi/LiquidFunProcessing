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

import org.jbox2d.dynamics.World;

import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix2D;
import processing.core.PShape;
import processing.opengl.PGraphics2D;


/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. <br>
 * Using PShape point-sprites for rendering.<br>
 * <br>
 * Performance is good, but DwParticleRenderGL is a lot faster.<br>
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
    super.release();
  }

  
  @Override
  public void update(){
    updateBuffers();
    updateShapesCount();
    updateShapesData();
  }
  
  
  @Override
  public void display(PGraphics2D canvas){
//    shp_particles.draw(canvas);
    canvas.shape(shp_particles);
  }
  
  
  protected void updateShapesCount(){
    
    if(particle_num == 0){
      return;
    }
    
    // synchronize buffer size
    int shp_count = shp_particles.getChildCount();
    if(shp_count != particle_num){
      
      // remove shapes, in case group is bigger than actual number
      for(int i = shp_count - 1; i >= particle_num; i--){
        shp_particles.removeChild(i);
      }
      
      // add shapes, in case group is smaller then actual number
      for(int i = shp_count; i < particle_num; i++){
        PShape shp_particle = papplet.createShape();
        shp_particle.beginShape(PConstants.QUADS);
        shp_particle.noFill();
        shp_particle.noStroke();
        shp_particle.textureMode(PConstants.NORMAL); // TODO: report issue
        shp_particle.texture(param.tex_sprite);      // TODO: report issue
        shp_particle.vertex(-1, -1,  0, 0);
        shp_particle.vertex(+1, -1,  1, 0);
        shp_particle.vertex(+1, +1,  1, 1);
        shp_particle.vertex(-1, +1,  0, 1);   
        shp_particle.endShape();
        shp_particles.addChild(shp_particle);
      }
    }
  }
  

  
  protected void updateShapesData(){
    
    if(particle_num == 0){
      return;
    }
  
    final PMatrix2D mat = new PMatrix2D();
    
    for (int i = 0, ipos = 0, icol = 0; i < particle_num; i++) {
      // position
      float pos_x = buf_pos[ipos++];
      float pos_y = buf_pos[ipos++];
      // velocity
//      float vel_x = buf_vel[ivel++];
//      float vel_y = buf_vel[ivel++];
      // tint
      int tint = DwUtils.createColorARGB(buf_col, icol); icol += 4;
      
      mat.reset();
      mat.translate(pos_x, pos_y);
      mat.scale(particle_rad_world);
//      mat.rotate((float)Math.atan2(vel_y, vel_x));
      
      PShape shp = shp_particles.getChild(i);
      shp.resetMatrix();
//      mat.translate(pos_x, pos_y);
//      mat.scale(particle_rad_world);
      shp.applyMatrix(mat);

      shp.setTint(tint);
    }
    
  }
  

  
  
  
  
  

}
