package io.github.proyectoM.debug;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.debug.AnimationControlComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent.ActionType;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.AnimationLockComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent.MovementType;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified animation debug panel that shows the current animation state of an entity and provides
 * real-time controls for locking action, movement, and direction states.
 */
public class AnimationPanel implements DebugPanel {

  private static final String TITLE = "Animation";

  private static final String SECTION_INSPECT = "Inspect";
  private static final String SECTION_CONTROL = "Control";
  private static final String SECTION_FRAMES = "Frames";
  private static final String ENTITY_LABEL = "Entity:";
  private static final String ANIMATION_KEY_PREFIX = "Anim: ";
  private static final String ANIMATION_KEY_NONE = "<none>";
  private static final String DIRECTION_VALUE_LABEL = "0";
  private static final String FRAME_MARKER = "-> ";
  private static final String FRAME_SPACE = "   ";
  private static final String FRAME_FORMAT = "%03d ";

  private static final float CELL_PAD = 4f;
  private static final float SECTION_PAD = 8f;
  private static final float LABEL_WIDTH = 100f;
  private static final float DIRECTION_LABEL_WIDTH = 20f;
  private static final int DIRECTION_MIN = 0;
  private static final int DIRECTION_MAX = 7;
  private static final int DIRECTION_STEP = 1;

  private static final Color SECTION_HEADER_COLOR = new Color(0.3f, 0.9f, 1f, 1f);
  private static final Color INFO_COLOR = new Color(0.7f, 0.8f, 0.9f, 1f);

  private final Engine engine;

  private final ComponentMapper<AnimationControlComponent> controlMapper =
      ComponentMapper.getFor(AnimationControlComponent.class);
  private final ComponentMapper<ActionStateComponent> actionMapper =
      ComponentMapper.getFor(ActionStateComponent.class);
  private final ComponentMapper<CompanionComponent> companionMapper =
      ComponentMapper.getFor(CompanionComponent.class);
  private final ComponentMapper<EnemyComponent> enemyMapper =
      ComponentMapper.getFor(EnemyComponent.class);

  private boolean active;
  private final List<Entity> filteredEntities = new ArrayList<>();

  private SelectBox<String> entitySelect;
  private Label animationKeyLabel;
  private TextArea framesListArea;

  private CheckBox enableControlCheck;
  private CheckBox lockActionCheck;
  private SelectBox<ActionType> actionSelect;
  private CheckBox lockMovementCheck;
  private SelectBox<MovementType> movementSelect;
  private CheckBox lockDirectionCheck;
  private Slider directionSlider;
  private Label directionValueLabel;

  /**
   * Creates the animation panel.
   *
   * @param engine the ECS engine used to query animated entities
   */
  public AnimationPanel(Engine engine) {
    this.engine = engine;
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

  /** Updates entity selector, animation details, and direction label. */
  @Override
  public void update(float delta) {
    updateEntitySelectItems();
    updateAnimationDetails();

    Entity entity = getSelectedEntity();
    if (entity != null && controlMapper.has(entity) && directionValueLabel != null) {
      directionValueLabel.setText(String.valueOf((int) directionSlider.getValue()));
    }
  }

  /** Builds the panel UI with inspect, control, and frames sections. */
  @Override
  public Actor buildPanel(Skin skin) {
    Table mainTable = new Table(skin);
    mainTable.top().left();
    mainTable.defaults().pad(CELL_PAD).expandX().fillX();

    addInspectSection(mainTable, skin);
    addControlSection(mainTable, skin);
    addFramesSection(mainTable, skin);

    ScrollPane scrollPane = new ScrollPane(mainTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);

    Table container = new Table(skin);
    container.add(scrollPane).expand().fill();
    return container;
  }

  @Override
  public void render(ShapeRenderer shapeRenderer) {}

  /** Adds the inspect section with entity selector and animation key display. */
  private void addInspectSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_INSPECT);

