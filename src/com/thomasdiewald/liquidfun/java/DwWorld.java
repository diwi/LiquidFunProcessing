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
import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.particle.ParticleType;

import com.thomasdiewald.liquidfun.java.interaction.DwMouseShootBullet;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleDestroyer;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.liquidfun.java.interaction.DwInteractionEvent;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragBodies;
import com.thomasdiewald.liquidfun.java.interaction.DwMouseDragParticles;
import com.thomasdiewald.liquidfun.java.render.DwBody;
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;
import com.thomasdiewald.liquidfun.java.render.DwDebugDraw;
import com.thomasdiewald.liquidfun.java.render.DwFixture;
import com.thomasdiewald.liquidfun.java.render.DwJoint;
import com.thomasdiewald.liquidfun.java.render.DwParticleRender;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderP5;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.opengl.PGraphics2D;



/**
 * 
 * Main box2d world.<br>
 * 
 * <br>
 * https://github.com/jbox2d/jbox2d/blob/master/jbox2d-library/src/main/java/org/jbox2d/dynamics/World.java
 * <br>
 * https://google.github.io/liquidfun/Programmers-Guide/html/md__chapter11__particles.html
 * <br>
 * 
 * @author Thomas Diewald
 *
 */
public class DwWorld extends World{
  
  public PApplet papplet;
  public DwViewportTransform transform;
  
  /**
   * if true, particles is an instance of {@link DwParticleRenderGLQuads}
   * otheriwse {@link DwParticleRenderP5} is instantiated.
   */
  static public boolean INIT_GL_PARTICLES = true;
  
  
  public DwDebugDraw debug_draw;
  
  public DwBodyGroup bodies;
  public DwParticleRender particles;
  
  public final DwMouseDragBodies    mouse_drag_bodies;   
  public final DwMouseDragParticles mouse_drag_particles;
  public final DwMouseShootBullet   mouse_shoot_bullet;
  public final DwParticleSpawn      mouse_spawn_particles;
  public final DwParticleDestroyer  mouse_destroy_particles;
  
  private String[] registered_methods = 
    {
      "dispose"
     ,"mouseEvent"
     ,"keyEvent" 
    };
  
  
  public DwWorld(PApplet papplet){
    this(papplet, 20);
  }
  

  
  public DwWorld(PApplet papplet, float scale){
    super(new Vec2(0, -10f));
    
    this.papplet = papplet;
    
    // particle settings
    super.setParticleGravityScale(0.4f);
    super.setParticleDensity(1.2f);
    super.setParticleDamping(1.0f);
    super.setParticleRadius(0.25f);
    
    
    int w = papplet.width;
    int h = papplet.height;
    transform = new DwViewportTransform(papplet);
    transform.setScreen(w, h, scale, w/2, h);
    
    
    createZombieBounds();
  
    debug_draw = new DwDebugDraw(papplet, this, transform);
    
      
    bodies = new DwBodyGroup(papplet, this, transform);
    
    if(INIT_GL_PARTICLES){
      particles = new DwParticleRenderGL(papplet, this, transform);
    } else {
      particles = new DwParticleRenderP5(papplet, this, transform);
    }
    
    for(int i = 0; i < registered_methods.length; i++){
      papplet.registerMethod(registered_methods[i], this);
    }

    
    mouse_drag_bodies       = new DwMouseDragBodies   (this, transform);
    mouse_drag_particles    = new DwMouseDragParticles(this, transform);
    mouse_shoot_bullet      = new DwMouseShootBullet  (this, transform);
    mouse_spawn_particles   = new DwParticleSpawn     (this, transform);
    mouse_destroy_particles = new DwParticleDestroyer (this, transform);
    
    addMouseAction(mouse_drag_bodies      );
    addMouseAction(mouse_drag_particles   );
    addMouseAction(mouse_shoot_bullet     );
    addMouseAction(mouse_spawn_particles  );
    addMouseAction(mouse_destroy_particles);
  }
  

  

  /**
   * called by processing
   */
  public void dispose(){
    for(int i = 0; i < registered_methods.length; i++){
      papplet.unregisterMethod(registered_methods[i], this);
    }
    
    if(bodies != null) bodies.release(); bodies = null;
    if(particles != null) particles.release(); particles = null;
  }
  
  
  /**
   * must be called by the user, in case this world instance wont be used any longer.
   */
  public void release(){
    dispose();
  }
  

  
  //////////////////////////////////////////////////////////////////////////////
  //
  // World Update Step
  //
  //////////////////////////////////////////////////////////////////////////////
  
 
  public void update(){
    update(1/60f, 8, 4);
  }
  
