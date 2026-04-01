package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marks an entity as a muzzle flash tied to a specific weapon entity. */
public class MuzzleFlashComponent implements Component, Poolable {
  public Entity weaponEntity;

  @Override
  public void reset() {
    weaponEntity = null;
  }
}