    table.add(createInfoLabel(skin, ENTITY_LABEL)).left().width(LABEL_WIDTH);
    entitySelect = new SelectBox<>(skin);
    entitySelect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateAnimationDetails();
          }
        });
    table.add(entitySelect).expandX().fillX().row();

    animationKeyLabel = new Label(ANIMATION_KEY_PREFIX + ANIMATION_KEY_NONE, skin);
    animationKeyLabel.setColor(INFO_COLOR);
    table.add(animationKeyLabel).left().colspan(2).row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the control section with enable toggle, lock options, and trigger buttons. */
  private void addControlSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_CONTROL);

    enableControlCheck = new CheckBox(" Enable Control", skin);
    enableControlCheck.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            toggleControl(enableControlCheck.isChecked());
          }
        });
    table.add(enableControlCheck).colspan(2).left().row();

    lockActionCheck = new CheckBox(" Lock Action", skin);
    lockActionCheck.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    table.add(lockActionCheck).left();
    actionSelect = new SelectBox<>(skin);
    actionSelect.setItems(ActionType.values());
    actionSelect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    table.add(actionSelect).expandX().fillX().row();

    lockMovementCheck = new CheckBox(" Lock Movement", skin);
    lockMovementCheck.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    table.add(lockMovementCheck).left();
    movementSelect = new SelectBox<>(skin);
    movementSelect.setItems(MovementType.values());
    movementSelect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    table.add(movementSelect).expandX().fillX().row();

    lockDirectionCheck = new CheckBox(" Lock Direction", skin);
    lockDirectionCheck.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    table.add(lockDirectionCheck).left();
    Table dirTable = new Table(skin);
    directionSlider = new Slider(DIRECTION_MIN, DIRECTION_MAX, DIRECTION_STEP, false, skin);
    directionSlider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            updateControlComponent();
          }
        });
    directionValueLabel = new Label(DIRECTION_VALUE_LABEL, skin);
    directionValueLabel.setColor(INFO_COLOR);
    dirTable.add(directionSlider).expandX().fillX();
    dirTable.add(directionValueLabel).width(DIRECTION_LABEL_WIDTH);
    table.add(dirTable).expandX().fillX().row();

    Table triggers = new Table(skin);
    TextButton attackBtn = new TextButton("Attack", skin);
    attackBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            triggerAttack();
          }
        });
    triggers.add(attackBtn).expandX().fillX().padRight(CELL_PAD);

    TextButton deathBtn = new TextButton("Death", skin);
    deathBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            triggerDeath();
          }
        });
    triggers.add(deathBtn).expandX().fillX();
    table.add(triggers).colspan(2).expandX().fillX().row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the frames section with a read-only text area showing frame names. */
  private void addFramesSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_FRAMES);

    framesListArea = new TextArea("", skin);
    framesListArea.setDisabled(true);
    table.add(framesListArea).expand().fill().colspan(2).row();
  }

  /** Updates the entity selector with filtered companion/enemy entities. */
  private void updateEntitySelectItems() {
    filteredEntities.clear();
    ImmutableArray<Entity> allEntities = engine.getEntities();

    for (int i = 0; i < allEntities.size(); i++) {
      Entity e = allEntities.get(i);
      if (companionMapper.has(e) || enemyMapper.has(e)) {
        filteredEntities.add(e);
      }
    }

    String[] items = new String[filteredEntities.size()];
    for (int i = 0; i < filteredEntities.size(); i++) {
      Entity e = filteredEntities.get(i);
      String type = companionMapper.has(e) ? "Companion" : "Enemy";
      items[i] = "[" + i + "] " + type + " (" + e.getComponents().size() + " comps)";
    }

    int prev = entitySelect.getSelectedIndex();
    entitySelect.setItems(items);
    if (items.length > 0) {
      if (prev >= 0 && prev < items.length) {
        entitySelect.setSelectedIndex(prev);
      } else {
        entitySelect.setSelectedIndex(0);
      }
    }
  }

  /** Updates animation key label and frame list for the currently selected entity. */
  private void updateAnimationDetails() {
    if (entitySelect == null || framesListArea == null || animationKeyLabel == null) {
      return;
    }

    Entity selected = getSelectedEntity();
    if (selected == null) {
      animationKeyLabel.setText(ANIMATION_KEY_PREFIX + ANIMATION_KEY_NONE);
      framesListArea.setText("");
      return;
    }

    ActionStateComponent action = selected.getComponent(ActionStateComponent.class);
    MovementDirectionStateComponent movement =
        selected.getComponent(MovementDirectionStateComponent.class);
    AnimationComponent animComp = selected.getComponent(AnimationComponent.class);

    if (action == null
        || movement == null
        || animComp == null
        || animComp.currentAnimation == null) {
      animationKeyLabel.setText(ANIMATION_KEY_PREFIX + ANIMATION_KEY_NONE);
      framesListArea.setText("");
      return;
    }

    animationKeyLabel.setText(
        ANIMATION_KEY_PREFIX
            + action.actionType
            + "/"
            + movement.movementType
            + "/"
            + movement.directionIndex);

    Animation<TextureRegion> animation = animComp.currentAnimation;
    TextureRegion[] frames = animation.getKeyFrames();

    StringBuilder sb = new StringBuilder();
    TextureRegion current = animation.getKeyFrame(animComp.stateTime, true);
    String currentName = getRegionName(current);

    for (int i = 0; i < frames.length; i++) {
      String name = getRegionName(frames[i]);
      sb.append(name.equals(currentName) ? FRAME_MARKER : FRAME_SPACE);
      sb.append(String.format(FRAME_FORMAT, i));
      sb.append(name);
      sb.append('\n');
    }

    framesListArea.setText(sb.toString());
  }

  /**
   * Returns the selected entity from the filtered list, or null if none.
   *
   * @return the selected entity or null
   */
  private Entity getSelectedEntity() {
    int idx = entitySelect.getSelectedIndex();
    if (idx >= 0 && idx < filteredEntities.size()) {
      return filteredEntities.get(idx);
    }
    return null;
  }

  /**
   * Toggles the AnimationControlComponent on the selected entity.
   *
   * @param enable whether to add or remove the control component
   */
  private void toggleControl(boolean enable) {
    Entity e = getSelectedEntity();
    if (e == null) {
      return;
    }

    if (enable) {
      if (!controlMapper.has(e)) {
        e.add(new AnimationControlComponent());
      }
      updateControlComponent();
    } else {
      if (controlMapper.has(e)) {
        e.remove(AnimationControlComponent.class);
      }
    }
  }

  /**
   * Pushes checkbox and selector states to the AnimationControlComponent of the selected entity.
   */
  private void updateControlComponent() {
    Entity e = getSelectedEntity();
    if (e == null || !controlMapper.has(e)) {
      return;
    }

    AnimationControlComponent acc = controlMapper.get(e);
    acc.lockAction = lockActionCheck.isChecked();
    acc.forcedAction = actionSelect.getSelected();
    acc.lockMovement = lockMovementCheck.isChecked();
    acc.forcedMovement = movementSelect.getSelected();
    acc.lockDirection = lockDirectionCheck.isChecked();
    acc.forcedDirection = (int) directionSlider.getValue();
  }

  /** Triggers the attack action on the selected entity. */
  private void triggerAttack() {
    Entity e = getSelectedEntity();
    if (e == null) {
      return;
    }

    ActionStateComponent asc = actionMapper.get(e);
    if (asc != null) {
      asc.actionType = ActionType.ATTACK;
      asc.actionTime = 0f;
      if (e.getComponent(AnimationLockComponent.class) == null) {
        e.add(new AnimationLockComponent());
      }
    }
  }

  /** Triggers the death action on the selected entity. */
  private void triggerDeath() {
    Entity e = getSelectedEntity();
    if (e == null) {
      return;
    }

    ActionStateComponent asc = actionMapper.get(e);
    if (asc != null) {
      asc.actionType = ActionType.DIE;
      asc.actionTime = 0f;
      if (e.getComponent(AnimationLockComponent.class) == null) {
        e.add(new AnimationLockComponent());
      }
    }
  }

  /**
   * Gets the readable name of a TextureRegion.
   *
   * @param region the texture region
   * @return the atlas region name or toString
   */
  private String getRegionName(TextureRegion region) {
    if (region instanceof TextureAtlas.AtlasRegion) {
      return ((TextureAtlas.AtlasRegion) region).name;
    }
    return region.toString();
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
