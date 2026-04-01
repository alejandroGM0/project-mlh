package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;

/** Updates entity facing angles so they look toward their current target. */
public class LookAtSystem extends IteratingSystem {
  private static final float ISOMETRIC_ANGLE_OFFSET_RADIANS = (float) (Math.PI / 4f);

  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<LookAtComponent> lookAtMapper =
      ComponentMapper.getFor(LookAtComponent.class);
  private final ComponentMapper<TargetComponent> targetMapper =
      ComponentMapper.getFor(TargetComponent.class);

  public LookAtSystem() {
    super(
        Family.all(PositionComponent.class, LookAtComponent.class, TargetComponent.class)
            .exclude(DeadComponent.class, AttackingComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    TargetComponent target = targetMapper.get(entity);
    if (target.targetEntity == null) {
      return;
    }

    PositionComponent targetPosition = positionMapper.get(target.targetEntity);
    if (targetPosition == null) {
      return;
    }

    PositionComponent position = positionMapper.get(entity);
    LookAtComponent lookAt = lookAtMapper.get(entity);
    float dx = targetPosition.x - position.x;
    float dy = targetPosition.y - position.y;
    lookAt.angle = ((float) Math.atan2(dy, dx)) - ISOMETRIC_ANGLE_OFFSET_RADIANS;
  }
}
