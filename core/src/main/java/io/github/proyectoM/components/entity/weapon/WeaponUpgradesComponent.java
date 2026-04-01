package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.HashMap;
import java.util.Map;

/** Stores per-weapon upgrade state for the current run. */
public class WeaponUpgradesComponent implements Component, Poolable {
  public static class WeaponUpgrade {
    public static final float DEFAULT_FIRE_RATE_MULTIPLIER = 1f;
    public static final float DEFAULT_BULLET_SPEED_MULTIPLIER = 1f;

    public float fireRateMultiplier = DEFAULT_FIRE_RATE_MULTIPLIER;
    public float bulletSpeedMultiplier = DEFAULT_BULLET_SPEED_MULTIPLIER;
    public int fireRateUpgrades = 0;
    public int bulletSpeedUpgrades = 0;

    public WeaponUpgrade() {}

    public WeaponUpgrade(
        float fireRateMultiplier,
        float bulletSpeedMultiplier,
        int fireRateUpgrades,
        int bulletSpeedUpgrades) {
      this.fireRateMultiplier = fireRateMultiplier;
      this.bulletSpeedMultiplier = bulletSpeedMultiplier;
      this.fireRateUpgrades = fireRateUpgrades;
      this.bulletSpeedUpgrades = bulletSpeedUpgrades;
    }
  }

  public final Map<String, WeaponUpgrade> weaponUpgrades = new HashMap<>();

  public WeaponUpgrade getWeaponUpgrade(String weaponId) {
    return weaponUpgrades.computeIfAbsent(weaponId, ignored -> new WeaponUpgrade());
  }

  public void applyFireRateUpgrade(String weaponId, float multiplier) {
    WeaponUpgrade upgrade = getWeaponUpgrade(weaponId);
    upgrade.fireRateMultiplier *= multiplier;
    upgrade.fireRateUpgrades++;
  }

  public void applyBulletSpeedUpgrade(String weaponId, float multiplier) {
    WeaponUpgrade upgrade = getWeaponUpgrade(weaponId);
    upgrade.bulletSpeedMultiplier *= multiplier;
    upgrade.bulletSpeedUpgrades++;
  }

  @Override
  public void reset() {
    weaponUpgrades.clear();
  }
}