  /**
   * Take a time step. This performs collision detection, integration, and constraint solution.
   * 
   * @param timeStep the amount of time to simulate, this should not vary.
   * @param velocityIterations for the velocity constraint solver.
   * @param positionIterations for the position constraint solver.
   */
  public void update(float timestep, int iter_velocity, int iter_position){
    
//    bodies.addBullet(true, 0xFF000000, false, 0xFF000000, 1f);
    bodies.addBullet(true, 0xFF806040, false, 0xFF000000, 1f);
//    bodies.addBullet(true, papplet.color(128), true, papplet.color(0), 1f);
    
    mouseUpdateAction();
    
    if(zombie_aabb_enabled){
      removeLostParticles();
      removeLostBodies();
    }
    
    super.step(timestep, iter_velocity, iter_position);
    updateBodies();
    updateJoints();
    particles.update();
  }


  public void updateBodies(){
    for (Body body = super.getBodyList(); body != null; body = body.getNext()) {
      Transform xf = body.getTransform();
      
      DwBody dwbody = getShape(body);
      if(dwbody != null){
        dwbody.shape.resetMatrix();
        dwbody.shape.rotate(xf.q.getAngle());
        dwbody.shape.translate(xf.p.x, xf.p.y);
      }
      
//      for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
//        PShape shp_fixture = DwBodyRenderP5.getShape(fixture);
//        if(shp_fixture != null){
//          shp_fixture.resetMatrix();
//          shp_fixture.rotate(xf.q.getAngle());
//          shp_fixture.translate(xf.p.x, xf.p.y);
//        }
//      }
    }
  }
  
  
  public void updateJoints(){
    Vec2 ancA = new Vec2();
    Vec2 ancB = new Vec2();
 
    for (Joint joint = super.getJointList(); joint != null; joint = joint.getNext()) {
      
      DwJoint dwjoint = getShape(joint);
      if(dwjoint != null){
        PShape shape = dwjoint.shape;
        
        JointType type = joint.getType();
        
        joint.getAnchorA(ancA);
        joint.getAnchorB(ancB);
        
//        Body bodyA = joint.getBodyA();
//        Body bodyB = joint.getBodyB();
//        Transform xfA = bodyA.getTransform();
//        Transform xfB = bodyB.getTransform();
//        Vec2 posA = xfA.p;
//        Vec2 posB = xfB.p;
        
        if(type == JointType.PULLEY){
          PulleyJoint pulley = (PulleyJoint) joint;
          Vec2 gancA = pulley.getGroundAnchorA();
          Vec2 gancB = pulley.getGroundAnchorB();

          updateLineShape(shape.getChild(0), ancA, gancA);
          updateLineShape(shape.getChild(1), ancB, gancB);
          updateLineShape(shape.getChild(2), gancA, gancB);
        } else {
          updateLineShape(shape, ancA, ancB);
        }
      }
      
    }

  }
  
  
  protected void updateLineShape(PShape shp, Vec2 p0, Vec2 p1){
    Vec2 AB = p1.sub(p0);
    
    // https://github.com/processing/processing/blob/master/core/src/processing/opengl/PShapeOpenGL.java#L1348
    float ab_len = AB.length() + 0.0000001f; 
    
    shp.resetMatrix();
    shp.scale(ab_len, 1);
    shp.rotate((float) Math.atan2(AB.y, AB.x));
    shp.translate(p0.x, p0.y);
  }
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Bounds, for removing lost particles/bodies (b2_zombieParticle);
  //
  //////////////////////////////////////////////////////////////////////////////
  
  public boolean zombie_aabb_enabled = true;
  public AABB zombie_aabb;
  
  

  
  public void enableZombieBounds(boolean enable){
    zombie_aabb_enabled = enable;
  }
  
  /**
   * custom AABB, which is used for removing bodies/particles outside that AABB.
   */
  public void createZombieBounds(AABB aabb_world){
    zombie_aabb.set(aabb_world);
  }

  /**
   * creates an screen-sized AABB + 50 border, which is used for removing 
   * bodies/particles outside that AABB.
   */
  public void createZombieBounds(){
    zombie_aabb = new AABB();

    float screenw = transform.screen_dimx;
    float screenh = transform.screen_dimy;
    float off = (screenh + screenw) * 0.5f; // border around the screen
    
    transform.getScreen2box(      0-off, screenh+off, zombie_aabb.lowerBound);
    transform.getScreen2box(screenw+off,       0-off, zombie_aabb.upperBound);
  }
  
