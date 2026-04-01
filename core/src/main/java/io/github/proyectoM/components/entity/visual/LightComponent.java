package io.github.proyectoM.components.entity.visual;

import box2dLight.Light;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Defines the configuration for a point, cone, or chain light emitted by an entity. */
public class LightComponent implements Component, Poolable {
  public enum LightType {
    POINT,
    CONE,
    CHAIN
  }

  public static final float DEFAULT_DISTANCE = 48f;
  public static final float DEFAULT_CONE_DEGREES = 30f;
  public static final float DEFAULT_DIRECTION_DEGREES = 0f;
  public static final float DEFAULT_DIRECTION_OFFSET_DEGREES = 0f;
  public static final float DEFAULT_SOFTNESS_LENGTH = 1.5f;
  public static final int DEFAULT_RAY_COUNT = 128;
  public static final boolean DEFAULT_ALIGN_TO_BODY_ANGLE = true;
  public static final boolean DEFAULT_ACTIVE = true;
  public static final boolean DEFAULT_XRAY = false;
  public static final boolean DEFAULT_ATTACH_TO_PHYSICS_BODY = true;
  public static final boolean DEFAULT_USE_CUSTOM_POSITION = false;

  public LightType type = LightType.POINT;
  public final Color color = new Color(Color.WHITE);
  public float distance = DEFAULT_DISTANCE;
  public float coneDegree = DEFAULT_CONE_DEGREES;
  public float coneDirectionDegrees = DEFAULT_DIRECTION_DEGREES;
  public float directionOffsetDegrees = DEFAULT_DIRECTION_OFFSET_DEGREES;
  public boolean alignToBodyAngle = DEFAULT_ALIGN_TO_BODY_ANGLE;
  public boolean active = DEFAULT_ACTIVE;
  public boolean xray = DEFAULT_XRAY;
  public boolean attachToPhysicsBody = DEFAULT_ATTACH_TO_PHYSICS_BODY;
  public boolean useCustomPosition = DEFAULT_USE_CUSTOM_POSITION;
  public int rays = DEFAULT_RAY_COUNT;
  public float softnessLength = DEFAULT_SOFTNESS_LENGTH;
  public final Vector2 positionMeters = new Vector2();
  public final Vector2 offsetMeters = new Vector2();
  public float[] chainVertices;
  public Light light;

  @Override
  public void reset() {
    type = LightType.POINT;
    color.set(Color.WHITE);
    distance = DEFAULT_DISTANCE;
    coneDegree = DEFAULT_CONE_DEGREES;
    coneDirectionDegrees = DEFAULT_DIRECTION_DEGREES;
    directionOffsetDegrees = DEFAULT_DIRECTION_OFFSET_DEGREES;
    alignToBodyAngle = DEFAULT_ALIGN_TO_BODY_ANGLE;
    active = DEFAULT_ACTIVE;
    xray = DEFAULT_XRAY;
    attachToPhysicsBody = DEFAULT_ATTACH_TO_PHYSICS_BODY;
    useCustomPosition = DEFAULT_USE_CUSTOM_POSITION;
    rays = DEFAULT_RAY_COUNT;
    softnessLength = DEFAULT_SOFTNESS_LENGTH;
    positionMeters.setZero();
    offsetMeters.setZero();
    chainVertices = null;
    light = null;
  }
}
