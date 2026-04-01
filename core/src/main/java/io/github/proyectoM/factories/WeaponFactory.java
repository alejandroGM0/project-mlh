package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import io.github.proyectoM.animation.AnimationKey;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.weapon.MuzzleFlashComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.MeleeWeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.RangedWeaponComponent;
import io.github.proyectoM.components.visual.VisualAssetComponent;
import io.github.proyectoM.registry.VisualAssetRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.WeaponTemplate;
import java.util.Objects;

/** Creates weapon entities and their optional muzzle-flash helpers. */
public final class WeaponFactory {
  private static final int DEFAULT_IDLE_VARIANT = 0;
  private static final float INITIAL_STATE_TIME = 0f;
  private static final float MUZZLE_LIGHT_RED = 1f;
  private static final float MUZZLE_LIGHT_GREEN = 0.9f;
  private static final float MUZZLE_LIGHT_BLUE = 0.6f;
  private static final float MUZZLE_LIGHT_ALPHA = 1f;
  private static final float MUZZLE_LIGHT_DISTANCE = 12f;
  private static final float MUZZLE_LIGHT_CONE_DEGREES = 60f;
  private static final int MUZZLE_LIGHT_RAY_COUNT = 64;
  private static final float MUZZLE_LIGHT_SOFTNESS_LENGTH = 1f;

  private WeaponFactory() {}

  public static Entity createWeapon(Engine engine, String weaponId, Entity owner) {
    Objects.requireNonNull(engine, "engine");
    Objects.requireNonNull(owner, "owner");

    WeaponTemplate template = getRequiredTemplate(weaponId);
    Entity weapon = engine.createEntity();

    addCommonWeaponComponents(engine, weapon, template, owner);
    addSpecificWeaponComponents(engine, weapon, template);

    if (hasAnimatedAtlas(template)) {
      initializeIdleAnimation(engine, weapon, template.atlas);
    }

    if (hasFlashAtlas(template)) {
      Entity flashEntity = createFlashEntity(engine, weapon, template, owner);
      engine.addEntity(flashEntity);
    }

    engine.addEntity(weapon);
    return weapon;
  }

  private static WeaponTemplate getRequiredTemplate(String weaponId) {
    WeaponTemplate template = WeaponRegistry.getInstance().getTemplate(weaponId);
    if (template == null) {
      throw new IllegalArgumentException("Unknown weapon template: " + weaponId);
    }
    return template;
  }

  private static void addCommonWeaponComponents(
      Engine engine, Entity weapon, WeaponTemplate template, Entity owner) {
    WeaponComponent wc = engine.createComponent(WeaponComponent.class);
    wc.id = template.id;
    wc.attackRange = template.attackRange;
    wc.targetRange = template.targetRange;
    wc.attackSpeed = template.attackSpeed;
    wc.reloadTime = template.reloadTime;
    wc.damage = template.damage;
    wc.sound = template.sound;
    wc.damageFrame = template.damageFrame;
    weapon.add(wc);
    weapon.add(createWeaponActionState(engine, template.attackVariant));
    PositionComponent pos = engine.createComponent(PositionComponent.class);
    weapon.add(pos);
    ParentComponent parent = engine.createComponent(ParentComponent.class);
    parent.parent = owner;
    weapon.add(parent);

    if (hasAnimatedAtlas(template)) {
      VisualAssetComponent visual = engine.createComponent(VisualAssetComponent.class);
      visual.visualAssetId = template.atlas;
      weapon.add(visual);
      weapon.add(engine.createComponent(MovementDirectionStateComponent.class));
    }
  }

  private static ActionStateComponent createWeaponActionState(Engine engine, int attackVariant) {
    ActionStateComponent action = engine.createComponent(ActionStateComponent.class);
    action.actionType = ActionStateComponent.ActionType.IDLE;
    action.setVariant(ActionStateComponent.ActionType.ATTACK, attackVariant);
    return action;
  }

  private static void addSpecificWeaponComponents(Engine engine, Entity weapon, WeaponTemplate template) {
    switch (template.type) {
      case WeaponTemplate.TYPE_RANGED:
        RangedWeaponComponent ranged = engine.createComponent(RangedWeaponComponent.class);
        ranged.bulletType = template.bulletType;
        weapon.add(ranged);
        break;
      case WeaponTemplate.TYPE_MELEE:
        weapon.add(engine.createComponent(MeleeWeaponComponent.class));
        break;
      default:
        throw new IllegalArgumentException("Unsupported weapon type: " + template.type);
    }
  }

