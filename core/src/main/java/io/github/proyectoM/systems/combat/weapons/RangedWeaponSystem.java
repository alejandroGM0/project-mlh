package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.RangedWeaponComponent;
import io.github.proyectoM.factories.BulletFactory;
import io.github.proyectoM.registry.BulletRegistry;
import io.github.proyectoM.templates.BulletTemplate;

/** Spawns bullets for ranged weapons when a valid target is available. */
public class RangedWeaponSystem extends IteratingSystem {
  private static final float FLASH_DURATION = 0.1f;
  private static final float DEFAULT_HOMING_STRENGTH = 5f;

  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<RangedWeaponComponent> rangedMapper =
      ComponentMapper.getFor(RangedWeaponComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<DamageComponent> damageMapper =
      ComponentMapper.getFor(DamageComponent.class);
  private final ComponentMapper<CompanionComponent> companionMapper =
      ComponentMapper.getFor(CompanionComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<MuzzlePointComponent> muzzlePointMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);

  private final World world;
  private final Vector2 velocity = new Vector2();

  public RangedWeaponSystem(World world) {
    super(
        Family.all(RangedWeaponComponent.class, WeaponComponent.class, ParentComponent.class)
            .get());
    this.world = world;
  }

  @Override
  protected void processEntity(Entity weaponEntity, float deltaTime) {
    WeaponComponent weapon = weaponMapper.get(weaponEntity);
    updateCooldown(weapon, deltaTime);

    if (weapon.targetEntity == null) {
      weapon.isAttacking = false;
      return;
    }

    weapon.isAttacking = true;
    if (weapon.cooldown > 0f) {
      return;
    }

    fireBullet(weaponEntity, weapon, rangedMapper.get(weaponEntity));
    weapon.cooldown = weapon.attackSpeed;
  }

  private void updateCooldown(WeaponComponent weapon, float deltaTime) {
    if (weapon.cooldown > 0f) {
      weapon.cooldown -= deltaTime;
    }
  }

  private void fireBullet(
      Entity weaponEntity, WeaponComponent weapon, RangedWeaponComponent rangedWeapon) {
    Entity owner = parentMapper.get(weaponEntity).parent;
    if (owner == null || weapon.targetEntity == null) {
      return;
    }

    MuzzlePointComponent muzzlePoint = muzzlePointMapper.get(owner);
    PositionComponent targetPosition = positionMapper.get(weapon.targetEntity);
    BulletTemplate bulletTemplate =
        BulletRegistry.getInstance().getTemplate(rangedWeapon.bulletType);
    if (muzzlePoint == null || targetPosition == null || bulletTemplate == null) {
      return;
    }

    weapon.flashTimer = FLASH_DURATION;
    calculateVelocity(muzzlePoint, targetPosition, bulletTemplate.speed);

    float targetAngle = velocity.angleDeg();
    float startAngle = getVisualAngle(owner, targetAngle);
    int damage = Math.round(getDamage(owner));

    Entity bulletEntity =
        BulletFactory.getInstance()
            .createBullet(
                bulletTemplate,
                muzzlePoint.position.x,
                muzzlePoint.position.y,
                velocity.x,
                velocity.y,
                damage,
                startAngle,
                world);
    configureHoming(bulletEntity, targetPosition, bulletTemplate.speed);
  }

  private float getVisualAngle(Entity owner, float fallbackAngle) {
    MovementDirectionStateComponent movement = movementMapper.get(owner);
    return movement != null ? movement.faceAngle : fallbackAngle;
  }

  private void configureHoming(Entity bulletEntity, PositionComponent targetPosition, float speed) {
    BulletComponent bullet = bulletEntity.getComponent(BulletComponent.class);
    if (bullet == null) {
      return;
    }

    bullet.isHoming = true;
    bullet.targetX = targetPosition.x;
    bullet.targetY = targetPosition.y;
    bullet.speed = speed;
    bullet.homingStrength = DEFAULT_HOMING_STRENGTH;
  }

  private void calculateVelocity(
      MuzzlePointComponent muzzlePoint, PositionComponent targetPosition, float speed) {
    velocity.set(targetPosition.x, targetPosition.y).sub(muzzlePoint.position).nor().scl(speed);
  }

  private float getDamage(Entity owner) {
    DamageComponent damageComponent = damageMapper.get(owner);
    if (damageComponent != null) {
      return damageComponent.damage;
    }
    CompanionComponent companion = companionMapper.get(owner);
    return companion != null ? companion.damage : DamageComponent.DEFAULT_DAMAGE;
  }
}
