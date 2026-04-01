package io.github.proyectoM.systems.sync;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;

/**
 * Syncs weapons[0].targetEntity to character.targetEntity. Processes all characters (companions and
 * enemies).
 */
public class TargetSyncSystem extends IteratingSystem {

  private final ComponentMapper<InventoryComponent> inventoryMapper =
      ComponentMapper.getFor(InventoryComponent.class);
  private final ComponentMapper<TargetComponent> targetMapper =
      ComponentMapper.getFor(TargetComponent.class);
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);

  public TargetSyncSystem() {
    super(Family.all(InventoryComponent.class, TargetComponent.class).get());
  }

  @Override
  protected void processEntity(Entity character, float deltaTime) {
    InventoryComponent inventory = inventoryMapper.get(character);
    TargetComponent target = targetMapper.get(character);

    if (inventory.weapons.size > 0) {
      Entity primaryWeapon = inventory.weapons.first();
      WeaponComponent weaponComp = weaponMapper.get(primaryWeapon);

      if (weaponComp != null) {
        target.targetEntity = weaponComp.targetEntity;
      }
    } else {
      target.targetEntity = null;
    }
  }
}
