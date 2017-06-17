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

uniform mat4 mat_mvp;
uniform float point_size;

in vec2 pos; // position
in vec2 vel; // velocity
in vec4 col; // color
in vec2 con; // contact [weight, count]

out vec4 var_col;

void main() {
  gl_Position = mat_mvp * vec4(pos, 0, 1);
  //gl_PointSize = point_size;
  
  var_col = col;
  
  // float vel_mag = clamp(length(vel) * 0.8, 0.5, 1.5);
  // var_col.rgb *= vel_mag;
  
  
  // float con_mult = clamp(con.x * 0.8, 0.5, 1.5);
  // var_col.rgb *= con_mult;
  
  
  float vel_mag = length(vel) / 100.0f;
  float con_mult = con.x / 50.0f;
  
  float sum = 1.0 + (con_mult + vel_mag);
  sum *= sum;
  sum *= sum;
  sum *= sum;
  //sum = pow(sum, 10);


  var_col.rgb *= sum;
  
 // if(sum > 1.2){
   // var_col.rgb = vec3(1.0);
 // }
  
}