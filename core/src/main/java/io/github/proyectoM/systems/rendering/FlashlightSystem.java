package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Aligns cone lights with muzzle position and facing direction. */
public class FlashlightSystem extends IteratingSystem {
  private final ComponentMapper<MuzzlePointComponent> muzzleMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);
  private final ComponentMapper<LookAtComponent> lookAtMapper =
      ComponentMapper.getFor(LookAtComponent.class);
  private final ComponentMapper<LightComponent> lightMapper =
      ComponentMapper.getFor(LightComponent.class);

  public FlashlightSystem() {
    super(
        Family.all(MuzzlePointComponent.class, LookAtComponent.class, LightComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    MuzzlePointComponent muzzle = muzzleMapper.get(entity);
    LookAtComponent lookAt = lookAtMapper.get(entity);
    LightComponent light = lightMapper.get(entity);
    if (!isActiveConeLight(light)) {
      return;
    }

    updatePosition(light, muzzle);
    light.coneDirectionDegrees = (float) Math.toDegrees(lookAt.angle);
  }

  private boolean isActiveConeLight(LightComponent light) {
    return light.active && light.type == LightComponent.LightType.CONE;
  }

  private void updatePosition(LightComponent light, MuzzlePointComponent muzzle) {
    light.positionMeters.set(
        muzzle.position.x * PhysicsConstants.METERS_PER_PIXEL,
        muzzle.position.y * PhysicsConstants.METERS_PER_PIXEL);
    light.useCustomPosition = true;
    light.attachToPhysicsBody = false;
  }
}
