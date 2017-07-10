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

import org.jbox2d.particle.ParticleGroup;

/**
 * 
 * @author Thomas Diewald
 * 
 */
public interface DwParticleRenderGroupCallback {
  public int getRenderGroupIndex(int particle_idx, ParticleGroup group, int particle_flag);
}