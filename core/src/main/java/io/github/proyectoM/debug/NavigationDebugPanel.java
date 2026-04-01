package io.github.proyectoM.debug;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.movement.PathfindingComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.pathfinding.NavigationGrid;
import java.util.ArrayList;
import java.util.List;

/** Debug panel for the navigation and pathfinding system visualization and statistics. */
public class NavigationDebugPanel implements DebugPanel {
  private static final String TITLE = "Navigation";
  private static final String NAVIGATION_SYSTEM_LABEL = "Navigation System";
  private static final String VISUALIZATION_LABEL = "Visualization:";
  private static final String GRID_INFO_LABEL = "Grid Info:";
  private static final String STATISTICS_LABEL = "Statistics:";
  private static final String SHOW_NAVIGATION_GRID_TEXT = " Show Navigation Grid";
  private static final String SHOW_PATHS_TEXT = " Show Paths";
  private static final String SHOW_STATIC_NAVMESH_TEXT = " Show Static NavMesh (Zombies)";
  private static final String SHOW_ENEMY_PATHS_TEXT = " Show Enemy Paths";
  private static final String SHOW_COMPANION_PATHS_TEXT = " Show Companion Paths";
  private static final String EMPTY_STRING = "";
  private static final String NO_NAVIGATION_GRID_AVAILABLE = "No navigation grid available";
  private static final String NO_STATISTICS_AVAILABLE = "No statistics available";
  private static final String GRID_INFO_FORMAT =
      "Size: %dx%d\nCell Size: %.0f px\nTotal Cells: %d\nOrigin: (%.1f, %.1f)";
  private static final String STATS_FORMAT = "Walkable: %d\nBlocked: %d\nWalkable %%: %.1f%%";

  private final Engine engine;
  private final OrthographicCamera camera;
  private final NavigationGrid navigationGrid;
  private final NavigationGridDebugRenderer gridRenderer;

  private CheckBox enableGridCheckbox;
  private CheckBox showPathsCheckbox;
  private CheckBox showStaticNavMeshCheckbox;
  private CheckBox showEnemyPathsCheckbox;
  private CheckBox showCompanionPathsCheckbox;
  private Label gridInfoLabel;
  private Label statsLabel;
  private boolean active = false;

  public NavigationDebugPanel(
      Engine engine, OrthographicCamera camera, NavigationGrid navigationGrid) {
    this.engine = engine;
    this.camera = camera;
    this.navigationGrid = navigationGrid;
    this.gridRenderer = new NavigationGridDebugRenderer(navigationGrid);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public Actor buildPanel(Skin skin) {
    Table mainTable = new Table(skin);
    mainTable.top().left();
    mainTable.defaults().pad(3).expandX().fillX();

    mainTable.add(new Label(NAVIGATION_SYSTEM_LABEL, skin)).left().colspan(2).padBottom(10);
    mainTable.row();

    mainTable.add(new Label(EMPTY_STRING, skin)).height(5).colspan(2);
    mainTable.row();

    createVisualizationControls(mainTable, skin);

    createGridInfo(mainTable, skin);

    createStatsSection(mainTable, skin);

    ScrollPane scrollPane = new ScrollPane(mainTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollbarsOnTop(false);
    scrollPane.setScrollingDisabled(true, false);
    scrollPane.setForceScroll(false, true);
    scrollPane.setSmoothScrolling(true);
    scrollPane.setOverscroll(false, false);

    Table containerTable = new Table(skin);
    containerTable.add(scrollPane).expand().fill();

    return containerTable;
  }

  /** Creates the controls to enable/disable visualization. */
  private void createVisualizationControls(Table table, Skin skin) {
    table.add(new Label(VISUALIZATION_LABEL, skin)).left().colspan(2);
    table.row();

    enableGridCheckbox = new CheckBox(SHOW_NAVIGATION_GRID_TEXT, skin);
    enableGridCheckbox.setChecked(false);
    enableGridCheckbox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            gridRenderer.setEnabled(enableGridCheckbox.isChecked());
            gridRenderer.setShowGrid(enableGridCheckbox.isChecked());
          }
        });
    table.add(enableGridCheckbox).left().colspan(2);
    table.row();

