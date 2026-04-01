package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import io.github.proyectoM.components.entity.visual.FlickerLightComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;

/** Applies a sine-based flicker to light distance. */
public class LightFlickerSystem extends IteratingSystem {
  private final ComponentMapper<FlickerLightComponent> flickerMapper =
      ComponentMapper.getFor(FlickerLightComponent.class);
  private final ComponentMapper<LightComponent> lightMapper =
      ComponentMapper.getFor(LightComponent.class);

  public LightFlickerSystem() {
    super(Family.all(LightComponent.class, FlickerLightComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    FlickerLightComponent flicker = flickerMapper.get(entity);
    LightComponent light = lightMapper.get(entity);
    if (!flicker.isFlickering) {
      restoreBaseDistance(light, flicker);
      return;
    }

    initializeBaseDistanceIfNeeded(light, flicker);
    flicker.timer += deltaTime * flicker.speed;
    light.distance = calculateFlickerDistance(flicker);
  }

  private void restoreBaseDistance(LightComponent light, FlickerLightComponent flicker) {
    if (flicker.baseDistance > 0f && light.distance != flicker.baseDistance) {
      light.distance = flicker.baseDistance;
    }
  }

  private void initializeBaseDistanceIfNeeded(LightComponent light, FlickerLightComponent flicker) {
    if (flicker.baseDistance <= 0f) {
      flicker.baseDistance = light.distance;
    }
  }

  private float calculateFlickerDistance(FlickerLightComponent flicker) {
    float sineWave = MathUtils.sin(flicker.timer);
    float distanceVariation = flicker.baseDistance * flicker.amount * sineWave;
    return flicker.baseDistance + distanceVariation;
  }
}
