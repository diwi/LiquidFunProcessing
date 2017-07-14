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

in vec2 pos;
in vec4 col;

out vec4 tint;

void main() {
  gl_Position = mat_mvp * vec4(pos, 0, 1);
  tint = col;
}