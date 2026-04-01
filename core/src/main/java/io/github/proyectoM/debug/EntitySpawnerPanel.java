package io.github.proyectoM.debug;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.AIComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.factories.CompanionFactory;
import io.github.proyectoM.factories.EnemyFactory;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.EnemyRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Unified entity spawner debug panel. Provides bulk spawning, positioned spawning, entity counting,
 * and companion health editing.
 */
public class EntitySpawnerPanel implements DebugPanel {

  private static final String TITLE = "Entity Spawner";

  private static final String SECTION_BULK = "Bulk Spawn";
  private static final String SECTION_POSITIONED = "Positioned Spawn";
  private static final String SECTION_HEALTH = "Companion HP";
  private static final String ENEMIES_HEADER = "Enemies";
  private static final String COMPANIONS_HEADER = "Companions";

  private static final float CELL_PAD = 4f;
  private static final float SECTION_PAD = 8f;
  private static final float LABEL_WIDTH = 100f;
  private static final float BUTTON_HEIGHT = 30f;
  private static final int BULK_BUTTON_WIDTH = 55;
  private static final int BULK_BUTTON_HEIGHT = 25;

  private static final float SPAWN_RADIUS_MIN = 5f;
  private static final float SPAWN_RADIUS_MAX = 20f;
  private static final float COMPANION_RANDOM_BASE_PIXELS = 200f;
  private static final float COMPANION_RANDOM_EXTRA_PIXELS = 100f;
  private static final float ZOMBIE_RANDOM_BASE_PIXELS = 300f;
  private static final float ZOMBIE_RANDOM_EXTRA_PIXELS = 200f;

  private static final String RANDOM_OPTION = "Random";
  private static final String ZERO_TEXT = "0";
  private static final String DEFAULT_MAX_HP = "100";
  private static final String SELECTION_SEPARATOR = ":";
  private static final int SELECTION_SPLIT_LIMIT = 2;
  private static final String COMPANION_NAME_FORMAT = "companion-%d";
  private static final String STATUS_PREFIX = "Status: ";
  private static final String STATUS_READY = "Ready";
  private static final String STATUS_HP_UPDATED = "OK: HP updated";
  private static final String STATUS_COUNTS_FORMAT = "Companions: %d | Zombies: %d";
  private static final String STATUS_COMPANION_FORMAT = "OK: Companion %s at (%.1f, %.1f)";
  private static final String STATUS_ZOMBIE_FORMAT = "OK: Zombie %s at (%.1f, %.1f)";
  private static final String STATUS_RANDOM_COMPANION_FORMAT = "OK: Random companion %s";
  private static final String STATUS_RANDOM_ZOMBIE_FORMAT = "OK: Random zombie %s";
  private static final String ERROR_NO_TYPES = "Error: No types available";
  private static final String ERROR_INVALID_COORDS = "Error: Invalid coordinates";
  private static final String ERROR_INVALID_HP = "Error: Invalid HP values";
  private static final String ERROR_SELECT = "Error: Select an item";

  private static final Color SECTION_HEADER_COLOR = new Color(0.3f, 0.9f, 1f, 1f);
  private static final Color ENEMIES_COLOR = new Color(1f, 0.4f, 0.4f, 1f);
  private static final Color COMPANIONS_COLOR = new Color(0.4f, 1f, 0.4f, 1f);
  private static final Color INFO_COLOR = new Color(0.7f, 0.8f, 0.9f, 1f);

  private static final int UNINITIALIZED_COUNT = -1;

  private final Engine engine;
  private final CompanionFactory companionFactory;
  private final EnemyFactory enemyFactory;

  private boolean active;
  private String currentStatus = STATUS_READY;
  private int lastCompanionCount = UNINITIALIZED_COUNT;

  private SelectBox<String> bulkEnemySelector;
  private SelectBox<String> bulkCompanionSelector;
  private SelectBox<String> posEnemySelector;
  private SelectBox<String> posCompanionSelector;
  private TextField posXField;
  private TextField posYField;
  private SelectBox<String> hpCompanionSelector;
  private TextField currentHpField;
  private TextField maxHpField;
  private Label statusLabel;
  private Label countsLabel;

