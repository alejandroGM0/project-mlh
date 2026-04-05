package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.weapon.MuzzleFlashComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Controls muzzle flash playback and attached light positioning. */
public class MuzzleFlashSystem extends IteratingSystem {
  private static final float FRAME_DURATION = 0.1f;
  private static final int FLASH_FRAME_COUNT = 15;
  public static final float FLASH_DURATION = FLASH_FRAME_COUNT * FRAME_DURATION;

  private final ComponentMapper<MuzzleFlashComponent> flashMapper =
      ComponentMapper.getFor(MuzzleFlashComponent.class);
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<WeaponStateComponent> weaponStateMapper =
      ComponentMapper.getFor(WeaponStateComponent.class);
  private final ComponentMapper<LightComponent> lightMapper =
      ComponentMapper.getFor(LightComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<MuzzlePointComponent> muzzlePointMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);
  private final ComponentMapper<LookAtComponent> lookAtMapper =
      ComponentMapper.getFor(LookAtComponent.class);

  public MuzzleFlashSystem() {
    super(Family.all(MuzzleFlashComponent.class, AnimationComponent.class).get());
  }

  @Override
  protected void processEntity(Entity flashEntity, float deltaTime) {
    MuzzleFlashComponent flash = flashMapper.get(flashEntity);
    WeaponStateComponent weaponState = weaponStateMapper.get(flash.weaponEntity);
    AnimationComponent animation = animationMapper.get(flashEntity);
    LightComponent light = lightMapper.get(flashEntity);

    if (weaponState == null || light == null) {
      return;
    }

    if (weaponState.flashTimer > 0f) {
      updateActiveFlash(flash.weaponEntity, weaponState, animation, light, deltaTime);
      return;
    }

    animation.stateTime = 0f;
    light.active = false;
  }

  private void updateActiveFlash(
      Entity weaponEntity,
      WeaponStateComponent weaponState,
      AnimationComponent animation,
      LightComponent light,
      float deltaTime) {
    weaponState.flashTimer -= deltaTime;
    animation.stateTime =
        animation.stateTime == 0f ? FRAME_DURATION : animation.stateTime + deltaTime;
    light.active = true;
    updateLightPosition(weaponEntity, light);
  }

  private void updateLightPosition(Entity weaponEntity, LightComponent light) {
    ParentComponent weaponParent = parentMapper.get(weaponEntity);
    if (weaponParent == null || weaponParent.parent == null) {
      return;
    }

    Entity owner = weaponParent.parent;
    MuzzlePointComponent muzzlePoint = muzzlePointMapper.get(owner);
    if (muzzlePoint == null) {
      return;
    }

    light.positionMeters.set(
        muzzlePoint.position.x * PhysicsConstants.METERS_PER_PIXEL,
        muzzlePoint.position.y * PhysicsConstants.METERS_PER_PIXEL);
    light.useCustomPosition = true;
    light.attachToPhysicsBody = false;

    LookAtComponent lookAt = lookAtMapper.get(owner);
    if (lookAt != null) {
      light.coneDirectionDegrees = (float) Math.toDegrees(lookAt.angle);
    }
  }
}