  /**
   * removes particles outside the zombie_aabb from the world.
   */
  public void removeLostParticles(){
    
    if(zombie_aabb == null){
      createZombieBounds();
    }
    
    int    pcount = getParticleCount();
    Vec2[] pverts = getParticlePositionBuffer();
    int[]  pflags = getParticleFlagsBuffer();
    
    for(int i = 0; i < pcount; i++){
      Vec2 vert = pverts[i];
      if(vert.x < zombie_aabb.lowerBound.x || vert.x > zombie_aabb.upperBound.x ||
         vert.y < zombie_aabb.lowerBound.y || vert.y > zombie_aabb.upperBound.y)
      {
        pflags[i] |= ParticleType.b2_zombieParticle;
        // world.destroyParticle(i); // works also
      }
    }
  }
  
  
  /**
   * removes bodies outside the zombie_aabb from the world.
   */
  public void removeLostBodies(){
    for (Body body = getBodyList(); body != null; body = body.getNext()) {
      Vec2 vert = body.getTransform().p;
      if(vert.x < zombie_aabb.lowerBound.x || vert.x > zombie_aabb.upperBound.x ||
         vert.y < zombie_aabb.lowerBound.y || vert.y > zombie_aabb.upperBound.y)
      {
        destroyBody(body);
      }
    }
  }
  
  
  
  
  
  
  
  
  

  //////////////////////////////////////////////////////////////////////////////
  //
  // DebugDraw
  //
  //////////////////////////////////////////////////////////////////////////////
  
  public void display(PGraphics2D canvas){
    bodies.display(canvas);
    particles.display(canvas);
  }
  
  
  public void displayDebugDraw(PGraphics canvas){
    debug_draw.display(canvas);
  }
  
  
  public void applyTransform(PGraphics canvas){
    canvas.applyMatrix(transform.mat_box2screen);
  }
  
  

  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Bullet
  //
  //////////////////////////////////////////////////////////////////////////////
  public void drawBulletSpawnTrack(PGraphics canvas){
    mouse_shoot_bullet.drawSpawnTrack(canvas);
  }
  

  

  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Interactions
  //
  //////////////////////////////////////////////////////////////////////////////
  
  
  protected ArrayList<DwInteractionEvent> mouse_actions = new ArrayList<>();

  public void mouseEvent(MouseEvent event){
    for(DwInteractionEvent maction : mouse_actions){
      maction.mouseEvent(event);
    }
  }
  
  public void keyEvent(KeyEvent event){
    for(DwInteractionEvent maction : mouse_actions){
      maction.keyEvent(event);
    }
  }
  
  protected void mouseUpdateAction(){
    for(DwInteractionEvent maction : mouse_actions){
      maction.updateEvent();
    }
  }
  
  
  
  
  public void addMouseAction(DwInteractionEvent mouse_action){
    if(!mouse_actions.contains(mouse_action)){
      mouse_actions.add(mouse_action);
    }
  }
  
  public void removeMouseAction(DwInteractionEvent mouse_action){
    mouse_actions.remove(mouse_action);
  }
  
