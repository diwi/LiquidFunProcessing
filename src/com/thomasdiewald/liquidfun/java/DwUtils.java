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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleType;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PGraphics2D;



/**
 * 
 * Helper Utils.
 * 
 * @author Thomas Diewald
 *
 */
public class DwUtils {

  @Deprecated
  static final public void updateBodyShapeTransforms(World world){
    
    Object obj = null;

    for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
      Transform xf = body.getTransform();
      float rz = xf.q.getAngle();
      float tx = xf.p.x;
      float ty = xf.p.y;
      
      obj = body.getUserData();
      if(obj != null && obj instanceof PShape){
        PShape shp = (PShape) obj;
        shp.resetMatrix();
        shp.rotate(rz);
        shp.translate(tx, ty);
      } 
      else {

        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
          obj = fixture.getUserData();
          if(obj != null && obj instanceof PShape){
            PShape shp = (PShape) obj;
            shp.resetMatrix();
            shp.rotate(rz);
            shp.translate(tx, ty);
          }
        }
      }
    }
  }
  

  
  
  @Deprecated
  static final public void updateParticleShapeTransforms(World world, PShape group_particles){
    updateParticleShapeTransforms(world, group_particles, false);
  }


  
  
  
  
  @Deprecated
  static final public void updateParticleShapeTransforms(World world, PShape group_particles, boolean rotate){
    
    int shp_count = group_particles.getChildCount();
    if(shp_count == 0){
      return;
    }
    PShape[] shp_particles = group_particles.getChildren();
    
    int count = world.getParticleCount();
    if (count == 0) return;

    if( count != shp_count ){
      System.out.println("ERROR: Box2dUtils.updateParticleShapeTransforms: particles numbers dont match "+count+" != "+shp_count);
      count = Math.min(count, shp_count);
    }
    
    Vec2[] pos = world.getParticlePositionBuffer();
    Vec2[] vel = world.getParticleVelocityBuffer();
    
    if(rotate){
      for (int i = 0; i < count; i++) {
        shp_particles[i].resetMatrix();
        shp_particles[i].rotate((float)Math.atan2(vel[i].y, vel[i].x));
        shp_particles[i].translate(pos[i].x, pos[i].y);
      }
    } else {
      for (int i = 0; i < count; i++) {
        shp_particles[i].resetMatrix();
        shp_particles[i].translate(pos[i].x, pos[i].y);
      }
    }
  }
  
  
  
  @Deprecated
  static final public int updateParticleShapeBuffer(World world, PShape group_particles){
    int particle_num_old = world.getParticleCount();
    int particle_num_new = 0;
    
    if(particle_num_old == 0){
      return 0;
    }
    
    if(group_particles.getChildCount() == 0){
      return 0;
    }
    
    // The following is way faster, then removing shapes in place.
    // ... even when iterating backwards, a lot of repeated/unnecessary copying 
    // will be needed.

    PShape group = group_particles;
    PShape[] shapes = group.getChildren();
    int[] particle_flag = world.getParticleFlagsBuffer();
    

    // re-map particles, so that zombies are left out
    for(int i = 0; i < particle_num_old; i++){
      if((particle_flag[i] & ParticleType.b2_zombieParticle) == 0){
        shapes[particle_num_new++] = shapes[i];
      }
    }
    
    // remove number of zombies by triming the buffer to the new size
    // unfortunately PShape offers no better way to do this
    for(int i = particle_num_old - 1; i >= particle_num_new; i--){
      group.removeChild(i);
    }
    
    
    int removed = particle_num_old - particle_num_new;
    
    return removed;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  static public PImage createSprite(PApplet papplet, int size, int PARTICLE_SHAPE_IDX){
    size = Math.max(32, size);
    
    PImage pimg = papplet.createImage(size, size, PConstants.ARGB);
    pimg.loadPixels();
    
    float center_x = size/2f;
    float center_y = size/2f;
    
    for(int y = 0; y < size; y++){
      for(int x = 0; x < size; x++){
        int pid = y * size + x;
        
        float dx = center_x - (x + 0.5f);
        float dy = center_y - (y + 0.5f);
        float dd = (float)Math.sqrt(dx*dx + dy*dy);
        dd /= size * 0.5f; // normalize
        
        // DISC
        if(PARTICLE_SHAPE_IDX == 0){
          if(dd<0) dd=0; else if(dd>1) dd=1;
          dd = dd*dd; dd = dd*dd; dd = dd*dd;
      
          dd = 1.0f - dd;
          int a = (int)(dd*255);
          pimg.pixels[pid] = a << 24 | 0x00FFFFFF;
        }
        // SPOT
        else if(PARTICLE_SHAPE_IDX == 1){
          if(dd<0) dd=0; else if(dd>1) dd=1;
          dd = 1-dd;
//          dd = dd*dd;
          int a = (int)(dd*255);
          pimg.pixels[pid] = a << 24 | 0x00FFFFFF;
        }
        // DONUT
        else if(PARTICLE_SHAPE_IDX == 2){
          dd = Math.abs(0.6f - dd);
          dd *= 1.8f;
          dd = 1-dd;
          dd = dd*dd*dd;
          if(dd<0) dd=0; else if(dd>1) dd=1;
          int a = (int)(dd*255);
          pimg.pixels[pid] = a << 24 | 0x00FFFFFF;
        }
        // RECT
        else if(PARTICLE_SHAPE_IDX == 3){
          int a = 255;
          if(Math.abs(dx) < size/3f && Math.abs(dy) < size/3f) a = 0;
          pimg.pixels[pid] = a << 24 | 0x00FFFFFF;
        } else {
          pimg.pixels[pid] = 0;
        }
        
      }
    }
    pimg.updatePixels();
 
    return pimg;
  }
  
  
  

  static public PImage createSprite(PApplet papplet, int size, float exp1, float exp2, float mult){
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
        dd = (float) Math.pow(dd, exp1);
        dd = 1.0f - dd;
        dd = (float) Math.pow(dd, exp2);
        dd *= mult;
        pimg.pixels[pid] = ((int)(dd * 255)) << 24 | 0x00FFFFFF;
      }
    }
    pimg.updatePixels();
    return pimg;
  }
  
  
  
  static public PGraphics2D createCheckerBoard(PApplet papplet, int dimx, int dimy, int size, int colA, int colB){
    
    int num_x = (int) dimx/size;
    int num_y = (int) dimy/size;
    
    int off_x = (dimx - size *  num_x) / 2;
    int off_y = (dimy - size *  num_y) / 2;

    PGraphics2D pg = (PGraphics2D) papplet.createGraphics(dimx, dimy, PConstants.P2D);
    pg.smooth(0);
    pg.beginDraw();
    pg.blendMode(PConstants.REPLACE);
    pg.textureSampling(2);
    pg.noStroke();
    pg.fill(200);
    for(int y = -1; y < num_y+1; y++){
      for(int x = -1; x < num_x+1; x++){
        int px = off_x + x * size;
        int py = off_y + y * size;
        
        int col = (x ^ y) & 1;
        
        if(col == 1){
          pg.fill(colA);
        } else {
          pg.fill(colB);
        }

        pg.rect(px, py, size, size);
      }
    }
    
    pg.endDraw();
    
    
    return pg;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  final static public String NL = System.getProperty("line.separator");
  



  static public String[] readASCIIfile(InputStream inputstream) {
    BufferedReader reader = null;

    int num_lines = 0;
    String[] lines = new String[2048];

    try {
      reader = new BufferedReader(new InputStreamReader(inputstream));
      String line = null;

      while ((line = reader.readLine()) != null) {
        if (num_lines == lines.length) {
          lines = Arrays.copyOf(lines, num_lines << 1);
        }
        lines[num_lines++] = line;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return Arrays.copyOf(lines, num_lines);
  }

  
  
  
 
  
  
  static public boolean DEBUG = !true;
  
  
  static public InputStream createInputStream(PApplet papplet, String path) {

    InputStream inputstream = null;
    
    if (inputstream == null) {
      File file = new File(path);
      if(file.exists()){
        try {
          inputstream = new FileInputStream(file);
          if(DEBUG)System.out.println("v0 path: " + file);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }
    }

    
    if (inputstream == null) {
      URL url = DwUtils.class.getClassLoader().getResource(path);
      if(url != null){
        inputstream = DwUtils.class.getClassLoader().getResourceAsStream(path);
        if (inputstream != null) {
          if(DEBUG)System.out.println("v0 url: " + url.getFile());
        }
      }
    }
    

    if (inputstream == null) {
      URL url = papplet.getClass().getResource(path);
      if(url != null){
        inputstream = papplet.getClass().getResourceAsStream(path);
        if (inputstream != null) {
          if(DEBUG)System.out.println("v1 path: " + url);
        } 
      }
    }

    
    // no success so far, so try the processing way (slower)
    if (inputstream == null) {
      inputstream = papplet.createInput(path);
      if(inputstream != null){
        if(DEBUG)System.out.println("v2 path: "+path);
      }
    }

    
    if (inputstream == null) {
      System.out.println("DwUtils ERROR: could not create inputstream for " + path);
    }

    return inputstream;
  }

  
  
  
  
  
  static public String[] readASCIIfile(PApplet papplet, String path) {
    InputStream inputstream = createInputStream(papplet,path);
    String[] lines = readASCIIfile(inputstream);
    return lines;
  }
  
  static public String[] readASCIIfileNL(PApplet papplet, String path) {
    String[] lines = readASCIIfile(papplet, path);
    for(int i = 0; i < lines.length; i++){
      lines[i] += DwUtils.NL;
    }
    return lines;
  }

  

  

  
  
  
  
  final static public float TO_RAD = (float) (Math.PI / 180.0);
  
  final static public double _1_DIV_3 = 1.0 / 3.0;
  

  final static public int log2ceil(double val){
    return (int) Math.ceil(Math.log(val)/Math.log(2));
  }
  
  final static public float mix(float a, float b, float mix){
    return a * (1f-mix) + b * (mix);
  }
  
  
  /**
   * allocates a new int[] array only if buffer.length < len
   */
  static final public int[] resizeBuffer(int[] buffer, int len){
    if(buffer == null) return new int[len];
    return (buffer.length >= len) ? buffer : new int[(int) Math.ceil(len * 1.5f)];
  }
  /**
   * allocates a new float[] array only if buffer.length < len
   */
  static final public float[] resizeBuffer(float[] buffer, int len){
    if(buffer == null) return new float[len];
    return (buffer.length >= len) ? buffer : new float[(int) Math.ceil(len * 1.5f)];
  }
  /**
   * allocates a new byte[] array only if buffer.length < len
   */
  static final public byte[] resizeBuffer(byte[] buffer, int len){
    if(buffer == null) return new byte[len];
    return (buffer.length >= len) ? buffer : new byte[(int) Math.ceil(len * 1.5f)];
  }
  
  
  static final public float clamp(float val, float lo, float hi){
    return (val < lo) ? lo : (val > hi) ? hi : val;
  }
  
  
  static final public int createColorARGB(byte[] bbuf, int off){
    int r = bbuf[off++] & 0xFF;
    int g = bbuf[off++] & 0xFF;
    int b = bbuf[off++] & 0xFF;
    int a = bbuf[off++] & 0xFF;
    return (a << 24 | r << 16 | g << 8 | b);
  }
  
  
  
  
//https://github.com/diwi/PixelFlow/blob/master/src/com/thomasdiewald/pixelflow/java/sampling/DwSampling.java
  static public double halton(int index, int base){
    double result = 0;
    double f = 1f / base;
    int i = index;
    while (i > 0){
      result += f * (i % base);
      i /= base;
      f /= base;
    }
    return result;
  }
  
  // https://github.com/diwi/PixelFlow/blob/master/src/com/thomasdiewald/pixelflow/java/sampling/DwSampling.java
  public static float[] sampleDisk_Halton(int index, float pow_dist){
    double phi = halton(index, 2) * Math.PI * 2 ;
    double rnd = halton(index, 3);
    double rad = Math.pow(rnd, pow_dist);
    double X   = Math.cos(phi) * rad;
    double Y   = Math.sin(phi) * rad;
    return new float[]{(float)X,(float)Y};
  }
  
  
  

}

