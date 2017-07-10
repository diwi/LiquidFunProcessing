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


package com.thomasdiewald.liquidfun.java.interaction;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import com.thomasdiewald.liquidfun.java.DwViewportTransform;

import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


/**
 * 
 * Class that handles mouse-interaction with Box2D/LiquidFun Particles.<br>
 * 
 * 
 * @author Thomas Diewald
 *
 */
public class DwParticleSpawn implements DwInteractionEvent{
  
  public World world;
  public DwViewportTransform transform;
  
  public boolean is_active = false;
  public int button = PConstants.CENTER;
  
  public DwParticleDestroyer pdestroyer;
  public ParticleGroupDef group_def;
  
  public ParticleGroup group_new;
  public ParticleGroup group_old;

  public Transform shape_transform = new Transform();
  
  public int spawn_count = 0; // number of new created particles
  public int spawn_begin = 0; // idx of first new particle
  public int spawn_end   = 0; // spawn_begin + spawn_count
  
  public boolean join_groups = true;
  public boolean destroy_inplace_before_spawn = true;

  
  public DwParticleSpawn(World world, DwViewportTransform transform){
    this.world = world;
    this.transform = transform;
    this.pdestroyer = new DwParticleDestroyer(world, transform);
    this.group_def  = new ParticleGroupDef();
    
    this.group_def.setColor(new Color3f(1,0.35f, 0.15f));
    this.group_def.flags = ParticleType.b2_waterParticle | ParticleType.b2_viscousParticle;
    this.group_def.groupFlags = 0;
    
    setCircleShape(30);
  }
  
  
  /**
   * Circle as a brush
   */
  public void setCircleShape(float screen_radius){
    CircleShape shape = new CircleShape();
    shape.m_radius = screen_radius / transform.screen_scale;
    shape.m_p.setZero();
    setShape(shape);
  }
  
  /**
   * Rectangle as a brush
   */
  public void setBoxShape(float screen_box_w, float screen_box_h){
    float bw = screen_box_w / transform.screen_scale;
    float bh = screen_box_h / transform.screen_scale;
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(bw * 0.5f, bh * 0.5f);
    setShape(shape);
  }
   
  /**
   * Any Shape given as a brush. Use (0,0) as center.
   * only ShapeType.CIRCLE and ShapeType.POLYGON can be used
   * 
   */
  public void setShape(Shape shape){
    boolean is_circle  = ShapeType.CIRCLE  == shape.getType();
    boolean is_polygon = ShapeType.POLYGON == shape.getType();
    
    if(is_circle || is_polygon){
      group_def.shape = shape;
    }
  }
  
 

  protected void applyShapeTransform(){
    if(group_def.shape.getType() == ShapeType.CIRCLE){
      CircleShape shp = (CircleShape) group_def.shape;
      Transform.mulToOut(shape_transform, shp.m_p, shp.m_p);
    } 
    else if(group_def.shape.getType() == ShapeType.POLYGON){
      PolygonShape shp = (PolygonShape) group_def.shape;
      
      for(int i = 0; i < shp.m_count; i++){
        Transform.mulToOut(shape_transform, shp.m_vertices[i], shp.m_vertices[i]);
      }
    } 
  }
  
  protected void revertShapeTransform(){
    if(group_def.shape.getType() == ShapeType.CIRCLE){
      CircleShape shp = (CircleShape) group_def.shape;
      Transform.mulTransToOut(shape_transform, shp.m_p, shp.m_p);
    }
    else if(group_def.shape.getType() == ShapeType.POLYGON){
      PolygonShape shp = (PolygonShape) group_def.shape;
      for(int i = 0; i < shp.m_count; i++){
        Transform.mulTransToOut(shape_transform, shp.m_vertices[i], shp.m_vertices[i]);
      }
    } 
  }
  
 
  public ParticleGroup spawn(float screen_x, float screen_y){
    transform.getScreen2box(screen_x, screen_y, shape_transform.p);
    
    applyShapeTransform();
    
    // destroy particles in group_def.shape
    if(destroy_inplace_before_spawn){
      pdestroyer.destroyParticles(group_def.shape);
    }

    // spawn particles in group_def.shape
    group_old = group_new;
    group_new = world.createParticleGroup(group_def);
    
    revertShapeTransform();
    
    // result stats
    spawn_count = group_new.getParticleCount();
    spawn_begin = group_new.getBufferIndex();
    spawn_end   = spawn_begin + spawn_count;

    // join groups if its a good idea
    if(join_groups){
      if (group_old != null && group_new.getGroupFlags() == group_old.getGroupFlags()) {
        world.joinParticleGroups(group_old, group_new);
        group_new = group_old;
      }
    }
    is_active = true;
    return group_new;
  }
  

  public void end(float screen_x, float screen_y){
    group_new = null;
    group_old = null;
    is_active = false;
  }

  
  public boolean is_enabled = true;
  
  @Override
  public void enable(boolean enable) {
    is_enabled = enable;
  }

  @Override
  public boolean isEnabled() {
    return is_enabled;
  }
  
  @Override
  public boolean isActive() {
    return is_active;
  }

  @Override
  public void setMouseButton(int button) {
    this.button = button;
  }

  @Override
  public int getMouseButton() {
    // TODO Auto-generated method stub
    return button;
  }


  int mouseX;
  int mouseY;
  int mouseX_off = 0;
  int mouseY_off = 0;
  
  @Override
  public void mouseEvent(MouseEvent event){
    if(!is_enabled){
      return;
    }
    mouseX = mouseX_off + event.getX();
    mouseY = mouseY_off + event.getY();
    if(event.getButton() == this.button){
      switch(event.getAction()){
      case MouseEvent.PRESS:
      case MouseEvent.DRAG:  
        if(key_combi_enabled == key_combi_active){
          spawn(mouseX, mouseY);
        }
        break;
      case MouseEvent.RELEASE: 
        end(mouseX, mouseY); 
        break;
      }
    }
  }

  public int     key_combi = 0;
  public boolean key_combi_active = false;
  public boolean key_combi_enabled = false;

  @Override
  public void keyEvent(KeyEvent event){
    switch(event.getAction()){
      case KeyEvent.PRESS: 
        key_combi_active = ((key_combi ^ event.getModifiers()) == 0);
        break;
      case KeyEvent.RELEASE:
        key_combi_active = false;
        break;
    }
   
  }
  

  @Override
  public void updateEvent() {
  }

  @Override
  public void setMouseOffset(int mouseX_off, int mouseY_off){
    this.mouseX_off = mouseX_off;
    this.mouseY_off = mouseY_off;
  }
  
  
  


}