  public void removeMouseAction(Class<?> obj_class){
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        mouse_actions.remove(i);
      }
    }
  }
  
  public void removeAllMouseActions(){
    mouse_actions.clear();
  }
  
  public boolean hasMouseAction(DwInteractionEvent mouse_action){
    return mouse_actions.contains(mouse_action);
  }
  
  public boolean hasMouseAction(Class<?> obj_class){
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        return true;
      }
    }
    return false;
  }
  

  public List<DwInteractionEvent> getMouseAction(Class<?> obj_class){
    List<DwInteractionEvent> list = new ArrayList<>();
    
    for(int i = mouse_actions.size() - 1; i >= 0; i--){
      DwInteractionEvent maction = mouse_actions.get(i);
      if(maction.getClass().equals(obj_class)){
        list.add(maction);
      }
    }
    return list;
  }

  public List<DwInteractionEvent> getMouseActions(){
    return mouse_actions;
  }
  
  
  
  
  
  


  
  
  
  
  /**
   * replace the default particle renderer.
   * @param particle_render
   */
  public void setParticleRender(DwParticleRender particle_render){
    if(particle_render == null){
      return;
    }
    if(particles != null){
      particles.release();
    }
    particles = particle_render;
  }
  
  /**
   * replace the default body renderer.
   * @param particle_render
   */
  public void setBodyRender(DwBodyGroup body_render){
    if(body_render == null){
      return;
    }
    if(bodies != null){
      bodies.release();
    }
    bodies = body_render;
  }
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //
  // Body Shape Utilities
  //
  //////////////////////////////////////////////////////////////////////////////
  
  public void destroyBody(Body body){
    release(body);
    super.destroyBody(body);
  }
  
  public void destroyJoint(Joint joint){
    release(joint);
    super.destroyJoint(joint);
  }
  
  public void destroyFixture(Fixture fixture){
    release(fixture);
    Body body = fixture.getBody();
    body.destroyFixture(fixture);
   
  }
  
  

  static public void release(Body body){
    DwBody shp = getShape(body);
    if(shp != null){
      shp.release();
    }
  }
  
  static public void release(Joint joint){
    DwJoint shp = getShape(joint);
    if(shp != null){
      shp.release();
    }
  }
  
  static public void release(Fixture fixture){
    DwFixture shp = getShape(fixture);
    if(shp != null){
      shp.release();
    }
  }

  
  
  static public boolean hasShape(Joint joint){
    return (joint.m_userData != null) && (joint.m_userData instanceof DwJoint);
  }
  
  static public DwJoint getShape(Joint joint){
    if(hasShape(joint)){
      return (DwJoint) joint.m_userData;
    }
    return null;
  }

  static public boolean hasShape(Fixture fixture){
    return (fixture.m_userData != null) && (fixture.m_userData instanceof DwFixture);
  }
  
  static public DwFixture getShape(Fixture fixture){
    if(hasShape(fixture)){
      return (DwFixture) fixture.m_userData;
    }
    return null;
  }
  
  static public boolean hasShape(Body body){
    return (body.m_userData != null) && (body.m_userData instanceof DwBody);
  }
  
  static public DwBody getShape(Body body){
    if(hasShape(body)){
      return (DwBody) body.m_userData;
    }
    return null;
  }
  
  
  

  public PShape setStyle(Body body
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwBody dwbody = getShape(body);
    dwbody.style.set(fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
    return setStyle(dwbody.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(Fixture fixture
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwFixture dwfixture = getShape(fixture);
    return setStyle(dwfixture.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(Joint joint
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    DwJoint dwjoint = getShape(joint);
    return setStyle(dwjoint.shape, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
  }
  
  public PShape setStyle(PShape shp
      , boolean fill_enabled
      , int     fill_color
      , boolean stroke_enabled
      , int     stroke_color
      , float   stroke_weight
  ){
    if(shp == null) return null;
    shp.setFill        (fill_enabled);
    shp.setFill        (fill_color);
    shp.setStroke      (stroke_enabled);
    shp.setStroke      (stroke_color);
    shp.setStrokeWeight(stroke_weight / transform.screen_scale);
    return shp;
  }

  
  
  
  
//  public Body createBody(BodyDef def) {
//    Body body = super.createBody(def);
//    
////    body.createFixture(def)
//    bodies.add(body); 
//    return body;
//  }
//  
//  public Body createBody(BodyDef def, ShapeStyle style) {
//    Body body = super.createBody(def);
//    bodies.add(body, style);
//    return body;
//  }
//  
//  public Body createBody(BodyDef def, boolean fill_enabled, int fill_color, boolean stroke_enabled, int stroke_color, float stroke_weight) {
//    Body body = super.createBody(def);
//    bodies.add(body, fill_enabled, fill_color, stroke_enabled, stroke_color, stroke_weight);
//    return body;
//  }
//  
//  
//  
//  
//  // exact copy off Body.createFixture();
//  public final Fixture createFixture(Body body, FixtureDef def) {
//    assert (this.isLocked() == false);
//
//    if (this.isLocked() == true) {
//      return null;
//    }
//
//    Fixture fixture = new Fixture();
//    fixture.create(body, def);
//
//    if ((m_flags & Body.e_activeFlag) == Body.e_activeFlag) {
//      BroadPhase broadPhase = this.m_contactManager.m_broadPhase;
//      fixture.createProxies(broadPhase, body.m_xf);
//    }
//
//    fixture.m_next = body.m_fixtureList;
//    body.m_fixtureList = fixture;
//    ++body.m_fixtureCount;
//
//    fixture.m_body = body;
//
//    // Adjust mass properties if needed.
//    if (fixture.m_density > 0.0f) {
//      body.resetMassData();
//    }
//
//    // Let the world know we have a new fixture. This will cause new contacts
//    // to be created at the beginning of the next time step.
//    this.m_flags |= World.NEW_FIXTURE;
//
//    return fixture;
//  }
//  
//  public final Fixture createFixture(Body body, Shape shape, float density) {
//    FixtureDef fixDef = new FixtureDef();
//    fixDef.shape = shape;
//    fixDef.density = density;
//    return createFixture(body, fixDef);
//  }
  
  
}