  /**
   * Creates the entity spawner panel.
   *
   * @param engine the ECS engine for entity queries
   * @param companionFactory factory for creating companion entities
   * @param enemyFactory factory for creating enemy entities
   * @param world the Box2D world (used by enemy factory)
   */
  public EntitySpawnerPanel(
      Engine engine, CompanionFactory companionFactory, EnemyFactory enemyFactory, World world) {
    this.engine = engine;
    this.companionFactory = companionFactory;
    this.enemyFactory = enemyFactory;
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

  /** Updates entity counts and refreshes the companion HP selector when entities change. */
  @Override
  public void update(float delta) {
    updateEntityCounts();

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(Family.all(CompanionComponent.class, HealthComponent.class).get());
    if (companions.size() != lastCompanionCount) {
      lastCompanionCount = companions.size();
      refreshHpCompanionList();
    }
  }

  /** Builds the panel UI with bulk spawn, positioned spawn, and health editing sections. */
  @Override
  public Actor buildPanel(Skin skin) {
    Table mainTable = new Table(skin);
    mainTable.top().left();
    mainTable.defaults().pad(CELL_PAD).expandX().fillX();

    addBulkSpawnSection(mainTable, skin);
    addPositionedSpawnSection(mainTable, skin);
    addHealthSection(mainTable, skin);
    addStatusSection(mainTable, skin);

    ScrollPane scrollPane = new ScrollPane(mainTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);

    Table container = new Table(skin);
    container.add(scrollPane).expand().fill();
    return container;
  }

  /** Adds the bulk spawning section with enemy and companion batch buttons. */
  private void addBulkSpawnSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_BULK);

    Label enemyHeader = new Label(ENEMIES_HEADER, skin);
    enemyHeader.setColor(ENEMIES_COLOR);
    table.add(enemyHeader).left().colspan(2).row();

    bulkEnemySelector = new SelectBox<>(skin);
    Array<String> enemyIds = new Array<>();
    enemyIds.add(RANDOM_OPTION);
    for (String id : EnemyRegistry.getAllZombieIds()) {
      enemyIds.add(id);
    }
    bulkEnemySelector.setItems(enemyIds);
    table.add(bulkEnemySelector).expandX().fillX().colspan(2).row();

    Table enemyButtons = new Table(skin);
    addBulkButton(enemyButtons, skin, "+10", 10, true);
    addBulkButton(enemyButtons, skin, "+50", 50, true);
    addBulkButton(enemyButtons, skin, "+100", 100, true);
    addBulkButton(enemyButtons, skin, "+500", 500, true);
    table.add(enemyButtons).left().colspan(2).row();

    table.add().height(CELL_PAD).colspan(2).row();

    Label compHeader = new Label(COMPANIONS_HEADER, skin);
    compHeader.setColor(COMPANIONS_COLOR);
    table.add(compHeader).left().colspan(2).row();

    bulkCompanionSelector = new SelectBox<>(skin);
    Array<String> compIds = new Array<>();
    for (String id : CompanionRegistry.getAllCompanionIds()) {
      compIds.add(id);
    }
    bulkCompanionSelector.setItems(compIds);
    table.add(bulkCompanionSelector).expandX().fillX().colspan(2).row();

