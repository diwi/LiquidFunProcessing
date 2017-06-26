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



package testbed;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleDestroyer;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;
import com.thomasdiewald.liquidfun.java.render.DwParticleRender;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderP5;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwParticleFluidFX;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class DebugDrawDemo extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;
  boolean APPLY_LIQUID_FX = true;
  
  DwWorld world;
  DwBodyGroup bodies;
  DwParticleRenderGL particlerender_gl;
  DwParticleRenderP5 particlerender_p5;

  DwParticleDestroyer  particle_destroyer;
  DwParticleSpawn      particle_spawn;
  
  DwPixelFlow pixelflow;
  DwParticleFluidFX particle_fluidfx;
  
  PGraphics2D pg_liquid;
  PGraphics2D pg_bodies;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    reset();
    frameRate(1000);
  }
  
  
  public void release(){
    if(bodies != null) bodies.release(); bodies = null;
    if(particlerender_gl != null) particlerender_gl.release(); particlerender_gl = null;
    if(particlerender_p5 != null) particlerender_p5.release(); particlerender_p5 = null;
    if(world     != null) world    .release(); world     = null;  
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    world = new DwWorld(this);
    
    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);
    particlerender_gl = new DwParticleRenderGL(this, world, world.transform);
    particlerender_p5 = new DwParticleRenderP5(this, world, world.transform);
    
    particle_destroyer  = new DwParticleDestroyer(world, world.transform);
    particle_spawn      = new DwParticleSpawn(world, world.transform);
    
    setParticleProperties(PARTICLE_TYPE_INDEX);

    pixelflow = new DwPixelFlow(this);
    particle_fluidfx = new DwParticleFluidFX(pixelflow);
    
    
    pg_liquid = (PGraphics2D) createGraphics(width, height, P2D);
    pg_liquid.smooth(0);

    pg_bodies = (PGraphics2D) createGraphics(width, height, P2D);
    pg_bodies.smooth(8);
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  
  


  //////////////////////////////////////////////////////////////////////////////
  // draw
  //////////////////////////////////////////////////////////////////////////////
  
  
  public void draw(){
    
    DwParticleRender particlerender = (keyPressed && key == 'h') ? particlerender_p5 : particlerender_gl;
    
    
    if(UPDATE_PHYSICS){
      mouseDrawAction();
      world.update();
      particlerender.update();
    }

 
    
    int BACKGROUND = 16;

    pg_liquid.beginDraw();
    pg_liquid.clear();
    pg_liquid.background(BACKGROUND, 0);
    pg_liquid.pushMatrix();
    world.applyTransform(pg_liquid);
    if(USE_DEBUG_DRAW){
      world.displayDebugDraw(pg_liquid);
      // DwDebugDraw.display(pg_liquid, world);
    } else {
//      bodies.display(pg_liquid);
      particlerender.display(pg_liquid);
    }
    pg_liquid.popMatrix();
    pg_liquid.endDraw();
    
    
    pg_bodies.beginDraw();
    pg_bodies.clear();
    pg_bodies.background(BACKGROUND, 0);
    pg_bodies.pushMatrix();
    world.applyTransform(pg_bodies);
    if(USE_DEBUG_DRAW){
      world.displayDebugDraw(pg_bodies);
      // DwDebugDraw.display(pg_bodies, world);
    } else {
      bodies.display(pg_bodies);
    }
    pg_bodies.popMatrix();
    pg_bodies.endDraw();
    
    
    
    if(APPLY_LIQUID_FX){
      particle_fluidfx.param.base_LoD = 1;
      particle_fluidfx.param.base_blur_radius = 2;
      particle_fluidfx.param.highlight_enabled = true;
      particle_fluidfx.param.highlight_LoD = 1;
      particle_fluidfx.param.highlight_decay = 0.6f;
      particle_fluidfx.param.sss_enabled = true;
      particle_fluidfx.param.sss_LoD = 3;
      particle_fluidfx.param.sss_decay = 0.5f;
      particle_fluidfx.apply(pg_liquid);
      
      particle_fluidfx.param.base_LoD = 0;
      particle_fluidfx.param.base_blur_radius = 1;
      particle_fluidfx.param.highlight_enabled = true;
      particle_fluidfx.param.highlight_LoD = 2;
      particle_fluidfx.param.highlight_decay = 0.9f;
      particle_fluidfx.param.sss_enabled = true;
      particle_fluidfx.param.sss_LoD = 1;
      particle_fluidfx.param.sss_decay = 0.7f;
      particle_fluidfx.apply(pg_bodies);
    }
    
    
//    DwFilter.get(pixelflow).gamma.apply(pg_liquid, pg_liquid, 1.4f);
//    DwFilter.get(pixelflow).gamma.apply(pg_bodies, pg_bodies, 1.4f);
    
    background(BACKGROUND);
    image(pg_liquid, 0, 0);
    image(pg_bodies, 0, 0);
    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }
  

  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////
  public void keyReleased(){
    if(key == 'r') reset();
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
    if(key == 'g') APPLY_LIQUID_FX = !APPLY_LIQUID_FX;
    if(key >= '1' && key <= '6') setParticleProperties(key - '1');
  }
  

  public void mousePressed() {
    if(mouseButton == CENTER){
      spawnParticles(mouseX, mouseY);
    }
    if(mouseButton == RIGHT){
      particle_destroyer.destroyParticles(mouseX, mouseY, 30);
    }
  }
  
  public void mouseDragged(){
    if(mouseButton == CENTER){
      spawnParticles(mouseX, mouseY);
    }
  }

  public void mouseDrawAction(){    
    if(mousePressed){
      if(mouseButton == RIGHT){
        particle_destroyer.destroyParticles(mouseX, mouseY, 30);
      }
    }
  }
  
  public void mouseReleased() {
    particle_spawn  .end(mouseX, mouseY);
  }
  
  

  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  public void initScene() {
    
    float scree_scale = world.transform.screen_scale;
    
    float b2d_screen_w = width / scree_scale;
    float b2d_screen_h = height / scree_scale;
    float b2d_thickness = 20 / scree_scale;
    
    {
      float radius = 30 / scree_scale;
      CircleShape circle_shape = new CircleShape();
      circle_shape.setRadius(radius);

      FixtureDef fixture_def = new FixtureDef();
      fixture_def.shape = circle_shape;
      fixture_def.density = 20f;
      fixture_def.friction = 0.30f;
      fixture_def.restitution = 0.30f;
      
      BodyDef body_def = new BodyDef();
      body_def.type = BodyType.DYNAMIC;
      body_def.angle = 0.0f;
      body_def.position.x = -1/scree_scale;
      body_def.position.y = b2d_screen_h - 10;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      circle_body.createFixture(fixture_def);
      
      bodies.add(circle_body, true, color(255,220, 0), true, color(0), 1f);
    }
    
    { // Walls
      BodyDef bd = new BodyDef();
      bd.position.set(0, 0);

      Body ground = world.createBody(bd);
      PolygonShape sd = new PolygonShape();
      
      float x, y, w, h;

      // BOTTOM
      x = 0;
      y = 0;
      w = b2d_screen_w;
      h = b2d_thickness;
     
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);

      // TOP
      x = 0;
      y = b2d_screen_h;
      w = b2d_screen_w;
      h = b2d_thickness;
      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      // LEFT
      x = -b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h - b2d_thickness;

      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      // RIGHT
      x = +b2d_screen_w/2;
      y = +b2d_screen_h/2;
      w = b2d_thickness;
      h = b2d_screen_h - b2d_thickness;

      sd.setAsBox(w/2f, h/2f, new Vec2(x, y), 0.0f);
      ground.createFixture(sd, 0f);
      
      bodies.add(ground, true, color(0), !true, color(0), 1f);
    }

    createWall(8, 20, 60, 25, 240, 10);

    
    
    // Particles
    float box_w = 15;
    float box_h = 16;

    PolygonShape shape = new PolygonShape();
    ParticleGroupDef def = new ParticleGroupDef();

    def.shape = shape;
    def.flags = 0
        | ParticleType.b2_waterParticle
        | ParticleType.b2_viscousParticle
