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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.jbox2d.dynamics.World;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL3;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;
import processing.opengl.PShader;


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
public class DwParticleRenderGL extends DwParticleRender{
  
  static public final String SHADER_DIR = "/com/thomasdiewald/liquidfun/glsl/";
  
  // GL
  public GL3 gl;
  
  // Shader
  public PShader shader_particles;

  // GL: VBO HANDLES
  public int[] HANDLE_vbo_pos = {0};
  public int[] HANDLE_vbo_vel = {0};
  public int[] HANDLE_vbo_col = {0};
  public int[] HANDLE_vbo_con = {0};
  

  public DwParticleRenderGL(PApplet papplet, World world, DwViewportTransform transform){
    super(papplet, world, transform);
    
    String[] src_frag = DwUtils.readASCIIfile(papplet, SHADER_DIR + "particle_render.frag");
    String[] src_vert = DwUtils.readASCIIfile(papplet, SHADER_DIR + "particle_render.vert");
 
    shader_particles = new PShader(papplet, src_vert, src_frag);
  }
  

  @Override
  public void release(){
    beginGL();
    gl.glDeleteBuffers(1, HANDLE_vbo_pos, 0); HANDLE_vbo_pos[0] = 0;
    gl.glDeleteBuffers(1, HANDLE_vbo_vel, 0); HANDLE_vbo_vel[0] = 0;
    gl.glDeleteBuffers(1, HANDLE_vbo_col, 0); HANDLE_vbo_col[0] = 0;
    gl.glDeleteBuffers(1, HANDLE_vbo_con, 0); HANDLE_vbo_con[0] = 0;
    endGL();
  }
  
  
  @Override
  public void update(){
    updateBuffers();
  }
  
  
  @Override
  public void display(PGraphics2D canvas){

    beginGL();
    
    // VBO handles
    if(HANDLE_vbo_pos[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_pos, 0);
    if(HANDLE_vbo_vel[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_vel, 0);
    if(HANDLE_vbo_col[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_col, 0);
    if(HANDLE_vbo_con[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_con, 0);
    
    boolean use_sprite = param.tex_sprite != null;
    

    PShader shader = shader_particles;

    // Get the location of the attribute variables.
    shader.bind();

    // shader uniforms
    canvas.updateProjmodelview();
    PMatrix3D mat_mvp = canvas.projmodelview.get();
    mat_mvp.transpose();
    float point_size = particle_rad * 2 * transform.screen_scale * param.radius_scale;

    shader.set("mat_mvp", mat_mvp);
    // shader_particles.set("point_size", point_size);
    shader.set("falloff_exp1" , param.falloff_exp1);
    shader.set("falloff_exp2" , param.falloff_exp2);
    shader.set("falloff_mult" , param.falloff_mult);
    shader.set("color_mult"   , param.color_mult);
    shader.set("use_sprite"   , use_sprite ? 1f : 0f);
    if(use_sprite){
      shader.set("tex_sprite", param.tex_sprite);
    }
    
    // shader vertex attribute: position
    int LOC_pos = gl.glGetAttribLocation(shader.glProgram, "pos");
    if(LOC_pos != -1){
      gl.glEnableVertexAttribArray(LOC_pos);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_pos[0]);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_pos_len * 4, FloatBuffer.wrap(buf_pos), GL.GL_DYNAMIC_DRAW);
      gl.glVertexAttribPointer(LOC_pos, 2, GL.GL_FLOAT, false, 0, 0);
    }
    
    // shader vertex attribute: velocity
    int LOC_vel = gl.glGetAttribLocation(shader.glProgram, "vel");
    if(LOC_vel != -1){
      gl.glEnableVertexAttribArray(LOC_vel);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_vel[0]);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_vel_len * 4, FloatBuffer.wrap(buf_vel), GL.GL_DYNAMIC_DRAW);
      gl.glVertexAttribPointer(LOC_vel, 2, GL.GL_FLOAT, false, 0, 0);
    }
    
    // shader vertex attribute: color
    int LOC_col = gl.glGetAttribLocation(shader.glProgram, "col");
    if(LOC_col != -1){
      gl.glEnableVertexAttribArray(LOC_col);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_col[0]);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_col_len * 1, ByteBuffer.wrap(buf_col), GL.GL_DYNAMIC_DRAW);
      gl.glVertexAttribPointer(LOC_col, 4, GL.GL_UNSIGNED_BYTE, true, 0, 0);
    }
    
    // shader vertex attribute: contaxt
    int LOC_con = gl.glGetAttribLocation(shader.glProgram, "con");
    if(LOC_con != -1){
      gl.glEnableVertexAttribArray(LOC_con);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_con[0]);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_con_len * 4, FloatBuffer.wrap(buf_con), GL.GL_DYNAMIC_DRAW);
      gl.glVertexAttribPointer(LOC_con, 2, GL.GL_FLOAT, false, 0, 0);
    }

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    
    // settings
//    gl.glEnable(GL.GL_MULTISAMPLE );
//    gl.glEnable(GL2.GL_POINT_SMOOTH);
    gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
    gl.glEnable(GL3.GL_VERTEX_PROGRAM_POINT_SIZE);
    gl.glEnable(GL2ES1.GL_POINT_SPRITE);
    gl.glPointSize(point_size);
  

    // draw particles as points (see fragment shader for details)
    gl.glDrawArrays(GL3.GL_POINTS, 0, particle_num);

    // cleanup
    if(LOC_pos != -1) gl.glDisableVertexAttribArray(LOC_pos);
    if(LOC_vel != -1) gl.glDisableVertexAttribArray(LOC_vel);
    if(LOC_col != -1) gl.glDisableVertexAttribArray(LOC_col);
    if(LOC_con != -1) gl.glDisableVertexAttribArray(LOC_con);
    
    shader.unbind();
    
    endGL();
  }
  
  

  
  // Utils
  
  protected void beginGL(){
    if(gl == null){
      PJOGL pgl = (PJOGL) papplet.beginPGL();  
      gl = pgl.gl.getGL3();
    }
  }
  
  protected void endGL(){
    if(gl != null){
      papplet.endPGL();
      gl = null;
    }
  }
  
  
  
  

  
  
  
}
