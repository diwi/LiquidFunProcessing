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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jbox2d.dynamics.World;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;
import processing.opengl.PShader;


/**
 * 
 * 
 * Box2D/LiquidFun particles renderer. <br>
 * Using low-level OpenGL calls to improve performance.<br>
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleRenderGL extends DwParticleRender{
  
  static public final String SHADER_DIR = "/com/thomasdiewald/liquidfun/glsl/";
  
  // GL
  protected GL2ES3 gl;
  protected PJOGL pgl;
  
  // Shader
  public PShader shader_particles;
  

  // GL: VBO HANDLES
  public int[] HANDLE_vbo_pos = {0};
//  public int[] HANDLE_vbo_vel = {0};
  public int[] HANDLE_vbo_col = {0};
//  public int[] HANDLE_vbo_con = {0};
  
  // GL: VBO Index Handle
  public int[] HANDLE_vbo_idx = {0};



  static public String NL = System.getProperty("line.separator");

  // vertex shader code for rendering point sprites
  static public String[] src_vert =
    {   ""
      , "#version 150                               " + NL
      , "                                           " + NL
      , "uniform mat4 mat_mvp;                      " + NL
      , "uniform float point_size;                  " + NL
      , "                                           " + NL
      , "in vec2 pos;                               " + NL
      , "in vec4 col;                               " + NL
      , "out vec2 posn;                             " + NL
      , "out vec4 tint;                             " + NL
      , "                                           " + NL
      , "void main() {                              " + NL
      , "  gl_Position = mat_mvp * vec4(pos, 0, 1); " + NL
      , "  gl_PointSize = point_size;               " + NL
      , "  tint = col;                              " + NL
      , "  posn = gl_Position.xy * 0.5 + 0.5;       " + NL
      , "}                                          " + NL
    }; 
  
  // fragment shader code for rendering point sprites
  static public String[] src_frag =
    {   ""
      , "#version 150                                             "+NL
      , "                                                         "+NL
      , "out vec4 fragColor;                                      "+NL
      , "in vec4 tint;                                            "+NL
      , "in vec2 posn;                                            "+NL
      , "                                                         "+NL
      , "uniform float point_size;                                "+NL
      , "uniform vec2 wh_viewport;                                "+NL
      , "uniform sampler2D tex_sprite;                            "+NL
      , "                                                         "+NL
      , "void main() {                                            "+NL
      , "  vec2 my_PointCoord = ((posn * wh_viewport) - gl_FragCoord.xy) / point_size + 0.5; // [0, 1]"+NL
      , "  fragColor = texture(tex_sprite, my_PointCoord) * tint; "+NL
      , "}                                                        "+NL
    }; 
  
  
  

  public DwParticleRenderGL(PApplet papplet, World world, DwViewportTransform transform){
    super(papplet, world, transform);
    
//    String[] src_frag = DwUtils.readASCIIfile(papplet, SHADER_DIR + "particle_render2.frag");
//    String[] src_vert = DwUtils.readASCIIfile(papplet, SHADER_DIR + "particle_render2.vert");
 
    shader_particles = new PShader(papplet, src_vert, src_frag);
  }
  

  @Override
  public void release(){
    beginGL();
    gl.glDeleteBuffers(1, HANDLE_vbo_idx, 0); HANDLE_vbo_idx[0] = 0;
    gl.glDeleteBuffers(1, HANDLE_vbo_pos, 0); HANDLE_vbo_pos[0] = 0;
    gl.glDeleteBuffers(1, HANDLE_vbo_col, 0); HANDLE_vbo_col[0] = 0;
//    gl.glDeleteBuffers(1, HANDLE_vbo_vel, 0); HANDLE_vbo_vel[0] = 0;
//    gl.glDeleteBuffers(1, HANDLE_vbo_con, 0); HANDLE_vbo_con[0] = 0;
    errCheck("DwParticleRenderGL.release");
    endGL();
    
    super.release();
  }
  
  
  @Override
  public void update(){
    generateParticleGroups();
    updateBuffers();
    updateVBOs();
  }
  
  
  protected void updateVBOs(){
    if(particle_num == 0){
      return;
    }
    
    beginGL();
    
    assureBuffers();
    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_pos[0]);
    gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_pos_len * 4, FloatBuffer.wrap(buf_pos), GL.GL_DYNAMIC_DRAW);
    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_col[0]);
    gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_col_len * 1, ByteBuffer.wrap(buf_col), GL.GL_DYNAMIC_DRAW);
    
//    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_vel[0]);
//    gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_vel_len * 4, FloatBuffer.wrap(buf_vel), GL.GL_DYNAMIC_DRAW);
    
//    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_con[0]);
//    gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_con_len * 4, FloatBuffer.wrap(buf_con), GL.GL_DYNAMIC_DRAW);
    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    
    
    if(USE_GROUPS)
    {
      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, HANDLE_vbo_idx[0]);
      gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, buf_idx_len * 4, IntBuffer.wrap(buf_idx), GL.GL_DYNAMIC_DRAW);
      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    errCheck("DwParticleRenderGL.updateVBOs");
    
    endGL();
  }
  
  
  protected void assureBuffers(){
    // VBO handles
    if(HANDLE_vbo_idx[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_idx, 0);
    if(HANDLE_vbo_pos[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_pos, 0);
    if(HANDLE_vbo_col[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_col, 0);
//    if(HANDLE_vbo_vel[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_vel, 0);
//    if(HANDLE_vbo_con[0] == 0) gl.glGenBuffers(1, HANDLE_vbo_con, 0);
  }
  
  
  
  @Override
  public void display(PGraphics2D canvas){
    
    if(particle_num == 0){
      return;
    }

    canvas.updateProjmodelview();
    PMatrix3D mat_mvp = canvas.projmodelview.get();
    mat_mvp.transpose();

    beginGL();
    
    assureBuffers();

    PShader shader = shader_particles;
    shader.bind();

    shader.set("mat_mvp", mat_mvp);
    shader.set("tex_sprite", param.tex_sprite);
    shader.set("point_size", particle_rad_screen * 2);
    shader.set("wh_viewport", (float)canvas.width, (float) canvas.height);
    
    errCheck("DwParticleRenderGL.display-uniforms");
    
    // shader vertex attribute: position
    int LOC_pos = gl.glGetAttribLocation(shader.glProgram, "pos");
    if(LOC_pos != -1){
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_pos[0]);
      gl.glEnableVertexAttribArray(LOC_pos);
      gl.glVertexAttribPointer(LOC_pos, 2, GL.GL_FLOAT, false, 0, 0);
    }
    
    // shader vertex attribute: color
    int LOC_col = gl.glGetAttribLocation(shader.glProgram, "col");
    if(LOC_col != -1){
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_col[0]);
      gl.glEnableVertexAttribArray(LOC_col);
      gl.glVertexAttribPointer(LOC_col, 4, GL.GL_UNSIGNED_BYTE, true, 0, 0);
    }
    
    // shader vertex attribute: velocity
//    int LOC_vel = gl.glGetAttribLocation(shader.glProgram, "vel");
//    if(LOC_vel != -1){
//      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_vel[0]);
//      gl.glEnableVertexAttribArray(LOC_vel);
//      gl.glVertexAttribPointer(LOC_vel, 2, GL.GL_FLOAT, false, 0, 0);
//    }
    
    // shader vertex attribute: contact
//    int LOC_con = gl.glGetAttribLocation(shader.glProgram, "con");
//    if(LOC_con != -1){
//      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, HANDLE_vbo_con[0]);
//      gl.glEnableVertexAttribArray(LOC_con);
//      gl.glVertexAttribPointer(LOC_con, 2, GL.GL_FLOAT, false, 0, 0);
//    }

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    errCheck("DwParticleRenderGL.display-buffers");



    // settings
    // fix: PJOGL.profile = 3; in settings()
    // https://github.com/diwi/LiquidFunProcessing/issues/2
    //  gl.glEnable(GL2.GL_POINT_SMOOTH);
    // gl.glEnable(GL3.GL_VERTEX_PROGRAM_POINT_SIZE);
    //  gl.glPointSize(particle_rad_screen * 2);
    
    // ... f***ing OpenGL mess
    gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
    // gl.glEnable(GL2.GL_POINT_SPRITE); // causes gl error

    errCheck("DwParticleRenderGL.display-pointRendering");
  
    
  
    // draw particles as points (see fragment shader for details)
    if(USE_GROUPS)
    {
      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, HANDLE_vbo_idx[0]);
      // gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, buf_idx_len * 4, IntBuffer.wrap(buf_idx), GL.GL_DYNAMIC_DRAW);

      for(int i = 0; i < group_ids.length; i++){
        int id = group_ids[i];
        if(id >= group_count){
          continue;
        }
        int off = group_offsets[id];
        int len = group_lengths[id];
        gl.glDrawElements(GL.GL_POINTS, len, GL.GL_UNSIGNED_INT, off * 4);
      }
      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
      errCheck("DwParticleRenderGL.display-glDrawElements");
    } 
    else {
      gl.glDrawArrays(GL.GL_POINTS, 0, particle_num);
      errCheck("DwParticleRenderGL.display-glDrawArrays");
    }
    
    
    // cleanup
    if(LOC_pos != -1) gl.glDisableVertexAttribArray(LOC_pos);
    if(LOC_col != -1) gl.glDisableVertexAttribArray(LOC_col);
//    if(LOC_vel != -1) gl.glDisableVertexAttribArray(LOC_vel);
//    if(LOC_con != -1) gl.glDisableVertexAttribArray(LOC_con);
    
    shader.unbind();
    errCheck("DwParticleRenderGL.shader.unbind()");

    endGL();
  }
  



  protected void beginGL(){
    if(gl == null){
      pgl = (PJOGL) papplet.beginPGL();  
      gl = pgl.gl.getGL2ES3();
    }
  }
  
  protected void endGL(){
    if(gl != null){
      papplet.endPGL();
      gl = null;
    }
  }
  
  protected void errCheck(String user_msg){
    int err = pgl.getError();
    if (err != 0) {
      String errString = pgl.errorString(err);
      String msg = "OpenGL error " + err + " at " + user_msg + ": " + errString;
      PGraphics.showWarning(msg);
    }
  }
  
  
}
