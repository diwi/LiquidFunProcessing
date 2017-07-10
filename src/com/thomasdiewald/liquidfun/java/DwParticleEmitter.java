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
import org.jbox2d.particle.ParticleDef;


/**
 * 
 * Custom Particle Emitter.<br>
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleEmitter {
  
    public World world;
    public DwViewportTransform transform;
    public ParticleDef pdef = new ParticleDef();
    
    public float emit_dir = (float) (Math.PI * 0.5f);
    public float emit_vel = 20;
    public Vec2  emit_pos = new Vec2();
    
    public float emit_dir_jitter = 0.2f; // [0, PI*2]
    public float emit_vel_jitter = 0.5f; // noise mult
    public float emit_pos_jitter = 0.4f; // spawn radius, world-space


    protected int counter = 0;
    
    public DwParticleEmitter(World world, DwViewportTransform transform){
      this.world = world;
      this.transform = transform;
    }


    /**
     * inline Emitter-definition. <br>
     * Velocity and Position must be defined in screen-space dimensions.
     */
    public void setInScreen(float pos_x, float pos_y, float vel, float dir_deg, int col_argb, int flags){
      emit_pos = transform.getScreen2box(pos_x, pos_y, emit_pos);
      emit_vel = vel / transform.screen_scale;
      emit_dir = dir_deg * DwUtils.TO_RAD;
      pdef.color.set(col_argb);
      pdef.flags = flags;
    }
    
    /**
     * inline Emitter-definition. <br>
     * Velocity and Position must be defined in world-space dimensions.
     */
    public void setInWorld(float pos_x, float pos_y, float vel, float dir_deg, int col_argb, int flags){
      emit_pos.set(pos_x, pos_y);
      emit_vel = vel;
      emit_dir = dir_deg * DwUtils.TO_RAD;
      pdef.color.set(col_argb);
      pdef.flags = flags;
    }
    
    
    
    
    
    public void emitParticles(int count){
      for(int i = 0; i < count; i++){
        emitParticle();
      }
    }

    public void emitParticle(){

      // velocity (noise)
      float val1 = (float) (Math.sin(counter / 10f)) * 0.2f;
      float val2 = (float) (Math.cos(counter / 100f));
      float srandnoise = val1 * val2;
      
      float rot_angle = emit_dir     + srandnoise * emit_dir_jitter;
      float vel_mag   = emit_vel  + srandnoise * emit_vel_jitter;
      
      float vel_x = (float) (Math.cos(rot_angle) * vel_mag);
      float vel_y = (float) (Math.sin(rot_angle) * vel_mag);
      
      // position
      float[] jitter = DwUtils.sampleDisk_Halton(counter, 0.5f);

      float pos_x = emit_pos.x + jitter[0] * emit_pos_jitter;
      float pos_y = emit_pos.y + jitter[1] * emit_pos_jitter;

      // create Particle
      pdef.position.set(pos_x, pos_y);
      pdef.velocity.set(vel_x, vel_y);
      world.createParticle(pdef);
      
      counter++;
    }
    

  }