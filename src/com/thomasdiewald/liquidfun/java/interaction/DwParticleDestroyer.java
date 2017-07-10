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

import org.jbox2d.callbacks.ParticleDestructionListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;

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
public class DwParticleDestroyer implements DwInteractionEvent, ParticleDestructionListener{
  
  public World world;
  public DwViewportTransform transform;
  
  public boolean enable_callback = false;
  
  public Transform shape_transform = new Transform();
  public Shape shape;
  
  public DwParticleDestroyer(World world, DwViewportTransform vptransform){
    this.world = world;
    this.transform = vptransform;
    this.shape_transform.setIdentity();
    
    setCircleShape(30);
//    setBoxShape(60, 60);
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
    System.out.println(bw);
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
       this.shape = shape;
     }
   }
  

  
//   protected void applyShapeTransform(){
//     if(shape.getType() == ShapeType.CIRCLE){
//       CircleShape shp = (CircleShape) shape;
//       Transform.mulToOut(shape_transform, shp.m_p, shp.m_p);
//     } 
//     else if(shape.getType() == ShapeType.POLYGON){
//       PolygonShape shp = (PolygonShape) shape;
//       for(int i = 0; i < shp.m_count; i++){
//         Transform.mulToOut(shape_transform, shp.m_vertices[i], shp.m_vertices[i]);
//       }
//     } 
//   }
//   
//   protected void revertShapeTransform(){
//     if(shape.getType() == ShapeType.CIRCLE){
//       CircleShape shp = (CircleShape) shape;
//       Transform.mulTransToOut(shape_transform, shp.m_p, shp.m_p);
//     }
//     else if(shape.getType() == ShapeType.POLYGON){
//       PolygonShape shp = (PolygonShape) shape;
//       for(int i = 0; i < shp.m_count; i++){
//         Transform.mulTransToOut(shape_transform, shp.m_vertices[i], shp.m_vertices[i]);
//       }
//     } 
//   }
  
  
   public void begin(float screen_x, float screen_y){
     transform.getScreen2box(screen_x, screen_y, shape_transform.p);
//     applyShapeTransform();
     destroyParticles(shape);
//     revertShapeTransform();
     shape_transform.setIdentity();
     is_active = true;
   }
   
   public void update(float screen_x, float screen_y){
     if(is_active){
       transform.getScreen2box(screen_x, screen_y, shape_transform.p);
//       applyShapeTransform();
       destroyParticles(shape);
//       revertShapeTransform();
       shape_transform.setIdentity();
     }
   }
   
   public void end(float screen_x, float screen_y){
     is_active = false;
   }
  

  public void destroyParticles(Shape shape){
    // push PDL
    ParticleDestructionListener pdl = world.getParticleDestructionListener();
    if(enable_callback){
      world.setParticleDestructionListener(this);
    }
    // destroy particles
    world.destroyParticlesInShape(shape, shape_transform, enable_callback);
    // pop PDL
    world.setParticleDestructionListener(pdl);
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
    return button;
  }

  
  public int button = PConstants.RIGHT;
  public boolean is_active = false;

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
          begin(mouseX, mouseY);
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
    update(mouseX, mouseY);
  }

  
  @Override
  public void setMouseOffset(int mouseX_off, int mouseY_off){
    this.mouseX_off = mouseX_off;
    this.mouseY_off = mouseY_off;
  }
  
  

  
  

  
  @Override
  public void sayGoodbye(ParticleGroup group) {
  }

  @Override
  public void sayGoodbye(int index) {
  }
  
  
  
}