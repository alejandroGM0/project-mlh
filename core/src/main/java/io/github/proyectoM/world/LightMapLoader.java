package io.github.proyectoM.world;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.FlickerLightComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Builds static light entities from Tiled map object layers. */
public final class LightMapLoader {
  private static final String LIGHT_LAYER = "lights";
  private static final String TYPE_KEY = "type";
  private static final String CONE_TYPE = "cone";
  private static final String CHAIN_TYPE = "chain";
  private static final String COLOR_KEY = "color";
  private static final String DISTANCE_KEY = "distance";
  private static final String CONE_DEGREE_KEY = "cone_degree";
  private static final String DIRECTION_KEY = "direction";
  private static final String XRAY_KEY = "xray";
  private static final String RAYS_KEY = "rays";
  private static final String SOFTNESS_KEY = "softness";
  private static final String FLICKER_KEY = "flicker";
  private static final String FLICKER_AMOUNT_KEY = "flickerAmount";
  private static final String FLICKER_SPEED_KEY = "flickerSpeed";
  private static final String AMBIENT_KEY = "ambient";
  private static final String AMBIENT_COLOR_KEY = "ambient_color";

  private static final float AMBIENT_LIGHT_INTENSITY = 0.3f;
  private static final float DEFAULT_AMBIENT_RED = 0.5f;
  private static final float DEFAULT_AMBIENT_GREEN = 0.5f;
  private static final float DEFAULT_AMBIENT_BLUE = 0.5f;
  private static final float OPAQUE_ALPHA = 1f;
  private static final float COLOR_BOOST_MULTIPLIER = 1.5f;
  private static final float DISTANCE_SCALE_MULTIPLIER = 6f;
  private static final int MIN_RAY_COUNT = 1;
  private static final Color AMBIENT_LIGHT_COLOR =
      new Color(DEFAULT_AMBIENT_RED, DEFAULT_AMBIENT_GREEN, DEFAULT_AMBIENT_BLUE, OPAQUE_ALPHA);

  private LightMapLoader() {}

  public static void createLights(TiledMap map, Engine engine) {
    MapLayer layer = map.getLayers().get(LIGHT_LAYER);
    if (layer == null) {
      return;
    }

    for (MapObject object : layer.getObjects()) {
      if (object instanceof RectangleMapObject) {
        createLight((RectangleMapObject) object, engine, map);
      }
    }
  }

  public static float readAmbientIntensity(TiledMap map) {
    Float ambient = map.getProperties().get(AMBIENT_KEY, Float.class);
    if (ambient != null) {
      return ambient;
    }
    return AMBIENT_LIGHT_INTENSITY;
  }

  public static Color readAmbientColor(TiledMap map) {
    String colorHex = map.getProperties().get(AMBIENT_COLOR_KEY, String.class);
    if (colorHex != null) {
      return Color.valueOf(colorHex);
    }
    return new Color(AMBIENT_LIGHT_COLOR);
  }

  private static void createLight(RectangleMapObject object, Engine engine, TiledMap map) {
    Entity entity = engine.createEntity();
    LightComponent light = engine.createComponent(LightComponent.class);
    PositionComponent position = engine.createComponent(PositionComponent.class);

    fillLightFromProperties(object, light, position, map);

    entity.add(light);
    entity.add(position);
    addFlickerIfApplicable(object, entity, engine);
    engine.addEntity(entity);
  }

  private static void fillLightFromProperties(
      RectangleMapObject object, LightComponent light, PositionComponent position, TiledMap map) {
    setType(object, light);
    setColor(object, light);
    setDistance(object, light);
    setCone(object, light);
    setXray(object, light);
    setRays(object, light);
    setSoftness(object, light);
    setStaticPosition(object, position, light, map);
  }

  private static void setStaticPosition(
      RectangleMapObject object, PositionComponent position, LightComponent light, TiledMap map) {
    Vector2 worldCenter = TiledCoordinates.getFlippedTiledCenter(object.getRectangle(), map);
    position.x = worldCenter.x;
    position.y = worldCenter.y;

    light.attachToPhysicsBody = false;
    light.useCustomPosition = true;
    light.positionMeters.set(
        worldCenter.x * PhysicsConstants.METERS_PER_PIXEL,
        worldCenter.y * PhysicsConstants.METERS_PER_PIXEL);

    if (light.type == LightComponent.LightType.CHAIN) {
      light.chainVertices = buildChainVertices(object.getRectangle(), map);
    }
  }

