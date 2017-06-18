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

import java.util.ArrayList;
import java.util.Collections;

import org.jbox2d.callbacks.ParticleDestructionListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;

import processing.core.PShape;


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
  
  private final Transform xf = new Transform();
  
  public ArrayList<Integer> indices = new ArrayList<Integer>();
  public int num_destroyed = 0;
  
  public CircleShape shape = new CircleShape();


  public DwParticleDestroyer(){
    reset();
  }

  public void reset(){
    xf.setIdentity();
    indices.clear();
    num_destroyed = 0;
  }
  
  public void destroyParticlesInShape(World world, Shape shape, boolean callback){
    world.setParticleDestructionListener(this);
    num_destroyed += world.destroyParticlesInShape(shape, xf, callback);
  }

  public void destroyParticlesInShape(World world, Shape shape){
    destroyParticlesInShape(world, shape, true);
  }
  
  public void destroyParticlesAtLocation(World world, Vec2 pos, float radius, boolean callback){
    shape.m_p.set(pos);
    shape.m_radius = radius;
    world.setParticleDestructionListener(this);
    num_destroyed += world.destroyParticlesInShape(shape, xf, callback);
  }
  
  public void destroyParticlesAtLocation(World world, Vec2 pos, float radius){
    destroyParticlesAtLocation(world, pos, radius, true);
  }

  
  public void destroyPShapes(PShape shp_group){
    if(shp_group == null){
      return;
    }
    int num_indices = indices.size();
    
    if(num_indices > 0){
    
      // make sure the list is sorted (just in case)
      Collections.sort(indices);
      
      if(shp_group.getChildCount() >= indices.get(num_indices-1)){
  
        // iterate from back to front and remove shapes
        for(int i = num_indices-1; i >= 0; i--){
          shp_group.removeChild(indices.get(i));
        }
        //      if(num_destroyed != num_indices){
        //        System.out.println("destroyed: "+num_destroyed+", "+num_indices);
        //      }
      }
    }
    reset();
  }


  @Override
  public void sayGoodbye(ParticleGroup group) {
    //System.out.println("ParticleDestroyer.sayGoodbye(ParticleGroup group) is not implemented!");
  }

  @Override
  public void sayGoodbye(int index) {
    indices.add(index);
  }

}