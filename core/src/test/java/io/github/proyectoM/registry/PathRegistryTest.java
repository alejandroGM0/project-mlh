package io.github.proyectoM.registry;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.registry.PathRegistry;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for PathRegistry.
 */
class PathRegistryTest {

    /**
     * Verifies that has path collision detects nearby waypoints from other entities.
     */
    @Test
    void hasPathCollisionDetectsNearbyWaypointsFromOtherEntities() {
        PathRegistry registry = new PathRegistry();
        Entity existingEntity = new Entity();
        Entity proposingEntity = new Entity();
        registry.registerPath(existingEntity, this.createPath(new Vector2(0.0f, 0.0f)));
        boolean hasCollision = registry.hasPathCollision(proposingEntity, this.createPath(new Vector2(10.0f, 10.0f)));
        Assertions.assertTrue(hasCollision);
    }

    /**
     * Verifies that has path collision ignores the entity that owns the registered path.
     */
    @Test
    void hasPathCollisionIgnoresTheEntityThatOwnsTheRegisteredPath() {
        PathRegistry registry = new PathRegistry();
        Entity entity = new Entity();
        Queue<Vector2> path = this.createPath(new Vector2(0.0f, 0.0f));
        registry.registerPath(entity, path);
        boolean hasCollision = registry.hasPathCollision(entity, this.createPath(new Vector2(0.0f, 0.0f)));
        Assertions.assertFalse(hasCollision);
    }

    /**
     * Verifies that register path stores a defensive copy of the waypoints.
     */
    @Test
    void registerPathStoresADefensiveCopyOfTheWaypoints() {
        PathRegistry registry = new PathRegistry();
        Entity existingEntity = new Entity();
        Entity proposingEntity = new Entity();
        Queue<Vector2> originalPath = this.createPath(new Vector2(0.0f, 0.0f));
        registry.registerPath(existingEntity, originalPath);
        originalPath.peek().set(500.0f, 500.0f);
        boolean hasCollision = registry.hasPathCollision(proposingEntity, this.createPath(new Vector2(10.0f, 10.0f)));
        Assertions.assertTrue(hasCollision);
    }

    /**
     * Verifies that update path replaces the registered path.
     */
    @Test
    void updatePathReplacesTheRegisteredPath() {
        PathRegistry registry = new PathRegistry();
        Entity existingEntity = new Entity();
        Entity proposingEntity = new Entity();
        registry.registerPath(existingEntity, this.createPath(new Vector2(0.0f, 0.0f)));
        registry.updatePath(existingEntity, this.createPath(new Vector2(500.0f, 500.0f)));
        boolean hasCollision = registry.hasPathCollision(proposingEntity, this.createPath(new Vector2(10.0f, 10.0f)));
        Assertions.assertFalse(hasCollision);
    }

    /**
     * Verifies that clear removes every registered path.
     */
    @Test
    void clearRemovesEveryRegisteredPath() {
        PathRegistry registry = new PathRegistry();
        registry.registerPath(new Entity(), this.createPath(new Vector2(0.0f, 0.0f)));
        registry.registerPath(new Entity(), this.createPath(new Vector2(100.0f, 100.0f)));
        registry.clear();
        Assertions.assertEquals(0, registry.getActivePathCount());
    }

    private Queue<Vector2> createPath(Vector2 ... waypoints) {
        LinkedList<Vector2> path = new LinkedList<Vector2>();
        for (Vector2 waypoint : waypoints) {
            path.offer(waypoint);
        }
        return path;
    }
}