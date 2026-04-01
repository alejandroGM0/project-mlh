package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.WeaponTemplate;

/** Updates muzzle positions for equipped weapons based on the current animation frame. */
public class MuzzlePointUpdateSystem extends IteratingSystem {
  private static final float FRAME_SIZE = 128f;
  private static final float FRAME_CENTER = FRAME_SIZE / 2f;
  private static final float RENDER_SCALE = 3f;

  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<InventoryComponent> inventoryMapper =
      ComponentMapper.getFor(InventoryComponent.class);
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<MuzzlePointComponent> muzzleMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);

  public MuzzlePointUpdateSystem() {
    super(
        Family.all(
                AnimationComponent.class,
                PositionComponent.class,
                InventoryComponent.class,
                MuzzlePointComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    AnimationComponent animation = animationMapper.get(entity);
    if (animation.currentAnimation == null) {
      return;
    }

    PositionComponent position = positionMapper.get(entity);
    MuzzlePointComponent muzzlePoint = muzzleMapper.get(entity);
    InventoryComponent inventory = inventoryMapper.get(entity);
    for (Entity weaponEntity : inventory.weapons) {
      updateMuzzleForWeapon(weaponEntity, animation, position, muzzlePoint);
    }
  }

  private void updateMuzzleForWeapon(
      Entity weaponEntity,
      AnimationComponent animation,
      PositionComponent position,
      MuzzlePointComponent muzzlePoint) {
    WeaponComponent weapon = weaponMapper.get(weaponEntity);
    if (weapon == null) {
      return;
    }

    WeaponTemplate template = WeaponRegistry.getInstance().getTemplate(weapon.id);
    if (template == null) {
      return;
    }

    TextureRegion currentFrame = animation.currentAnimation.getKeyFrame(animation.stateTime);
    if (!(currentFrame instanceof TextureAtlas.AtlasRegion)) {
      return;
    }

    TextureAtlas.AtlasRegion atlasFrame = (TextureAtlas.AtlasRegion) currentFrame;
    Vector2 muzzleOffset = template.muzzlePoints.get(atlasFrame.name);
    if (muzzleOffset == null) {
      return;
    }

    float offsetX = (muzzleOffset.x - FRAME_CENTER) * RENDER_SCALE;
    float offsetY = (FRAME_CENTER - muzzleOffset.y) * RENDER_SCALE;
    muzzlePoint.position.set(position.x + offsetX, position.y + offsetY);
  }
}
