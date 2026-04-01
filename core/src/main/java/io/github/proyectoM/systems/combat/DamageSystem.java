package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.combat.PendingDamageComponent;

/** Applies pending damage to living entities and then clears the pending marker. */
public class DamageSystem extends IteratingSystem {
  private final ComponentMapper<HealthComponent> healthMapper =
      ComponentMapper.getFor(HealthComponent.class);
  private final ComponentMapper<PendingDamageComponent> pendingDamageMapper =
      ComponentMapper.getFor(PendingDamageComponent.class);

  public DamageSystem() {
    super(
        Family.all(HealthComponent.class, PendingDamageComponent.class)
            .exclude(DeadComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    HealthComponent health = healthMapper.get(entity);
    PendingDamageComponent pendingDamage = pendingDamageMapper.get(entity);

    int appliedDamage = (int) pendingDamage.amount;
    health.currentHealth -= appliedDamage;
    entity.remove(PendingDamageComponent.class);
  }
}
