package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;

/** Periodically assigns the nearest valid target to each weapon entity. */
public class TargetSelectorSystem extends IteratingSystem {
  private static final float TARGET_SEARCH_INTERVAL = 0.2f;

  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<CompanionComponent> companionMapper =
      ComponentMapper.getFor(CompanionComponent.class);

  private float searchTimer = 0f;

  public TargetSelectorSystem() {
    super(Family.all(WeaponComponent.class, PositionComponent.class, ParentComponent.class).get());
  }

  @Override
  public void update(float deltaTime) {
    searchTimer += deltaTime;
    if (searchTimer < TARGET_SEARCH_INTERVAL) {
      return;
    }

    searchTimer = 0f;
    super.update(deltaTime);
  }

  @Override
  protected void processEntity(Entity weaponEntity, float deltaTime) {
    ParentComponent parentComponent = parentMapper.get(weaponEntity);
    if (parentComponent.parent == null) {
      return;
    }

    WeaponComponent weapon = weaponMapper.get(weaponEntity);
    PositionComponent weaponPosition = positionMapper.get(weaponEntity);
    CompanionComponent ownerCompanion = companionMapper.get(parentComponent.parent);

    Family targetFamily = getTargetFamily(ownerCompanion);
    float maxRange = getMaxTargetRange(weapon, ownerCompanion);
    weapon.targetEntity = findNearestTarget(weaponPosition, maxRange, targetFamily);
  }

  private Family getTargetFamily(CompanionComponent ownerCompanion) {
    Class<? extends Component> targetType =
        ownerCompanion != null ? EnemyComponent.class : CompanionComponent.class;
    return Family.all(targetType, PositionComponent.class).exclude(DeadComponent.class).get();
  }

  private float getMaxTargetRange(WeaponComponent weapon, CompanionComponent ownerCompanion) {
    if (ownerCompanion == null) {
      return Float.POSITIVE_INFINITY;
    }
    return weapon.targetRange * ownerCompanion.rangeMultiplier;
  }

  private Entity findNearestTarget(
      PositionComponent sourcePosition, float maxRange, Family targetFamily) {
    ImmutableArray<Entity> candidates = getEngine().getEntitiesFor(targetFamily);
    Entity closestTarget = null;
    float closestDistanceSquared = maxRange * maxRange;

    for (int index = 0; index < candidates.size(); index++) {
      Entity candidate = candidates.get(index);
      PositionComponent candidatePosition = positionMapper.get(candidate);
      float distanceSquared = calculateDistanceSquared(sourcePosition, candidatePosition);
      if (distanceSquared <= closestDistanceSquared) {
        closestDistanceSquared = distanceSquared;
        closestTarget = candidate;
      }
    }

    return closestTarget;
  }

  private float calculateDistanceSquared(PositionComponent first, PositionComponent second) {
    float dx = first.x - second.x;
    float dy = first.y - second.y;
    return (dx * dx) + (dy * dy);
  }
}
