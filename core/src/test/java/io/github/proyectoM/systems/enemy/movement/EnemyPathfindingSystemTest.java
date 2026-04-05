package io.github.proyectoM.systems.enemy.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.movement.PathfindingComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.pathfinding.NavigationGrid;
import io.github.proyectoM.systems.enemy.movement.EnemyPathfindingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for EnemyPathfindingSystem.
 */
class EnemyPathfindingSystemTest {
    private static final int GRID_SIZE = 8;
    private static final float CELL_SIZE = 32.0f;
    private static final float ORIGIN = 0.0f;

    /**
     * Verifies that constructor rejects null navigation grid.
     */
    @Test
    void constructorRejectsNullNavigationGrid() {
        Assertions.assertThrows(NullPointerException.class, () -> new EnemyPathfindingSystem(null));
    }

    /**
     * Verifies that update calculates path across open grid.
     */
    @Test
    void updateCalculatesPathAcrossOpenGrid() {
        NavigationGrid navigationGrid = new NavigationGrid(8, 8, 32.0f, 0.0f, 0.0f);
        EnemyPathfindingSystem system = new EnemyPathfindingSystem(navigationGrid);
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)system);
        PathfindingComponent pathfinding = new PathfindingComponent();
        pathfinding.setTarget(160.0f, 160.0f);
        Entity enemy = new Entity();
        enemy.add((Component)new EnemyComponent());
        enemy.add((Component)pathfinding);
        enemy.add((Component)new PositionComponent(32.0f, 32.0f));
        engine.addEntity(enemy);
        engine.update(1.0f);
        Assertions.assertTrue(pathfinding.hasValidPath);
        Assertions.assertTrue((pathfinding.getRemainingWaypoints() > 0 ? 1 : 0) != 0);
        Assertions.assertFalse(pathfinding.currentWaypoint.isZero());
    }

    /**
     * Verifies that update clears path when target falls outside grid.
     */
    @Test
    void updateClearsPathWhenTargetFallsOutsideGrid() {
        NavigationGrid navigationGrid = new NavigationGrid(8, 8, 32.0f, 0.0f, 0.0f);
        EnemyPathfindingSystem system = new EnemyPathfindingSystem(navigationGrid);
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)system);
        PathfindingComponent pathfinding = new PathfindingComponent();
        pathfinding.setTarget(160.0f, 160.0f);
        Entity enemy = new Entity();
        enemy.add((Component)new EnemyComponent());
        enemy.add((Component)pathfinding);
        enemy.add((Component)new PositionComponent(32.0f, 32.0f));
        engine.addEntity(enemy);
        engine.update(1.0f);
        pathfinding.setTarget(1000.0f, 1000.0f);
        engine.update(1.0f);
        Assertions.assertFalse(pathfinding.hasValidPath);
        Assertions.assertTrue(pathfinding.currentPath.isEmpty());
    }
}