//        | ParticleType.b2_tensileParticle
//        | ParticleType.b2_colorMixingParticle
        ;
    def.groupFlags = 0;
    
//    def.color = new ParticleColor(new Color3f(1f, 0.35f, 0.1f));
    def.color = new ParticleColor(new Color3f(0.05f, 0.2f, 1));
    shape.setAsBox(box_w, box_h, new Vec2(-(box_w*1.5f), box_h*1.5f), 0);
    world.createParticleGroup(def);
  }
  
  
  
  
  
  
  

  public void createWall(int numx, int numy, float dimx, float dimy, float tx, float ty){
    
    float scree_scale = world.transform.screen_scale;
    
    
    dimx /= scree_scale;
    dimy /= scree_scale;
    
    tx /= scree_scale;
    ty /= scree_scale;
    
    PolygonShape cube = new PolygonShape();
    cube.setAsBox(dimx*0.5f, dimy*0.5f);
    
    FixtureDef fixture_def = new FixtureDef();
    fixture_def.shape = cube;
    fixture_def.density = 30;
    fixture_def.friction = 0.50f;
    fixture_def.restitution = 0.5f;
    
    BodyDef body_def = new BodyDef();
    body_def.type = BodyType.DYNAMIC;
    body_def.angle = 0.0f;
    body_def.allowSleep = true;
    
    
    int scol = color(0);
    int fcol = color(224,128,64);

    colorMode(HSB, 360,100,100);
    
    randomSeed(1);
    
    float ox = 0;
    float oy = dimy/2;
    for(int y = 0; y < numy; y++){
      
      float off = 0.5f;
      
      for(int x = 0; x < numx; x++){
        boolean odd_row = (y & 1) == 1;
//        if(odd_row && x == numx-1) continue;
        
//        if(random(1) < 0.1) continue;

        ox = -numx * dimx * 0.5f;
        ox += odd_row ? dimx * off : 0;
        
        body_def.position.x = tx + ox + x * (dimx);
        body_def.position.y = ty + oy + y * (dimy);
   
        Body brick = world.createBody(body_def);
        brick.createFixture(fixture_def);
        
        float hsb_h = 20 + random(-3, 3);
        float hsb_s = random(70,60);
        float hsb_b = random(70,100);
        fcol = color(hsb_h, hsb_s, hsb_b);
        
        bodies.add(brick, true, fcol, true, scol, 0.5f);
      }
    }
    
    colorMode(RGB, 255);
   
  }
  
  
  
  
  int PARTICLE_TYPE_INDEX = 0;
  int particle_color_hue = 5;
  
 
  public void setParticleProperties(int type){
    
    PARTICLE_TYPE_INDEX = type;
    
    int COLOR_MIX = 0;
    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    particle_spawn.group_def.flags = 0;
    particle_spawn.group_def.groupFlags = 0;
    
    int col_dif = 60;
    int col_off = 5;

    switch (PARTICLE_TYPE_INDEX) {
 
      case 0:
        particle_color_hue = 0 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_color_hue = 1 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        break;
      case 2:
        particle_color_hue = 2 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        break;
      case 3:
        particle_color_hue = 3 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        break;
      case 4:
        particle_color_hue = 4 * col_dif + col_off;
        particle_spawn.group_def.groupFlags =  ParticleGroupType.b2_rigidParticleGroup | ParticleGroupType.b2_solidParticleGroup;
        break;
      case 5:
        particle_color_hue = 5 * col_dif + col_off;
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        break;
      
    }
    
    
    particle_color_hue %= 360;
    particle_spawn.group_def.color = new ParticleColor(createHSBColor(particle_color_hue, 90, 90));
  }
  
  
  
  
  
  
  public void spawnParticles(float screen_x, float screen_y){
    float screen_radius = 20;
    particle_spawn.join_groups = false;
    if((particle_spawn.group_def.groupFlags & ParticleGroupType.b2_solidParticleGroup) != 0){
      particle_spawn.join_groups = true;
      screen_radius = 10;
    }
    particle_spawn.setCircleShape(screen_radius);
    particle_spawn.spawn(screen_x, screen_y);
  }
  
  
  public void genParticleColors(ParticleGroup group, int hue){
    int group_size = group.getParticleCount();
    int group_start = group.getBufferIndex();
    genParticleColors(group_start, group_start+group_size, hue);
  }
  
  public void genParticleColors(int idx_from, int idx_to, int hue){
    
    int             particle_num = world.getParticleCount();
    ParticleColor[] particle_col = world.getParticleColorBuffer();
    idx_to = Math.min(idx_to, particle_num);
    
    colorMode(HSB, 360, 100, 100);
    for(int i = idx_from; i < idx_to; i++){ 
      float hsb_h = hue + random(-15, 15);
      float hsb_s = random(20, 100);
      float hsb_b = random(40, 100);
      int   argb  = color(hsb_h, hsb_s, hsb_b);
      
      particle_col[i].a = (byte) ((argb >> 24) & 0xFF);
      particle_col[i].r = (byte) ((argb >> 16) & 0xFF);
      particle_col[i].g = (byte) ((argb >>  8) & 0xFF);
      particle_col[i].b = (byte) ((argb >>  0) & 0xFF);
    }
    colorMode(RGB, 255, 255, 255);
  }
  
  
  
  Color3f createHSBColor(float hsb_h, float hsb_s, float hsb_b){
    colorMode(HSB, 360, 100, 100);
    int rgb = color(hsb_h, hsb_s, hsb_b);
    colorMode(RGB, 255, 255, 255);
    
    float r = ((rgb >> 16) & 0xFF) / 255f;
    float g = ((rgb >>  8) & 0xFF) / 255f;
    float b = ((rgb >>  0) & 0xFF) / 255f;
    return new Color3f(r,g,b);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { DebugDrawDemo.class.getName() });
  }
  
}