    showPathsCheckbox = new CheckBox(SHOW_PATHS_TEXT, skin);
    showPathsCheckbox.setChecked(true);
    showPathsCheckbox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            gridRenderer.setShowPaths(showPathsCheckbox.isChecked());
          }
        });
    table.add(showPathsCheckbox).left().colspan(2);
    table.row();

    showStaticNavMeshCheckbox = new CheckBox(SHOW_STATIC_NAVMESH_TEXT, skin);
    showStaticNavMeshCheckbox.setChecked(false);
    showStaticNavMeshCheckbox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            gridRenderer.setShowStaticNavMesh(showStaticNavMeshCheckbox.isChecked());
          }
        });
    table.add(showStaticNavMeshCheckbox).left().colspan(2);
    table.row();

    showEnemyPathsCheckbox = new CheckBox(SHOW_ENEMY_PATHS_TEXT, skin);
    showEnemyPathsCheckbox.setChecked(true);
    table.add(showEnemyPathsCheckbox).left().colspan(2);
    table.row();

    showCompanionPathsCheckbox = new CheckBox(SHOW_COMPANION_PATHS_TEXT, skin);
    showCompanionPathsCheckbox.setChecked(true);
    table.add(showCompanionPathsCheckbox).left().colspan(2);
    table.row();

    table.add(new Label(EMPTY_STRING, skin)).height(10);
    table.row();
  }

  /** Creates the grid information section. */
  private void createGridInfo(Table table, Skin skin) {
    table.add(new Label(GRID_INFO_LABEL, skin)).left().colspan(2);
    table.row();

    gridInfoLabel = new Label(EMPTY_STRING, skin);
    table.add(gridInfoLabel).left().colspan(2);
    table.row();

    table.add(new Label(EMPTY_STRING, skin)).height(10);
    table.row();
  }

  /** Creates the statistics section. */
  private void createStatsSection(Table table, Skin skin) {
    table.add(new Label(STATISTICS_LABEL, skin)).left().colspan(2);
    table.row();

    statsLabel = new Label(EMPTY_STRING, skin);
    table.add(statsLabel).left().colspan(2);
    table.row();
  }

  @Override
  public void update(float delta) {
    updateGridInfo();
    updateStats();

    if (gridRenderer.isEnabled()) {
      gridRenderer.render(camera);

      if (showPathsCheckbox.isChecked()) {
        renderActivePaths();
      }
    }
  }

  /** Renders all active paths of entities with pathfinding. */
  private void renderActivePaths() {
    Family pathfindingFamily =
        Family.all(PathfindingComponent.class, PositionComponent.class).get();

    ImmutableArray<Entity> entities = engine.getEntitiesFor(pathfindingFamily);

    ComponentMapper<PathfindingComponent> pathfindingMapper =
        ComponentMapper.getFor(PathfindingComponent.class);
    ComponentMapper<PositionComponent> positionMapper =
        ComponentMapper.getFor(PositionComponent.class);
    ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
    ComponentMapper<CompanionComponent> companionMapper =
        ComponentMapper.getFor(CompanionComponent.class);

    for (Entity entity : entities) {
      boolean isEnemy = enemyMapper.has(entity);
      boolean isCompanion = companionMapper.has(entity);

      if (isEnemy && !showEnemyPathsCheckbox.isChecked()) {
        continue;
      }

      if (isCompanion && !showCompanionPathsCheckbox.isChecked()) {
        continue;
      }

      PathfindingComponent pathfinding = pathfindingMapper.get(entity);
      PositionComponent position = positionMapper.get(entity);

      if (pathfinding != null
          && position != null
          && pathfinding.hasValidPath
          && !pathfinding.currentPath.isEmpty()) {
        List<Vector2> pathList = new ArrayList<>();

        pathList.add(new Vector2(position.x, position.y));

        for (Vector2 waypoint : pathfinding.currentPath) {
          pathList.add(new Vector2(waypoint));
        }

        gridRenderer.renderPath(pathList, isEnemy);
      }
    }
  }

  /** Updates the grid information. */
  private void updateGridInfo() {
    if (navigationGrid != null) {
      String info =
          String.format(
              GRID_INFO_FORMAT,
              navigationGrid.getGridWidth(),
              navigationGrid.getGridHeight(),
              navigationGrid.getCellSize(),
              navigationGrid.getGridWidth() * navigationGrid.getGridHeight(),
              navigationGrid.getOriginX(),
              navigationGrid.getOriginY());
      gridInfoLabel.setText(info);
    } else {
      gridInfoLabel.setText(NO_NAVIGATION_GRID_AVAILABLE);
    }
  }

  /** Updates the pathfinding statistics. */
  private void updateStats() {
    if (navigationGrid != null) {
      int walkableCells = 0;
      int blockedCells = 0;

      for (int x = 0; x < navigationGrid.getGridWidth(); x++) {
        for (int y = 0; y < navigationGrid.getGridHeight(); y++) {
          if (navigationGrid.isWalkable(x, y)) {
            walkableCells++;
          } else {
            blockedCells++;
          }
        }
      }

      String stats =
          String.format(
              STATS_FORMAT,
              walkableCells,
              blockedCells,
              (walkableCells / (float) (walkableCells + blockedCells)) * 100);
      statsLabel.setText(stats);
    } else {
      statsLabel.setText(NO_STATISTICS_AVAILABLE);
    }
  }

  /** Gets the grid renderer for external use. */
  public NavigationGridDebugRenderer getGridRenderer() {
    return gridRenderer;
  }

  /** Releases the resources used. */
  public void dispose() {
    if (gridRenderer != null) {
      gridRenderer.dispose();
    }
  }
}
