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



package liquidfun_ParticleRenderGroups_LiquidFx;


import com.thomasdiewald.liquidfun.java.DwUtils;
import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGroupCallback;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwLiquidFX;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;
import processing.opengl.PJOGL;


public class liquidfun_ParticleRenderGroups_LiquidFx extends PApplet {

  
  //
  // This Examples shows how to use different RenderGroups.
  // 
  // To separate particles during the rendering-stage, they can be separated into
  // groups, given different ID's, by implementing ParticleRenderGroupCallback.
  //
  // In this example, rigid wall-particles are rendered in its own pass in its 
  // own rendertarget to apply its own postprocessing.
  // 
  //
  // required libraries:
  //  - PixelFlow, https://github.com/diwi/PixelFlow
  //
  //
  // Controls:
  //
  // LMB         ... drag bodies
  // LMB + SHIFT ... shoot bullet
  // MMB         ... add particles
  // RMB         ... remove particles
  // 'r'         ... reset
  // 't'         ... update/pause physics
  // 'f'         ... toggle debug draw
  // 'g'         ... toggle DwLiquidFX
  //
  
  
  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;
  boolean APPLY_LIQUID_FX = true;
  
  DwWorld world;

  DwPixelFlow pixelflow;
  DwLiquidFX particle_fluidfx;
  
  PGraphics2D pg_liquid;
  PGraphics2D pg_prigid;
  PGraphics2D pg_bodies;
  PGraphics2D pg_checkerboard;
  
  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
    PJOGL.profile = 3;
  }
  
  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);
    pg_checkerboard = DwUtils.createCheckerBoard(this, width, height, 100, color(32, 0), color(48,0));
