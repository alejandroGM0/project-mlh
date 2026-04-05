package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.animation.AnimationKey;
import io.github.proyectoM.components.entity.AIComponent;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.visual.VisualAssetComponent;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.registry.VisualAssetRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import io.github.proyectoM.templates.WeaponTemplate;
import java.util.Objects;

/** Shared helpers for creating character entities with combat, animation, and physics. */
public abstract class AbstractCharacterFactory {
  protected static final int DEFAULT_IDLE_VARIANT = 0;
  protected static final float CHARACTER_BODY_RADIUS_METERS = 1.25f;

  private static final float BODY_DENSITY = 1f;
  private static final float BODY_FRICTION = 0.2f;
  private static final float BODY_RESTITUTION = 0f;
  private static final float LINEAR_DAMPING = 4f;
  private static final float INITIAL_ANIMATION_STATE_TIME = 0f;
  private static final BodyDef.BodyType CHARACTER_BODY_TYPE = BodyDef.BodyType.DynamicBody;

  protected final Engine engine;
  protected final World world;
  protected final WeaponRegistry weaponRegistry;

  protected AbstractCharacterFactory(Engine engine, World world, WeaponRegistry weaponRegistry) {
    this.engine = Objects.requireNonNull(engine, "engine");
    this.world = world;
    this.weaponRegistry = Objects.requireNonNull(weaponRegistry, "weaponRegistry");
  }

  protected void addBaseStats(Entity entity, Vector2 positionMeters, int health, int damage) {
    PositionComponent position = engine.createComponent(PositionComponent.class);
    position.x = positionMeters.x * PhysicsConstants.PIXELS_PER_METER;
    position.y = positionMeters.y * PhysicsConstants.PIXELS_PER_METER;
    entity.add(position);
    HealthComponent healthComp = engine.createComponent(HealthComponent.class);
    healthComp.maxHealth = health;
    healthComp.currentHealth = health;
    entity.add(healthComp);
    DamageComponent damageComp = engine.createComponent(DamageComponent.class);
    damageComp.damage = damage;
    entity.add(damageComp);
  }

  protected void addWeaponSystem(Entity entity, Engine engine, String weaponId) {
    InventoryComponent inventory = engine.createComponent(InventoryComponent.class);
    entity.add(inventory);

    WeaponTemplate weaponTemplate = getRequiredWeaponTemplate(weaponId);
    if (WeaponTemplate.TYPE_RANGED.equals(weaponTemplate.type)) {
      entity.add(engine.createComponent(MuzzlePointComponent.class));
    }

    Entity weapon = WeaponFactory.createWeapon(engine, weaponId, entity);
    inventory.addWeapon(weapon);
  }

  protected void addPhysicsComponent(Entity entity, Vector2 positionMeters, float radiusMeters) {
    Body body = createCharacterBody(positionMeters);
    attachCircleFixture(body, radiusMeters);
    linkPhysicsToEntity(entity, body);
  }

  protected void addAIComponent(Entity entity, CharacterTemplate template) {
    AIComponent ai = engine.createComponent(AIComponent.class);
    ai.id = template.id;
    ai.name = template.name;
    ai.speed = template.speed;
    ai.armor = template.armor;
    ai.baseDamage = template.damage;
    ai.baseRange = template.attackRangeMultiplier;
    ai.cooldownAttack = template.attackCooldownTime;
    ai.mass = template.mass;
    entity.add(ai);
  }

  protected void addCombatComponents(Entity entity) {
    entity.add(engine.createComponent(LookAtComponent.class));
    entity.add(engine.createComponent(TargetComponent.class));
  }

  /**
   * Adds visual and animation components shared by all character types.
   *
   * @param entity the entity to configure
   * @param atlasPath visual asset identifier for the character
   * @param weaponId weapon template id used to resolve the attack animation variant
   */
  protected void addVisualAndAnimationComponents(
      Entity entity, String atlasPath, String weaponId) {
    entity.add(engine.createComponent(MovementDirectionStateComponent.class));

    VisualAssetComponent visual = engine.createComponent(VisualAssetComponent.class);
    visual.visualAssetId = atlasPath;
    entity.add(visual);

    int attackVariant = resolveAttackVariant(weaponId);
    ActionStateComponent action = createIdleActionState(attackVariant);
    entity.add(action);

    addInitialIdleAnimation(entity, atlasPath);
  }

  protected int resolveAttackVariant(String weaponId) {
    return getRequiredWeaponTemplate(weaponId).attackVariant;
  }

  protected ActionStateComponent createIdleActionState(int attackVariant) {
    ActionStateComponent action = engine.createComponent(ActionStateComponent.class);
    action.actionType = ActionStateComponent.ActionType.IDLE;
    action.setVariant(ActionStateComponent.ActionType.ATTACK, attackVariant);
    return action;
  }

  protected void addInitialIdleAnimation(Entity entity, String atlasPath) {
    AnimationComponent animation = engine.createComponent(AnimationComponent.class);
    animation.currentAnimation =
        VisualAssetRegistry.getAnimation(
            atlasPath,
            AnimationKey.get(
                ActionStateComponent.ActionType.IDLE,
                MovementDirectionStateComponent.MovementType.FORWARD,
                DEFAULT_IDLE_VARIANT));
    animation.stateTime = INITIAL_ANIMATION_STATE_TIME;
    entity.add(animation);
  }

  private WeaponTemplate getRequiredWeaponTemplate(String weaponId) {
    return weaponRegistry.getRequired(weaponId);
  }

  private Body createCharacterBody(Vector2 positionMeters) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = CHARACTER_BODY_TYPE;
    bodyDef.position.set(Objects.requireNonNull(positionMeters, "positionMeters"));

    Body body = world.createBody(bodyDef);
    body.setLinearDamping(LINEAR_DAMPING);
    return body;
  }

  private void attachCircleFixture(Body body, float radiusMeters) {
    CircleShape shape = new CircleShape();
    shape.setRadius(radiusMeters);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = BODY_DENSITY;
    fixtureDef.friction = BODY_FRICTION;
    fixtureDef.restitution = BODY_RESTITUTION;

    body.createFixture(fixtureDef);
    shape.dispose();
  }

  private void linkPhysicsToEntity(Entity entity, Body body) {
    body.setUserData(entity);

    PhysicsComponent physics = engine.createComponent(PhysicsComponent.class);
    physics.body = body;
    entity.add(physics);
  }
}