  private static void setType(RectangleMapObject object, LightComponent light) {
    String type = object.getProperties().get(TYPE_KEY, String.class);
    if (CONE_TYPE.equalsIgnoreCase(type)) {
      light.type = LightComponent.LightType.CONE;
      return;
    }
    if (CHAIN_TYPE.equalsIgnoreCase(type)) {
      light.type = LightComponent.LightType.CHAIN;
      return;
    }

    light.type = LightComponent.LightType.POINT;
  }

  private static void setColor(RectangleMapObject object, LightComponent light) {
    String colorHex = object.getProperties().get(COLOR_KEY, String.class);
    if (colorHex == null) {
      return;
    }

    light.color.set(Color.valueOf(colorHex));
    light.color.a = Math.max(OPAQUE_ALPHA, light.color.a);
    light.color.r = boostColorChannel(light.color.r);
    light.color.g = boostColorChannel(light.color.g);
    light.color.b = boostColorChannel(light.color.b);
  }

  private static void setDistance(RectangleMapObject object, LightComponent light) {
    Float distance = object.getProperties().get(DISTANCE_KEY, Float.class);
    if (distance != null) {
      light.distance = distance;
      return;
    }

    Rectangle rectangle = object.getRectangle();
    float maxDimension = Math.max(rectangle.width, rectangle.height);
    light.distance =
        Math.max(
            LightComponent.DEFAULT_DISTANCE,
            maxDimension * PhysicsConstants.METERS_PER_PIXEL * DISTANCE_SCALE_MULTIPLIER);
  }

  private static void setCone(RectangleMapObject object, LightComponent light) {
    Float cone = object.getProperties().get(CONE_DEGREE_KEY, Float.class);
    if (cone != null) {
      light.coneDegree = cone;
    }

    Float direction = object.getProperties().get(DIRECTION_KEY, Float.class);
    if (direction != null) {
      light.coneDirectionDegrees = direction;
      light.alignToBodyAngle = false;
    }
  }

  private static void setXray(RectangleMapObject object, LightComponent light) {
    Boolean xray = object.getProperties().get(XRAY_KEY, Boolean.class);
    if (xray != null) {
      light.xray = xray;
    }
  }

  private static void setRays(RectangleMapObject object, LightComponent light) {
    Integer rays = object.getProperties().get(RAYS_KEY, Integer.class);
    if (rays != null && rays >= MIN_RAY_COUNT) {
      light.rays = rays;
    }
  }

  private static void setSoftness(RectangleMapObject object, LightComponent light) {
    Float softness = object.getProperties().get(SOFTNESS_KEY, Float.class);
    if (softness != null) {
      light.softnessLength = softness;
    }
  }

  private static void addFlickerIfApplicable(
      RectangleMapObject object, Entity entity, Engine engine) {
    Boolean flickerFlag = object.getProperties().get(FLICKER_KEY, Boolean.class);
    if (flickerFlag == null || !flickerFlag) {
      return;
    }

    FlickerLightComponent flicker = engine.createComponent(FlickerLightComponent.class);

    Float flickerAmount = object.getProperties().get(FLICKER_AMOUNT_KEY, Float.class);
    if (flickerAmount != null) {
      flicker.amount = flickerAmount;
    }

    Float flickerSpeed = object.getProperties().get(FLICKER_SPEED_KEY, Float.class);
    if (flickerSpeed != null) {
      flicker.speed = flickerSpeed;
    }

    entity.add(flicker);
  }

  private static float boostColorChannel(float channel) {
    return Math.min(OPAQUE_ALPHA, channel * COLOR_BOOST_MULTIPLIER);
  }

  private static float[] buildChainVertices(Rectangle rect, TiledMap map) {
    Vector2 firstPoint = TiledCoordinates.toIsometric(rect.x, rect.y, map);
    Vector2 secondPoint = TiledCoordinates.toIsometric(rect.x + rect.width, rect.y, map);
    Vector2 thirdPoint =
        TiledCoordinates.toIsometric(rect.x + rect.width, rect.y + rect.height, map);
    Vector2 fourthPoint = TiledCoordinates.toIsometric(rect.x, rect.y + rect.height, map);
    float metersPerPixel = PhysicsConstants.METERS_PER_PIXEL;

    return new float[] {
      firstPoint.x * metersPerPixel, firstPoint.y * metersPerPixel,
      secondPoint.x * metersPerPixel, secondPoint.y * metersPerPixel,
      thirdPoint.x * metersPerPixel, thirdPoint.y * metersPerPixel,
      fourthPoint.x * metersPerPixel, fourthPoint.y * metersPerPixel,
      firstPoint.x * metersPerPixel, firstPoint.y * metersPerPixel
    };
  }
}