//    pg_checkerboard = DwUtils.createCheckerBoard(this, width, height, 100, color(200, 0), color(255,0));
    reset();
    frameRate(120);
  }
  
  
  public void release(){
    if(world != null) world.release(); world = null;  
  }
  
  
  public void reset(){
    // release old resources
    release();
    
    
    // liquid effect
    pixelflow = new DwPixelFlow(this);
    particle_fluidfx = new DwLiquidFX(pixelflow);
    
    // different render-targets for different bodies/particles/etc...
    pg_liquid = (PGraphics2D) createGraphics(width, height, P2D);
    pg_liquid.smooth(0);

    pg_prigid = (PGraphics2D) createGraphics(width, height, P2D);
    pg_prigid.smooth(0);
    
    pg_bodies = (PGraphics2D) createGraphics(width, height, P2D);
    pg_bodies.smooth(8);
    

    

    // box2d/liquidfun world
    world = new DwWorld(this, 15);
    
    // particle render settings
    
    world.particles.param.falloff_exp1 = 3f;
    world.particles.param.falloff_exp2 = 1f;
    world.particles.param.radius_scale = 2;
    
    // callback to group particles by their properties (flags)
    world.particles.setParticleRenderGroupCallback(new DwParticleRenderGroupCallback() {
      @Override
      public int getRenderGroupIndex(int particle_idx, ParticleGroup group, int particle_flag) {
        int group_flag = group.getGroupFlags();
        
        boolean is_wall  = (particle_flag & ParticleType     .b2_wallParticle      ) != 0;
        boolean is_solid = (group_flag    & ParticleGroupType.b2_solidParticleGroup) != 0;
        boolean is_rigid = (group_flag    & ParticleGroupType.b2_rigidParticleGroup) != 0;

        int group_id = 0;
        if(is_wall || is_solid || is_rigid){
          group_id = 1;
        } else {
          group_id = 0;
        }
        return group_id;
      }
    });
    

   
    setParticleSpawnProperties(spawn_type);
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }
  



  
 
  //////////////////////////////////////////////////////////////////////////////
  // draw
  //////////////////////////////////////////////////////////////////////////////
  
  public void draw(){
    
    if(UPDATE_PHYSICS){
      world.update();
    }


    int BACKGROUND = 128;
    
    if(USE_DEBUG_DRAW){
//      background(BACKGROUND);
      blendMode(REPLACE);
      image(pg_checkerboard, 0, 0);
      blendMode(BLEND);
      pushMatrix();
      world.applyTransform(this.g);
      world.drawBulletSpawnTrack(this.g);
      world.displayDebugDraw(this.g);
      popMatrix();
    } else {
      
      world.particles.useGroups(true);
      
      pg_liquid.beginDraw();
      pg_liquid.clear();
//      pg_liquid.background(BACKGROUND, 0);
      pg_liquid.blendMode(REPLACE);
      pg_liquid.image(pg_checkerboard, 0, 0);
      pg_liquid.blendMode(BLEND);
      world.applyTransform(pg_liquid);
      world.particles.display(pg_liquid, 0);
      pg_liquid.endDraw();
      
      pg_prigid.beginDraw();
      pg_prigid.clear();
//      pg_prigid.background(BACKGROUND, 0);
      pg_prigid.blendMode(REPLACE);
      pg_prigid.image(pg_checkerboard, 0, 0);
      pg_prigid.blendMode(BLEND);
      world.applyTransform(pg_prigid);
      world.particles.display(pg_prigid, 1);
      pg_prigid.endDraw();
      
      pg_bodies.beginDraw();
      pg_bodies.clear();
//      pg_bodies.background(BACKGROUND, 0);
      pg_bodies.blendMode(REPLACE);
      pg_bodies.image(pg_checkerboard, 0, 0);
      pg_bodies.blendMode(BLEND);
      world.applyTransform(pg_bodies);
      world.bodies.display(pg_bodies);
      pg_bodies.endDraw();
      
      if(APPLY_LIQUID_FX)
      {
        particle_fluidfx.param.base_LoD = 1;
        particle_fluidfx.param.base_blur_radius = 2;
        particle_fluidfx.param.base_threshold = 0.7f;
        particle_fluidfx.param.highlight_enabled = true;
        particle_fluidfx.param.highlight_LoD = 1;
        particle_fluidfx.param.highlight_decay = 0.6f;
        particle_fluidfx.param.sss_enabled = true;
        particle_fluidfx.param.sss_LoD = 3;
        particle_fluidfx.param.sss_decay = 0.7f;
        particle_fluidfx.apply(pg_liquid);
        
        particle_fluidfx.apply(pg_prigid);
        
        particle_fluidfx.param.base_LoD = 0;
        particle_fluidfx.param.base_blur_radius = 1;
        particle_fluidfx.param.base_threshold = 0.7f;
        particle_fluidfx.param.highlight_enabled = true;
        particle_fluidfx.param.highlight_LoD = 2;
        particle_fluidfx.param.highlight_decay = 0.9f;
        particle_fluidfx.param.sss_enabled = true;
        particle_fluidfx.param.sss_LoD = 1;
        particle_fluidfx.param.sss_decay = 0.7f;
        particle_fluidfx.apply(pg_bodies);
      }
      
      
//      DwFilter.get(pixelflow).gamma.apply(pg_liquid, pg_liquid, 1.4f);
//      DwFilter.get(pixelflow).gamma.apply(pg_bodies, pg_bodies, 1.4f);
      
//      background(BACKGROUND);
      blendMode(REPLACE);
      image(pg_checkerboard, 0, 0);
      blendMode(BLEND);
      image(pg_prigid, 0, 0);
      image(pg_liquid, 0, 0);
      image(pg_bodies, 0, 0);
      pushMatrix();
      world.applyTransform(this.g);
      world.drawBulletSpawnTrack(this.g);
      popMatrix();
    }
    
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
    if(key >= '1' && key <= '6') setParticleSpawnProperties(key - '1');
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
      body_def.position.x = -(width/4f)/scree_scale;
      body_def.position.y = b2d_screen_h - 3;
      body_def.bullet = true;
         
      Body circle_body = world.createBody(body_def);
      circle_body.createFixture(fixture_def);
      
      world.bodies.add(circle_body, true, color(255,220, 0), true, color(0), 1f);
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
      
      world.bodies.add(ground, true, color(0), !true, color(0), 1f);
    }

    createWall(5, 20, 50, 25, 260, 10);

    float screen_x = width/4;
    float screen_y = height/2;
    float size_x = width/2-50;
    float size_y = height/2;
    world.mouse_spawn_particles.setBoxShape(size_x, size_y);
    world.mouse_spawn_particles.spawn(screen_x, screen_y);
    
    
    
    
    // add some rigid particles.
    // those will be rendered with another render-group into another render-target.
    // This makes it possible to apply different post-processing effects on
    // different particle render-groups.
    int spawn_type_cpy = spawn_type;

    setParticleSpawnProperties(4);
    
    screen_x = width/4;
    screen_y = height/2;
    world.mouse_spawn_particles.setBoxShape(500, 20);
    world.mouse_spawn_particles.spawn(screen_x, screen_y);
    
    setParticleSpawnProperties(spawn_type_cpy);
    
  }
  


  public void createWall(int numx, int numy, float dimx, float dimy, float tx, float ty){
    
    float scree_scale = world.transform.screen_scale;
    
    dimx /= scree_scale;
    dimy /= scree_scale;
    
    tx /= scree_scale;
    ty /= scree_scale;
    
    PolygonShape brick_shape = new PolygonShape();
    brick_shape.setAsBox(dimx*0.5f, dimy*0.5f);
    
    PolygonShape brick_shape2 = new PolygonShape();
    brick_shape2.setAsBox(dimx*0.25f, dimy*0.5f);
    
    FixtureDef fixture_def = new FixtureDef();
    fixture_def.shape = brick_shape;
    fixture_def.density = 30;
    fixture_def.friction = 0.50f;
    fixture_def.restitution = 0.05f;
    
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
        ox = -numx * dimx * 0.5f;
        ox += odd_row ? dimx * off : 0;
        
        fixture_def.shape = brick_shape;
        if(!odd_row && x == 0){
          fixture_def.shape = brick_shape2;
          ox += dimx * 0.25;
        }
        else if(odd_row && x == (numx-1)){
          fixture_def.shape = brick_shape2;
          ox -= dimx * 0.25;
        }
        
        body_def.position.x = tx + ox + x * (dimx);
        body_def.position.y = ty + oy + y * (dimy);
   
        Body brick = world.createBody(body_def);
        brick.createFixture(fixture_def);
        
        float hsb_h = 20 + random(-3, 3);
//        float hsb_s = random(70,60);
//        float hsb_b = random(70,100);
        float hsb_s = 60 + random(-1,1) * 5;
        float hsb_b = 85 + random(-1,1) * 15;
        fcol = color(hsb_h, hsb_s, hsb_b);
        
        world.bodies.add(brick, true, fcol, true, scol, 0.5f);
      }
    }
    
    colorMode(RGB, 255);
   
  }
  
  
 
  

  
  int spawn_type = 0;
  
  public void setParticleSpawnProperties(int type){
    
    spawn_type = type;

    int COLOR_MIX = 0;
//    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    DwParticleSpawn particle_spawn = world.mouse_spawn_particles;
    particle_spawn.join_groups = false;
    particle_spawn.setCircleShape(30);
    
    colorMode(HSB, 360, 100, 100);
    
    float hsb_s = 100;
    float hsb_b = 100;

    switch (type) {
      case 0:
        particle_spawn.group_def.setColor(color(225, hsb_s, hsb_b));
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_spawn.group_def.setColor(color(100, hsb_s, hsb_b));
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 2:
        particle_spawn.group_def.setColor(color(0, hsb_s, hsb_b));
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 3:
        particle_spawn.group_def.setColor(color(60, hsb_s * 0.5f, hsb_b));
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 4:
        particle_spawn.group_def.setColor(color(330, hsb_s, hsb_b));
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        particle_spawn.join_groups = true;
        particle_spawn.setCircleShape(20);
        break;
    }
    
    colorMode(RGB, 255);

  }

  
  
  
   
  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_ParticleRenderGroups_LiquidFx.class.getName() });
  }
  
}