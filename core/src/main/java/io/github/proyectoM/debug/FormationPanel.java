package io.github.proyectoM.debug;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;

/**
 * Unified formation debug panel that shows formation status and provides real-time controls for
 * formation type and spacing.
 */
public class FormationPanel implements DebugPanel {

  private static final String TITLE = "Formation";

  private static final String SECTION_STATUS = "Status";
  private static final String SECTION_CONTROLS = "Controls";
  private static final String SECTION_MEMBERS = "Members";
  private static final String FORMATION_LABEL = "Formation:";
  private static final String MEMBERS_LABEL = "Members:";
  private static final String TYPE_LABEL = "Type:";
  private static final String SPACING_LABEL = "Spacing:";
  private static final String SPACING_SUFFIX = " px";
  private static final String PLACEHOLDER_DASH = "---";

  private static final float SECTION_PAD = 8f;
  private static final float CELL_PAD = 4f;
  private static final float LABEL_WIDTH = 100f;
  private static final float SLIDER_MIN = 100f;
  private static final float SLIDER_MAX = 500f;
  private static final float SLIDER_STEP = 20f;
  private static final float SLIDER_DEFAULT = 240f;
  private static final String MEMBER_INFO_FORMAT =
      "Idx: %d | Pos: (%.0f, %.0f) | Offset: (%.0f, %.0f)";

  private static final Color SECTION_HEADER_COLOR = new Color(0.3f, 0.9f, 1f, 1f);
  private static final Color INFO_COLOR = new Color(0.7f, 0.8f, 0.9f, 1f);

  private final Engine engine;

  private boolean active;
  private Label formationTypeLabel;
  private Label memberCountLabel;
  private Label spacingValueLabel;
  private SelectBox<String> formationSelectBox;
  private Slider spacingSlider;
  private Table membersTable;

  /**
   * Creates the formation panel.
   *
   * @param engine the ECS engine used to query formation entities
   */
  public FormationPanel(Engine engine) {
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

  /** Updates formation status labels, companion table, and syncs controls to current state. */
  @Override
  public void update(float delta) {
    syncControllerState();
    updateMembersTable();
  }

  /** Builds the panel UI with status, controls, and members sections. */
  @Override
  public Actor buildPanel(Skin skin) {
    Table mainTable = new Table(skin);
    mainTable.top().left();
    mainTable.defaults().pad(CELL_PAD).expandX().fillX();

    addStatusSection(mainTable, skin);
    addControlsSection(mainTable, skin);
    addMembersSection(mainTable, skin);

    ScrollPane scrollPane = new ScrollPane(mainTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);

    Table container = new Table(skin);
    container.add(scrollPane).expand().fill();
    return container;
  }

  /** Adds the formation status section with formation type and member count labels. */
  private void addStatusSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_STATUS);

    table.add(createInfoLabel(skin, FORMATION_LABEL)).left().width(LABEL_WIDTH);
    formationTypeLabel = new Label(PLACEHOLDER_DASH, skin);
    formationTypeLabel.setColor(INFO_COLOR);
    table.add(formationTypeLabel).expandX().fillX().row();

