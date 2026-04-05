package io.github.proyectoM.systems.core;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.OrthographicCamera;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.systems.core.CameraSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for CameraSystem.
 */
class CameraSystemTest {

    /**
     * Verifies that process entity smoothly moves camera towards the group controller.
     */
    @Test
    void processEntitySmoothlyMovesCameraTowardsTheGroupController() {
        TestOrthographicCamera camera = new TestOrthographicCamera();
        camera.position.set(0.0f, 0.0f, 0.0f);
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new CameraSystem((OrthographicCamera)camera));
        Entity controller = new Entity();
        controller.add((Component)new GroupControllerComponent());
        controller.add((Component)new PositionComponent(10.0f, 20.0f));
        engine.addEntity(controller);
        engine.update(0.1f);
        Assertions.assertEquals(8.0f, camera.position.x, 1.0E-4f);
        Assertions.assertEquals(16.0f, camera.position.y, 1.0E-4f);
    }

    /**
     * Verifies that reset camera snaps to the controller position.
     */
    @Test
    void resetCameraSnapsToTheControllerPosition() {
        TestOrthographicCamera camera = new TestOrthographicCamera();
        camera.position.set(-5.0f, -7.0f, 3.0f);
        CameraSystem cameraSystem = new CameraSystem((OrthographicCamera)camera);
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)cameraSystem);
        Entity controller = new Entity();
        controller.add((Component)new GroupControllerComponent());
        controller.add((Component)new PositionComponent(25.0f, 40.0f));
        engine.addEntity(controller);
        engine.update(0.0f);
        cameraSystem.resetCamera();
        Assertions.assertEquals(25.0f, camera.position.x, 1.0E-4f);
        Assertions.assertEquals(40.0f, camera.position.y, 1.0E-4f);
        Assertions.assertEquals(0.0f, camera.position.z, 1.0E-4f);
    }

    /**
     * Verifies that get camera returns the managed camera instance.
     */
    @Test
    void getCameraReturnsTheManagedCameraInstance() {
        TestOrthographicCamera camera = new TestOrthographicCamera();
        CameraSystem cameraSystem = new CameraSystem((OrthographicCamera)camera);
        Assertions.assertSame((camera), cameraSystem.getCamera());
    }

    /**
     * Verifies that constructor rejects null camera.
     */
    @Test
    void constructorRejectsNullCamera() {
        Assertions.assertThrows(NullPointerException.class, () -> new CameraSystem(null));
    }

    private static final class TestOrthographicCamera
    extends OrthographicCamera {
        private TestOrthographicCamera() {
        }

        public void update() {
        }

        public void update(boolean updateFrustum) {
        }
    }
}