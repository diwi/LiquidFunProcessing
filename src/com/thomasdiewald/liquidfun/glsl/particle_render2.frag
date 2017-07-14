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
in vec4 tint;

uniform sampler2D tex_sprite;

void main() {
  fragColor = texture(tex_sprite, gl_PointCoord) * tint;
}