    table.add(createInfoLabel(skin, MEMBERS_LABEL)).left().width(LABEL_WIDTH);
    memberCountLabel = new Label("0", skin);
    memberCountLabel.setColor(INFO_COLOR);
    table.add(memberCountLabel).expandX().fillX().row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the formation controls section with a type selector and spacing slider. */
  private void addControlsSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_CONTROLS);

    table.add(createInfoLabel(skin, TYPE_LABEL)).left().width(LABEL_WIDTH);
    formationSelectBox = new SelectBox<>(skin);
    formationSelectBox.setItems("LINE", "COLUMN", "CIRCLE", "V_FORMATION", "SQUARE");
    formationSelectBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            applyFormationType(formationSelectBox.getSelected());
          }
        });
    table.add(formationSelectBox).expandX().fillX().row();

    table.add(createInfoLabel(skin, SPACING_LABEL)).left().width(LABEL_WIDTH);
    Table sliderRow = new Table(skin);
    spacingSlider = new Slider(SLIDER_MIN, SLIDER_MAX, SLIDER_STEP, false, skin);
    spacingSlider.setValue(SLIDER_DEFAULT);
    spacingSlider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            applyFormationSpacing(spacingSlider.getValue());
            spacingValueLabel.setText((int) spacingSlider.getValue() + SPACING_SUFFIX);
          }
        });
    spacingValueLabel = new Label((int) SLIDER_DEFAULT + SPACING_SUFFIX, skin);
    spacingValueLabel.setColor(INFO_COLOR);
    sliderRow.add(spacingSlider).expandX().fillX();
    sliderRow.add(spacingValueLabel).padLeft(CELL_PAD);
    table.add(sliderRow).expandX().fillX().row();

    table.add().height(SECTION_PAD).colspan(2).row();
  }

  /** Adds the members list section showing each companion's position and formation offset. */
  private void addMembersSection(Table table, Skin skin) {
    addSectionHeader(table, skin, SECTION_MEMBERS);

    membersTable = new Table(skin);
    membersTable.top().left();
    membersTable.defaults().pad(2).expandX().fillX().left();

    table.add(membersTable).expand().fill().colspan(2).row();
  }

  /** Reads the current GroupController state and syncs all UI widgets to match. */
  private void syncControllerState() {
    ImmutableArray<Entity> controllers =
        engine.getEntitiesFor(
            Family.all(GroupControllerComponent.class, PositionComponent.class).get());

    if (controllers.size() == 0) {
      formationTypeLabel.setText(PLACEHOLDER_DASH);
      return;
    }

    GroupControllerComponent controller =
        controllers.first().getComponent(GroupControllerComponent.class);

    formationTypeLabel.setText(controller.currentFormation.name());
    formationSelectBox.setSelected(controller.currentFormation.name());
    spacingSlider.setValue(controller.formationSpacing);
    spacingValueLabel.setText((int) controller.formationSpacing + SPACING_SUFFIX);

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(
            Family.all(SquadMovementComponent.class, PositionComponent.class).get());
    memberCountLabel.setText(String.valueOf(companions.size()));
  }

  /** Refreshes the companion member list table with current positions and offsets. */
  private void updateMembersTable() {
    if (membersTable == null) {
      return;
    }

    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(
            Family.all(SquadMovementComponent.class, PositionComponent.class).get());

    membersTable.clear();

    for (Entity companion : companions) {
      SquadMovementComponent squad = companion.getComponent(SquadMovementComponent.class);
      PositionComponent pos = companion.getComponent(PositionComponent.class);

      String info =
          String.format(
              MEMBER_INFO_FORMAT,
              squad.memberIndex,
              pos.x,
              pos.y,
              squad.formationOffset.x,
              squad.formationOffset.y);

      Label memberLabel = new Label(info, membersTable.getSkin());
      memberLabel.setColor(INFO_COLOR);
      membersTable.add(memberLabel).left().row();
    }
  }

  /**
   * Applies the selected formation type to the GroupController entity.
   *
   * @param formationName the name of the formation type enum value
   */
  private void applyFormationType(String formationName) {
    ImmutableArray<Entity> controllers =
        engine.getEntitiesFor(Family.all(GroupControllerComponent.class).get());

    if (controllers.size() == 0) {
      return;
    }

    GroupControllerComponent controller =
        controllers.first().getComponent(GroupControllerComponent.class);

    try {
      controller.currentFormation = GroupControllerComponent.FormationType.valueOf(formationName);
      controller.formationChanged = true;
    } catch (IllegalArgumentException ignored) {
    }
  }

  /**
   * Applies the given spacing value to the GroupController entity.
   *
   * @param newSpacing the new formation spacing in pixels
   */
  private void applyFormationSpacing(float newSpacing) {
    ImmutableArray<Entity> controllers =
        engine.getEntitiesFor(Family.all(GroupControllerComponent.class).get());

    if (controllers.size() == 0) {
      return;
    }

    GroupControllerComponent controller =
        controllers.first().getComponent(GroupControllerComponent.class);

    controller.formationSpacing = newSpacing;
    controller.formationChanged = true;
  }

  /**
   * Creates a styled info label for use in the panel layout.
   *
   * @param skin the UI skin
   * @param text the label text
   * @return a colored label
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
