package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
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
  private final ComponentMapper<WeaponStateComponent> weaponStateMapper =
      ComponentMapper.getFor(WeaponStateComponent.class);
  private final ComponentMapper<RangedWeaponComponent> rangedMapper =
      ComponentMapper.getFor(RangedWeaponComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<DamageComponent> damageMapper =
      ComponentMapper.getFor(DamageComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<MuzzlePointComponent> muzzlePointMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);

  private final World world;
  private final BulletFactory bulletFactory;
  private final BulletRegistry bulletRegistry;
  private final Vector2 velocity = new Vector2();

  public RangedWeaponSystem(World world, BulletFactory bulletFactory, BulletRegistry bulletRegistry) {
    super(
        Family.all(RangedWeaponComponent.class, WeaponComponent.class, ParentComponent.class)
            .get());
    this.world = world;
    this.bulletFactory = bulletFactory;
    this.bulletRegistry = bulletRegistry;
  }

  @Override
  protected void processEntity(Entity weaponEntity, float deltaTime) {
    WeaponComponent weapon = weaponMapper.get(weaponEntity);
    WeaponStateComponent weaponState = weaponStateMapper.get(weaponEntity);
    updateCooldown(weaponState, deltaTime);

    if (weaponState.targetEntity == null) {
      weaponState.isAttacking = false;
      return;
    }

    weaponState.isAttacking = true;
    if (weaponState.cooldown > 0f) {
      return;
    }

    fireBullet(weaponEntity, weapon, weaponState, rangedMapper.get(weaponEntity));
    weaponState.cooldown = weapon.attackSpeed;
  }

  private void updateCooldown(WeaponStateComponent weaponState, float deltaTime) {
    if (weaponState.cooldown > 0f) {
      weaponState.cooldown -= deltaTime;
    }
  }

  private void fireBullet(
      Entity weaponEntity, WeaponComponent weapon, WeaponStateComponent weaponState,
      RangedWeaponComponent rangedWeapon) {
    Entity owner = parentMapper.get(weaponEntity).parent;
    MuzzlePointComponent muzzlePoint = muzzlePointMapper.get(owner);
    PositionComponent targetPosition = positionMapper.get(weaponState.targetEntity);
    BulletTemplate bulletTemplate = bulletRegistry.getTemplate(rangedWeapon.bulletType);

    weaponState.flashTimer = FLASH_DURATION;
    calculateVelocity(muzzlePoint, targetPosition, bulletTemplate.speed);

    float startAngle = movementMapper.get(owner).faceAngle;
    int damage = damageMapper.get(owner).damage;

    Entity bulletEntity =
        bulletFactory
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

  private void configureHoming(Entity bulletEntity, PositionComponent targetPosition, float speed) {
    BulletComponent bullet = bulletEntity.getComponent(BulletComponent.class);
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
}
