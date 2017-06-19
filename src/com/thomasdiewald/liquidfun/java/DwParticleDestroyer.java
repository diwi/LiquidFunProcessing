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

import org.jbox2d.callbacks.ParticleDestructionListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;


/**
 * 
 * Class that handles mouse-interaction with Box2D/LiquidFun Particles.
 * 
 * Picking, Dragging, Destroying ...
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleDestroyer implements ParticleDestructionListener{
  
  public World world;
  public DwViewportTransform vptransform;
  
  private final Transform xf = new Transform();
  
  public CircleShape shape = new CircleShape();

  public DwParticleDestroyer(World world, DwViewportTransform vptransform){
    this.world = world;
    this.vptransform = vptransform;
    xf.setIdentity();
  }


  public void destroyParticles(float sceen_x, float screen_y, float screen_rad){
    destroyParticles(sceen_x, screen_y, screen_rad, false);
  }
  
  public void destroyParticles(float sceen_x, float screen_y, float screen_rad, boolean callback){
    vptransform.getScreen2box(sceen_x, screen_y, shape.m_p);
    shape.m_radius = screen_rad / vptransform.screen_scale;
    destroyParticles(shape);
  }
  
  public void destroyParticles(Shape shape){
    destroyParticles(shape, false);
  }
  
  public void destroyParticles(Shape shape, boolean callback){
    // push PDL
    ParticleDestructionListener pdl = world.getParticleDestructionListener();
    if(callback){
      world.setParticleDestructionListener(this);
    }
    // destroy particles
    world.destroyParticlesInShape(shape, xf, callback);
    // pop PDL
    world.setParticleDestructionListener(pdl);
  }
 
  
  @Override
  public void sayGoodbye(ParticleGroup group) {
  }

  @Override
  public void sayGoodbye(int index) {
  }

}