    Table compButtons = new Table(skin);
    addBulkButton(compButtons, skin, "+1", 1, false);
    addBulkButton(compButtons, skin, "+5", 5, false);
    addBulkButton(compButtons, skin, "+10", 10, false);
    addBulkButton(compButtons, skin, "+50", 50, false);
    table.add(compButtons).left().colspan(2).row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the positioned spawning section with coordinate fields and spawn buttons. */
  private void addPositionedSpawnSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_POSITIONED);

    table.add(createInfoLabel(skin, "Enemy Type:")).left().width(LABEL_WIDTH);
    posEnemySelector = new SelectBox<>(skin);
    Set<String> zombieIds = EnemyRegistry.getAllZombieIds();
    posEnemySelector.setItems(zombieIds.toArray(new String[0]));
    table.add(posEnemySelector).expandX().fillX().row();

    table.add(createInfoLabel(skin, "Companion:")).left().width(LABEL_WIDTH);
    posCompanionSelector = new SelectBox<>(skin);
    posCompanionSelector.setItems(CompanionRegistry.getAllCompanionIds().toArray(new String[0]));
    table.add(posCompanionSelector).expandX().fillX().row();

    table.add(createInfoLabel(skin, "X:")).left().width(LABEL_WIDTH);
    posXField = new TextField(ZERO_TEXT, skin);
    table.add(posXField).expandX().fillX().row();

    table.add(createInfoLabel(skin, "Y:")).left().width(LABEL_WIDTH);
    posYField = new TextField(ZERO_TEXT, skin);
    table.add(posYField).expandX().fillX().row();

    Table spawnButtons = new Table(skin);
    addActionButton(spawnButtons, skin, "Add Enemy", this::addPositionedEnemy);
    addActionButton(spawnButtons, skin, "Add Companion", this::addPositionedCompanion);
    table.add(spawnButtons).colspan(2).expandX().fillX().row();

    Table randomButtons = new Table(skin);
    addActionButton(randomButtons, skin, "Random Enemy", this::addRandomEnemy);
    addActionButton(randomButtons, skin, "Random Companion", this::addRandomCompanion);
    table.add(randomButtons).colspan(2).expandX().fillX().row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the companion HP editing section with companion selector and HP fields. */
  private void addHealthSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_HEALTH);

    table.add(createInfoLabel(skin, "Companion:")).left().width(LABEL_WIDTH);
    hpCompanionSelector = new SelectBox<>(skin);
    table.add(hpCompanionSelector).expandX().fillX().row();

    table.add(createInfoLabel(skin, "Current HP:")).left().width(LABEL_WIDTH);
    currentHpField = new TextField(ZERO_TEXT, skin);
    table.add(currentHpField).expandX().fillX().row();

    table.add(createInfoLabel(skin, "Max HP:")).left().width(LABEL_WIDTH);
    maxHpField = new TextField(DEFAULT_MAX_HP, skin);
    table.add(maxHpField).expandX().fillX().row();

    TextButton setHpBtn = new TextButton("Set HP", skin);
    setHpBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            setCompanionHealth();
          }
        });
    table.add(setHpBtn).colspan(2).expandX().fillX().height(BUTTON_HEIGHT).row();

    table.add().height(SECTION_PAD).colspan(2).row();

    refreshHpCompanionList();
  }

  /** Adds the status section with entity counts and status message. */
  private void addStatusSection(Table table, Skin skin) {
    countsLabel = new Label("", skin);
    countsLabel.setColor(INFO_COLOR);
    table.add(countsLabel).left().colspan(2).row();

    statusLabel = new Label(STATUS_PREFIX + currentStatus, skin);
    statusLabel.setColor(INFO_COLOR);
    table.add(statusLabel).left().colspan(2).row();
  }

  /** Adds a bulk spawn button to the given row. */
  private void addBulkButton(Table row, Skin skin, String label, int count, boolean isEnemy) {
    TextButton button = new TextButton(label, skin);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (isEnemy) {
              bulkSpawnEnemies(count);
            } else {
              bulkSpawnCompanions(count);
            }
          }
        });
    row.add(button).width(BULK_BUTTON_WIDTH).height(BULK_BUTTON_HEIGHT).pad(2);
  }

  /** Adds an action button to a row table. */
  private void addActionButton(Table row, Skin skin, String label, Runnable action) {
    TextButton button = new TextButton(label, skin);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    row.add(button).expandX().fillX().height(BUTTON_HEIGHT).pad(2);
  }

  /** Spawns enemies in bulk at random positions around the spawn center. */
  private void bulkSpawnEnemies(int count) {
    String selectedId = bulkEnemySelector.getSelected();
    boolean useRandom = RANDOM_OPTION.equals(selectedId);
    Array<String> allIds = new Array<>();

    if (useRandom) {
      for (String id : EnemyRegistry.getAllZombieIds()) {
        allIds.add(id);
      }
    }

    for (int i = 0; i < count; i++) {
      String templateId = useRandom ? allIds.get(MathUtils.random(allIds.size - 1)) : selectedId;
      CharacterTemplate template = EnemyRegistry.getInstance().getTemplate(templateId);
      Vector2 position = getRandomBulkPosition();
      enemyFactory.createEnemy(template, position);
    }

    updateStatus("Spawned " + count + " enemies");
  }

  /** Spawns companions in bulk at random positions around the spawn center. */
  private void bulkSpawnCompanions(int count) {
    String selectedId = bulkCompanionSelector.getSelected();

    for (int i = 0; i < count; i++) {
      Vector2 position = getRandomBulkPosition();
      companionFactory.createCompanion(selectedId, position);
    }

    updateStatus("Spawned " + count + " companions");
  }

  /** Spawns a single enemy at the position entered in the coordinate fields. */
  private void addPositionedEnemy() {
    String type = posEnemySelector.getSelected();
    if (type == null) {
      updateStatus(ERROR_SELECT);
      return;
    }

    CharacterTemplate template = EnemyRegistry.getInstance().getTemplate(type);
    if (template == null) {
      updateStatus(ERROR_SELECT);
      return;
    }

    try {
      Vector2 pos = parsePositionPixels();
      enemyFactory.createEnemy(template, toMeters(pos));
      updateStatus(String.format(Locale.ROOT, STATUS_ZOMBIE_FORMAT, type, pos.x, pos.y));
    } catch (NumberFormatException e) {
      updateStatus(ERROR_INVALID_COORDS);
    }
  }

  /** Spawns a single companion at the position entered in the coordinate fields. */
  private void addPositionedCompanion() {
    String type = posCompanionSelector.getSelected();
    if (type == null) {
      updateStatus(ERROR_SELECT);
      return;
    }

    try {
      Vector2 pos = parsePositionPixels();
      companionFactory.createCompanion(type, toMeters(pos));
      updateStatus(String.format(Locale.ROOT, STATUS_COMPANION_FORMAT, type, pos.x, pos.y));
    } catch (NumberFormatException e) {
      updateStatus(ERROR_INVALID_COORDS);
    }
  }

  /** Spawns a random enemy near the player position. */
  private void addRandomEnemy() {
    List<String> types = new ArrayList<>(EnemyRegistry.getAllZombieIds());
    if (types.isEmpty()) {
      updateStatus(ERROR_NO_TYPES);
      return;
    }

    String type = types.get(MathUtils.random(types.size() - 1));
    CharacterTemplate template = EnemyRegistry.getInstance().getTemplate(type);
    Vector2 pos = createRandomNearPosition(ZOMBIE_RANDOM_BASE_PIXELS, ZOMBIE_RANDOM_EXTRA_PIXELS);
    enemyFactory.createEnemy(template, toMeters(pos));
    updateStatus(String.format(Locale.ROOT, STATUS_RANDOM_ZOMBIE_FORMAT, type));
  }

  /** Spawns a random companion near the player position. */
  private void addRandomCompanion() {
    Set<String> typeSet = CompanionRegistry.getAllCompanionIds();
    if (typeSet.isEmpty()) {
      updateStatus(ERROR_NO_TYPES);
      return;
    }

    List<String> types = new ArrayList<>(typeSet);
    String type = types.get(MathUtils.random(types.size() - 1));
    Vector2 pos =
        createRandomNearPosition(COMPANION_RANDOM_BASE_PIXELS, COMPANION_RANDOM_EXTRA_PIXELS);
    companionFactory.createCompanion(type, toMeters(pos));
    updateStatus(String.format(Locale.ROOT, STATUS_RANDOM_COMPANION_FORMAT, type));
  }

  /** Sets the health of the selected companion from the HP fields. */
  private void setCompanionHealth() {
    String selected = hpCompanionSelector.getSelected();
    if (selected == null) {
      updateStatus(ERROR_SELECT);
      return;
    }

    String[] parts = selected.split(SELECTION_SEPARATOR, SELECTION_SPLIT_LIMIT);
    int index = Integer.parseInt(parts[0]);

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(Family.all(CompanionComponent.class, HealthComponent.class).get());
    if (index < 0 || index >= companions.size()) {
      updateStatus(ERROR_SELECT);
      return;
    }

    try {
      int hp = Integer.parseInt(currentHpField.getText());
      int maxHp = Integer.parseInt(maxHpField.getText());
      HealthComponent health = companions.get(index).getComponent(HealthComponent.class);
      health.maxHealth = maxHp;
      health.currentHealth = Math.max(0, Math.min(hp, maxHp));
      updateStatus(STATUS_HP_UPDATED);
    } catch (NumberFormatException e) {
      updateStatus(ERROR_INVALID_HP);
    }
  }

  /** Refreshes the companion HP selector list. */
  private void refreshHpCompanionList() {
    if (hpCompanionSelector == null) {
      return;
    }

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(Family.all(CompanionComponent.class, HealthComponent.class).get());
    String[] items = new String[companions.size()];
    for (int i = 0; i < companions.size(); i++) {
      Entity e = companions.get(i);
      AIComponent ai = e.getComponent(AIComponent.class);
      String name = ai != null ? ai.name : String.format(Locale.ROOT, COMPANION_NAME_FORMAT, i);
      items[i] = i + SELECTION_SEPARATOR + name;
    }

    hpCompanionSelector.setItems(items);
  }

  /** Updates the entity counts display label. */
  private void updateEntityCounts() {
    if (countsLabel == null) {
      return;
    }

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(Family.all(CompanionComponent.class, PositionComponent.class).get());
    ImmutableArray<Entity> enemies =
        engine.getEntitiesFor(Family.all(EnemyComponent.class, PositionComponent.class).get());
    countsLabel.setText(
        String.format(Locale.ROOT, STATUS_COUNTS_FORMAT, companions.size(), enemies.size()));
  }

  /**
   * Updates the status label.
   *
   * @param message the new status message
   */
  private void updateStatus(String message) {
    currentStatus = message;
    if (statusLabel != null) {
      statusLabel.setText(STATUS_PREFIX + message);
    }
  }

  /** Returns a random position for bulk spawning around the group controller position. */
  private Vector2 getRandomBulkPosition() {
    Vector2 controllerPixels = findPlayerPositionOrOrigin();
    float centerX = controllerPixels.x * PhysicsConstants.METERS_PER_PIXEL;
    float centerY = controllerPixels.y * PhysicsConstants.METERS_PER_PIXEL;
    float angle = MathUtils.random(MathUtils.PI2);
    float radius = MathUtils.random(SPAWN_RADIUS_MIN, SPAWN_RADIUS_MAX);
    return new Vector2(
        centerX + MathUtils.cos(angle) * radius, centerY + MathUtils.sin(angle) * radius);
  }

  /** Returns a random position near the player for positioned spawning. */
  private Vector2 createRandomNearPosition(float base, float extra) {
    Vector2 origin = findPlayerPositionOrOrigin();
    float angle = MathUtils.random(MathUtils.PI2);
    float dist = base + MathUtils.random(extra);
    return new Vector2(
        origin.x + MathUtils.cos(angle) * dist, origin.y + MathUtils.sin(angle) * dist);
  }

  /** Parses the X/Y text fields to a position vector. */
  private Vector2 parsePositionPixels() {
    return new Vector2(
        Float.parseFloat(posXField.getText()), Float.parseFloat(posYField.getText()));
  }

  /**
   * Converts pixel coordinates to Box2D meters.
   *
   * @param pixels the position in pixels
   * @return the position in meters
   */
  private Vector2 toMeters(Vector2 pixels) {
    return new Vector2(
        pixels.x * PhysicsConstants.METERS_PER_PIXEL, pixels.y * PhysicsConstants.METERS_PER_PIXEL);
  }

  /**
   * Finds the player entity position, or returns the origin.
   *
   * @return the player position in pixels, or (0,0)
   */
  private Vector2 findPlayerPositionOrOrigin() {
    ImmutableArray<Entity> entities = engine.getEntities();
    for (Entity entity : entities) {
      if (entity.getComponent(GroupControllerComponent.class) != null) {
        PositionComponent pos = entity.getComponent(PositionComponent.class);
        return new Vector2(pos.x, pos.y);
      }
    }
    return new Vector2(0, 0);
  }

  /**
   * Creates a white info label.
   *
   * @param skin the UI skin
   * @param text the label text
   * @return the styled label
   */
  private Label createInfoLabel(Skin skin, String text) {
    Label label = new Label(text, skin);
    label.setColor(Color.WHITE);
    return label;
  }

  /**
   * Adds a colored section header row to the table.
   *
   * @param table the parent table
   * @param skin the UI skin
   * @param text the section header text
   */
  private void addSectionHeader(Table table, Skin skin, String text) {
    Label header = new Label(text, skin);
    header.setColor(SECTION_HEADER_COLOR);
    table.add(header).left().colspan(2).padBottom(CELL_PAD).row();
  }
}
