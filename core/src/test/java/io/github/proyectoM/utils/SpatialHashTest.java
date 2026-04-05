package io.github.proyectoM.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.ashley.core.Entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpatialHashTest {

  private static final float CELL_SIZE = 100f;

  private SpatialHash spatialHash;

  /**
   * Initializes a clean SpatialHash before each test.
   */
  @BeforeEach
  void setUp() {
    spatialHash = new SpatialHash(CELL_SIZE);
  }

  /**
   * Verifies that the constructor rejects a zero cell size.
   */
  @Test
  void constructor_zeroCellSize_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new SpatialHash(0f));
  }

  /**
   * Verifies that the constructor rejects a negative cell size.
   */
  @Test
  void constructor_negativeCellSize_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new SpatialHash(-1f));
  }

  /**
   * Verifies that getCellSize returns the configured value.
   */
  @Test
  void getCellSize_returnsConfiguredSize() {
    assertEquals(CELL_SIZE, spatialHash.getCellSize());
  }

  /**
   * Verifies that a newly created SpatialHash has no occupied cells.
   */
  @Test
  void newSpatialHash_hasZeroOccupiedCells() {
    assertEquals(0, spatialHash.getOccupiedCellCount());
  }

  /**
   * Verifies that inserting an entity increments the occupied cell count.
   */
  @Test
  void insert_singleEntity_incrementsOccupiedCells() {
    Entity entity = new Entity();
    spatialHash.insert(entity, 50f, 50f);

    assertEquals(1, spatialHash.getOccupiedCellCount());
  }

  /**
   * Verifies that inserting entities in the same cell does not increment the count.
   */
  @Test
  void insert_entitiesInSameCell_singleOccupiedCell() {
    spatialHash.insert(new Entity(), 10f, 10f);
    spatialHash.insert(new Entity(), 20f, 20f);

    assertEquals(1, spatialHash.getOccupiedCellCount());
  }

  /**
   * Verifies that inserting entities in different cells increments the count.
   */
  @Test
  void insert_entitiesInDifferentCells_multipleOccupiedCells() {
    spatialHash.insert(new Entity(), 10f, 10f);
    spatialHash.insert(new Entity(), 200f, 200f);

    assertEquals(2, spatialHash.getOccupiedCellCount());
  }

  /**
   * Verifies that queryRadius finds an inserted entity within the radius.
   */
  @Test
  void queryRadius_entityInRange_findsEntity() {
    Entity entity = new Entity();
    spatialHash.insert(entity, 50f, 50f);

    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(50f, 50f, 10f, result);

    assertEquals(1, result.size());
    assertTrue(result.contains(entity));
  }

  /**
   * Verifies that queryRadius returns an empty list when there are no entities.
   */
  @Test
  void queryRadius_emptyGrid_returnsEmpty() {
    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(50f, 50f, 100f, result);

    assertTrue(result.isEmpty());
  }

  /**
   * Verifies that queryRadius finds multiple entities within the radius.
   */
  @Test
  void queryRadius_multipleEntitiesInRange_findsAll() {
    Entity e1 = new Entity();
    Entity e2 = new Entity();
    Entity e3 = new Entity();

    spatialHash.insert(e1, 10f, 10f);
    spatialHash.insert(e2, 20f, 20f);
    spatialHash.insert(e3, 30f, 30f);

    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(20f, 20f, 50f, result);

    assertEquals(3, result.size());
  }

  /**
   * Verifies that clear() removes all entities and resets the count.
   */
  @Test
  void clear_afterInsertions_resetsToEmpty() {
    spatialHash.insert(new Entity(), 10f, 10f);
    spatialHash.insert(new Entity(), 200f, 200f);

    spatialHash.clear();

    assertEquals(0, spatialHash.getOccupiedCellCount());

    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(10f, 10f, 500f, result);
    assertTrue(result.isEmpty());
  }

  /**
   * Verifies that queryRadius does not find entities in distant cells outside the radius.
   */
  @Test
  void queryRadius_entityFarAway_notFound() {
    Entity nearby = new Entity();
    Entity farAway = new Entity();

    spatialHash.insert(nearby, 10f, 10f);
    spatialHash.insert(farAway, 5000f, 5000f);

    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(10f, 10f, 50f, result);

    assertEquals(1, result.size());
    assertTrue(result.contains(nearby));
  }

  /**
   * Verifies that queryRadius clears the result list before populating it.
   */
  @Test
  void queryRadius_clearsResultListBeforePopulating() {
    Entity entity = new Entity();
    spatialHash.insert(entity, 10f, 10f);

    List<Entity> result = new ArrayList<>();
    result.add(new Entity());
    result.add(new Entity());

    spatialHash.queryRadius(10f, 10f, 50f, result);

    assertEquals(1, result.size());
  }

  /**
   * Verifies that inserting at negative coordinates works correctly.
   */
  @Test
  void insert_negativeCoordinates_worksCorrectly() {
    Entity entity = new Entity();
    spatialHash.insert(entity, -50f, -50f);

    List<Entity> result = new ArrayList<>();
    spatialHash.queryRadius(-50f, -50f, 10f, result);

    assertEquals(1, result.size());
  }
}