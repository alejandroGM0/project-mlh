package io.github.proyectoM.world;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.FlickerLightComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.world.LightMapLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for LightMapLoader.
 */
class LightMapLoaderTest {
    private static final float EPSILON = 1.0E-4f;
    private static final String LIGHT_LAYER = "lights";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private static final String TILE_WIDTH_KEY = "tilewidth";
    private static final String TILE_HEIGHT_KEY = "tileheight";
    private static final String AMBIENT_KEY = "ambient";
    private static final String AMBIENT_COLOR_KEY = "ambient_color";
    private static final String TYPE_KEY = "type";
    private static final String COLOR_KEY = "color";
    private static final String DISTANCE_KEY = "distance";
    private static final String FLICKER_KEY = "flicker";
    private static final String FLICKER_AMOUNT_KEY = "flickerAmount";
    private static final String FLICKER_SPEED_KEY = "flickerSpeed";
    private static final String CHAIN_TYPE = "chain";

    /**
     * Verifies that read ambient intensity returns default when property is missing.
     */
    @Test
    void readAmbientIntensityReturnsDefaultWhenPropertyIsMissing() {
        Assertions.assertEquals(0.3f, LightMapLoader.readAmbientIntensity((TiledMap)LightMapLoaderTest.createMap()), 1.0E-4f);
    }

    /**
     * Verifies that read ambient color returns independent default color copies.
     */
    @Test
    void readAmbientColorReturnsIndependentDefaultColorCopies() {
        TiledMap map = LightMapLoaderTest.createMap();
        Color first = LightMapLoader.readAmbientColor((TiledMap)map);
        Color second = LightMapLoader.readAmbientColor((TiledMap)map);
        Assertions.assertNotSame(first, second);
        Assertions.assertEquals(0.5f, first.r, 1.0E-4f);
        Assertions.assertEquals(0.5f, first.g, 1.0E-4f);
        Assertions.assertEquals(0.5f, first.b, 1.0E-4f);
        Assertions.assertEquals(1.0f, first.a, 1.0E-4f);
    }

    /**
     * Verifies that create lights builds light position and optional flicker component.
     */
    @Test
    void createLightsBuildsLightPositionAndOptionalFlickerComponent() {
        TiledMap map = LightMapLoaderTest.createMap();
        RectangleMapObject lightObject = new RectangleMapObject(32.0f, 64.0f, 32.0f, 32.0f);
        lightObject.getProperties().put(TYPE_KEY, CHAIN_TYPE);
        lightObject.getProperties().put(COLOR_KEY, "808080");
        lightObject.getProperties().put(DISTANCE_KEY, Float.valueOf(20.0f));
        lightObject.getProperties().put(FLICKER_KEY, true);
        lightObject.getProperties().put(FLICKER_AMOUNT_KEY, Float.valueOf(0.25f));
        lightObject.getProperties().put(FLICKER_SPEED_KEY, Float.valueOf(8.0f));
        map.getLayers().get(LIGHT_LAYER).getObjects().add((MapObject)lightObject);
        PooledEngine engine = new PooledEngine();
        LightMapLoader.createLights((TiledMap)map, (Engine)engine);
        ImmutableArray entities = engine.getEntitiesFor(Family.all((Class[])new Class[]{LightComponent.class, PositionComponent.class}).get());
        Assertions.assertEquals(1, entities.size());
        Entity lightEntity = (Entity)entities.first();
        LightComponent light = (LightComponent)lightEntity.getComponent(LightComponent.class);
        PositionComponent position = (PositionComponent)lightEntity.getComponent(PositionComponent.class);
        FlickerLightComponent flicker = (FlickerLightComponent)lightEntity.getComponent(FlickerLightComponent.class);
        Assertions.assertSame(LightComponent.LightType.CHAIN, light.type);
        Assertions.assertEquals(20.0f, light.distance, 1.0E-4f);
        Assertions.assertEquals(1.0f, light.color.a, 1.0E-4f);
        Assertions.assertNotNull(light.chainVertices);
        Assertions.assertEquals(10, light.chainVertices.length);
        Assertions.assertEquals(48.0f, position.x, 1.0E-4f);
        Assertions.assertEquals(240.0f, position.y, 1.0E-4f);
        Assertions.assertEquals(1.5f, light.positionMeters.x, 1.0E-4f);
        Assertions.assertEquals(7.5f, light.positionMeters.y, 1.0E-4f);
        Assertions.assertNotNull(flicker);
        Assertions.assertEquals(0.25f, flicker.amount, 1.0E-4f);
        Assertions.assertEquals(8.0f, flicker.speed, 1.0E-4f);
    }

    /**
     * Verifies that read ambient values use map properties when present.
     */
    @Test
    void readAmbientValuesUseMapPropertiesWhenPresent() {
        TiledMap map = LightMapLoaderTest.createMap();
        map.getProperties().put(AMBIENT_KEY, Float.valueOf(0.6f));
        map.getProperties().put(AMBIENT_COLOR_KEY, "336699");
        Color color = LightMapLoader.readAmbientColor((TiledMap)map);
        Assertions.assertEquals(0.6f, LightMapLoader.readAmbientIntensity((TiledMap)map), 1.0E-4f);
        Assertions.assertEquals(Color.valueOf((String)"336699"), color);
    }

    private static TiledMap createMap() {
        TiledMap map = new TiledMap();
        map.getProperties().put(WIDTH_KEY, 10);
        map.getProperties().put(HEIGHT_KEY, 10);
        map.getProperties().put(TILE_WIDTH_KEY, 64);
        map.getProperties().put(TILE_HEIGHT_KEY, 32);
        MapLayer lightLayer = new MapLayer();
        lightLayer.setName(LIGHT_LAYER);
        map.getLayers().add(lightLayer);
        return map;
    }
}