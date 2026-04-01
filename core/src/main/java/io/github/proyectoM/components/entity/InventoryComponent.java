package io.github.proyectoM.components.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the weapons currently attached to an entity. */
public class InventoryComponent implements Component, Poolable {
  public static final int INVENTORY_SIZE = 10;

  public final Array<Entity> weapons = new Array<>(INVENTORY_SIZE);

  public boolean addWeapon(Entity weapon) {
    if (hasCapacity()) {
      weapons.add(weapon);
      return true;
    }
    return false;
  }

  public boolean hasCapacity() {
    return weapons.size < INVENTORY_SIZE;
  }

  @Override
  public void reset() {
    weapons.clear();
  }
}
