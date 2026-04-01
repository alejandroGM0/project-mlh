package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.visual.SpriteComponent;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.resources.Assets;
import io.github.proyectoM.templates.BulletTemplate;

/** Creates bullet entities and their optional physics bodies. */
public class BulletFactory {
  private static final float BODY_DENSITY = 1f;
  private static final float BODY_RESTITUTION = 0f;
  private static final float BODY_FRICTION = 0f;
  private static final float DEFAULT_SPRITE_ANGLE_DEGREES = 0f;
  private static final float BULLET_RADIUS_DIVISOR = 2f;
  private static final float INITIAL_DISTANCE_TRAVELLED = 0f;

  private static BulletFactory instance;

  private Engine engine;
  private final ObjectMap<String, TextureRegion> textureCache = new ObjectMap<>();
  private final BodyDef reusableBodyDef = new BodyDef();
  private final FixtureDef reusableFixtureDef = new FixtureDef();
  private CircleShape reusableShape;

  private BulletFactory(Engine engine) {
    this.engine = engine;
    this.reusableShape = new CircleShape();
    reusableFixtureDef.density = BODY_DENSITY;
    reusableFixtureDef.restitution = BODY_RESTITUTION;
    reusableFixtureDef.friction = BODY_FRICTION;
    reusableFixtureDef.isSensor = true;
  }

  /** Resets the factory for a new game session with a fresh engine reference. */
  public static void initialize(Engine newEngine) {
    if (instance != null) {
      instance.dispose();
    }
    instance = new BulletFactory(newEngine);
  }

  public static BulletFactory getInstance() {
    if (instance == null) {
      throw new IllegalStateException("BulletFactory.initialize(Engine) must be called first.");
    }
    return instance;
  }

  /**
   * Creates a bullet entity without a Box2D body.
   *
   * @param template bullet configuration
   * @param x starting x position in pixels
   * @param y starting y position in pixels
   * @param vx unused legacy velocity x parameter
   * @param vy unused legacy velocity y parameter
   * @param damage damage applied on hit
   * @return created bullet entity
   */
  public Entity createBullet(
      BulletTemplate template, float x, float y, float vx, float vy, int damage) {
    Entity bullet = engine.createEntity();

    addPositionComponent(bullet, x, y);
    addSpriteComponent(bullet, template.sprite, template.scale, DEFAULT_SPRITE_ANGLE_DEGREES);
    addBulletComponent(bullet, template.maxdistance, x, y);
    addDamageComponent(bullet, damage);

    engine.addEntity(bullet);
    return bullet;
  }

  /**
   * Creates a bullet entity with a Box2D body.
   *
   * @param template bullet configuration
   * @param x starting x position in pixels
   * @param y starting y position in pixels
   * @param vx velocity x in pixels per second
   * @param vy velocity y in pixels per second
   * @param damage damage applied on hit
   * @param angle sprite angle in degrees
   * @param world Box2D world used for the bullet body
   * @return created bullet entity
   */
  public Entity createBullet(
      BulletTemplate template,
      float x,
      float y,
      float vx,
      float vy,
      int damage,
      float angle,
      World world) {
    Entity bullet = engine.createEntity();

    addPositionComponent(bullet, x, y);
    addSpriteComponent(bullet, template.sprite, template.scale, angle);
    addBulletComponent(bullet, template.maxdistance, x, y);
    addDamageComponent(bullet, damage);
    addPhysicsComponent(
        bullet,
        world,
        x * PhysicsConstants.METERS_PER_PIXEL,
        y * PhysicsConstants.METERS_PER_PIXEL,
        template.scale / BULLET_RADIUS_DIVISOR * PhysicsConstants.METERS_PER_PIXEL,
        vx * PhysicsConstants.METERS_PER_PIXEL,
        vy * PhysicsConstants.METERS_PER_PIXEL);

    engine.addEntity(bullet);
    return bullet;
  }

  private void addPositionComponent(Entity bullet, float x, float y) {
    PositionComponent position = engine.createComponent(PositionComponent.class);
    position.x = x;
    position.y = y;
    bullet.add(position);
  }

  private void addSpriteComponent(Entity entity, String spritePath, float scale, float angle) {
    TextureRegion textureRegion = getOrCacheTexture(spritePath);
    SpriteComponent sprite = engine.createComponent(SpriteComponent.class);
    sprite.texture = textureRegion;
    sprite.scale = scale;
    sprite.angle = angle;
    entity.add(sprite);
  }

  private void addBulletComponent(Entity entity, float maxDistance, float x, float y) {
    BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
    bulletComponent.maxDistance = maxDistance;
    bulletComponent.startX = x;
    bulletComponent.startY = y;
    bulletComponent.distanceTravelled = INITIAL_DISTANCE_TRAVELLED;
    entity.add(bulletComponent);
  }

  private TextureRegion getOrCacheTexture(String spritePath) {
    TextureRegion cached = textureCache.get(spritePath);
    if (cached == null) {
      Texture texture = Assets.getManager().get(spritePath, Texture.class);
      cached = new TextureRegion(texture);
      textureCache.put(spritePath, cached);
    }
    return cached;
  }

  private void addDamageComponent(Entity entity, int damage) {
    DamageComponent damageComponent = engine.createComponent(DamageComponent.class);
    damageComponent.damage = damage;
    entity.add(damageComponent);
  }

  private void addPhysicsComponent(
      Entity entity, World world, float x, float y, float radius, float vx, float vy) {
    reusableBodyDef.type = BodyDef.BodyType.DynamicBody;
    reusableBodyDef.position.set(x, y);
    reusableBodyDef.bullet = true;
    Body body = world.createBody(reusableBodyDef);

    reusableShape.setRadius(radius);
    reusableFixtureDef.shape = reusableShape;
    body.createFixture(reusableFixtureDef);

    body.setLinearVelocity(vx, vy);
    body.setUserData(entity);

    PhysicsComponent physics = engine.createComponent(PhysicsComponent.class);
    physics.body = body;
    entity.add(physics);
  }

  private void dispose() {
    textureCache.clear();
    if (reusableShape != null) {
      reusableShape.dispose();
      reusableShape = null;
    }
  }
}
