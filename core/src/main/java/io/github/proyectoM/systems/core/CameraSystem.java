package io.github.proyectoM.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import java.util.Objects;

/** Keeps the camera centered on the group controller. */
public class CameraSystem extends IteratingSystem {
  private static final float CAMERA_FOLLOW_Z = 0f;
  private static final float CAMERA_SMOOTHNESS = 8f;
  private static final float MAX_FOLLOW_ALPHA = 1f;

  private final OrthographicCamera camera;
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);

  public CameraSystem(OrthographicCamera camera) {
    super(Family.all(GroupControllerComponent.class, PositionComponent.class).get());
    this.camera = Objects.requireNonNull(camera, "camera");
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = positionMapper.get(entity);
    float followAlpha = calculateFollowAlpha(deltaTime);
    moveCameraTowards(position.x, position.y, followAlpha);
  }

  public OrthographicCamera getCamera() {
    return camera;
  }

  public void resetCamera() {
    for (Entity entity : getEntities()) {
      PositionComponent position = positionMapper.get(entity);
      snapCameraTo(position.x, position.y);
      return;
    }
  }

  private float calculateFollowAlpha(float deltaTime) {
    return Math.min(MAX_FOLLOW_ALPHA, CAMERA_SMOOTHNESS * deltaTime);
  }

  private void moveCameraTowards(float targetX, float targetY, float followAlpha) {
    camera.position.x = MathUtils.lerp(camera.position.x, targetX, followAlpha);
    camera.position.y = MathUtils.lerp(camera.position.y, targetY, followAlpha);
    camera.update();
  }

  private void snapCameraTo(float targetX, float targetY) {
    camera.position.set(targetX, targetY, CAMERA_FOLLOW_Z);
    camera.update();
  }
}