  private static boolean hasAnimatedAtlas(WeaponTemplate template) {
    return template.atlas != null
        && !template.atlas.isBlank()
        && !template.atlas.equals(template.id);
  }

  private static boolean hasFlashAtlas(WeaponTemplate template) {
    return template.flashAtlas != null && !template.flashAtlas.isBlank();
  }

  private static void initializeIdleAnimation(Engine engine, Entity weapon, String atlasPath) {
    AnimationComponent animation = engine.createComponent(AnimationComponent.class);
    animation.currentAnimation =
        VisualAssetRegistry.getAnimation(
            atlasPath,
            AnimationKey.get(
                ActionStateComponent.ActionType.IDLE,
                MovementDirectionStateComponent.MovementType.FORWARD,
                DEFAULT_IDLE_VARIANT));
    animation.stateTime = INITIAL_STATE_TIME;
    weapon.add(animation);
  }

  private static Entity createFlashEntity(
      Engine engine, Entity weapon, WeaponTemplate template, Entity owner) {
    Entity flashEntity = engine.createEntity();

    flashEntity.add(createFlashComponent(engine, weapon));
    PositionComponent pos = engine.createComponent(PositionComponent.class);
    flashEntity.add(pos);
    flashEntity.add(engine.createComponent(MovementDirectionStateComponent.class));
    flashEntity.add(createFlashActionState(engine, template.attackVariant));
    VisualAssetComponent visual = engine.createComponent(VisualAssetComponent.class);
    visual.visualAssetId = template.flashAtlas;
    flashEntity.add(visual);
    ParentComponent parent = engine.createComponent(ParentComponent.class);
    parent.parent = owner;
    flashEntity.add(parent);
    flashEntity.add(createFlashAnimation(engine, template));
    flashEntity.add(createMuzzleLight(engine));

    weapon.getComponent(WeaponComponent.class).flashEntity = flashEntity;
    return flashEntity;
  }

  private static MuzzleFlashComponent createFlashComponent(Engine engine, Entity weapon) {
    MuzzleFlashComponent flashComponent = engine.createComponent(MuzzleFlashComponent.class);
    flashComponent.weaponEntity = weapon;
    return flashComponent;
  }

  private static ActionStateComponent createFlashActionState(Engine engine, int attackVariant) {
    ActionStateComponent action = engine.createComponent(ActionStateComponent.class);
    action.actionType = ActionStateComponent.ActionType.ATTACK;
    action.setVariant(ActionStateComponent.ActionType.ATTACK, attackVariant);
    return action;
  }

  private static AnimationComponent createFlashAnimation(Engine engine, WeaponTemplate template) {
    AnimationComponent animation = engine.createComponent(AnimationComponent.class);
    animation.currentAnimation =
        VisualAssetRegistry.getAnimation(
            template.flashAtlas,
            AnimationKey.get(
                ActionStateComponent.ActionType.ATTACK,
                MovementDirectionStateComponent.MovementType.FORWARD,
                DEFAULT_IDLE_VARIANT,
                template.attackVariant));
    animation.stateTime = INITIAL_STATE_TIME;
    return animation;
  }

  private static LightComponent createMuzzleLight(Engine engine) {
    LightComponent muzzleLight = engine.createComponent(LightComponent.class);
    muzzleLight.type = LightComponent.LightType.CONE;
    muzzleLight.color.set(
        MUZZLE_LIGHT_RED, MUZZLE_LIGHT_GREEN, MUZZLE_LIGHT_BLUE, MUZZLE_LIGHT_ALPHA);
    muzzleLight.distance = MUZZLE_LIGHT_DISTANCE;
    muzzleLight.coneDegree = MUZZLE_LIGHT_CONE_DEGREES;
    muzzleLight.rays = MUZZLE_LIGHT_RAY_COUNT;
    muzzleLight.softnessLength = MUZZLE_LIGHT_SOFTNESS_LENGTH;
    muzzleLight.xray = true;
    muzzleLight.active = false;
    muzzleLight.attachToPhysicsBody = false;
    muzzleLight.alignToBodyAngle = false;
    return muzzleLight;
  }
}
