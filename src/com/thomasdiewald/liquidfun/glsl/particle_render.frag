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



#version 150

out vec4 fragColor;
in vec4 var_col;

uniform sampler2D tex_sprite;
uniform float falloff_exp1 = 1.0;
uniform float falloff_exp2 = 1.0;
uniform float falloff_mult = 1.0;
uniform float color_mult   = 1.0;

uniform float use_sprite = 0.0;

void main() {

  fragColor = var_col * color_mult;
  
  if(use_sprite == 1.0){
    fragColor *= texture(tex_sprite, gl_PointCoord);
  } else {
    vec2 pcoord_norm = abs(gl_PointCoord * 2.0 - 1.0); // abs[-1, 1]

    float falloff = clamp(length(pcoord_norm), 0.0, 1.0);
    falloff = pow(falloff, falloff_exp1);
    falloff = 1.0 - falloff;
    falloff = pow(falloff, falloff_exp2);

    fragColor.a *= falloff * falloff_mult;
  }
   
}