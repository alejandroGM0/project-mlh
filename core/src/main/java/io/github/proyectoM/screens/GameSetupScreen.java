package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.Main;
import io.github.proyectoM.localization.TranslationKeys;
import io.github.proyectoM.registry.MapRegistry;
import io.github.proyectoM.templates.MapTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Lets the player choose a map and game mode before starting a new run. */
public class GameSetupScreen extends AbstractMenuScreen {
  private static final String LOCKED_LABEL_FORMAT = "%s [%s]";

  private static final float TITLE_FONT_SIZE_RATIO = 0.08f;
  private static final int MIN_TITLE_FONT_SIZE = 12;
  private static final float TITLE_BORDER_WIDTH = 2f;
  private static final float TITLE_PADDING = 40f;
  private static final float SECTION_PADDING = 20f;
  private static final float BUTTON_PADDING = 10f;
  private static final float BUTTON_CELL_PADDING = BUTTON_PADDING / 2f;
  private static final float BUTTON_WIDTH = 200f;
  private static final float BUTTON_HEIGHT = 50f;

  private static final Color GREEN_ACCENT = new Color(0f, 1f, 0f, 1f);
  private static final Color DISABLED_TEXT_COLOR = new Color(0.5f, 0.5f, 0.5f, 1f);

  private final List<TextButton> mapButtons = new ArrayList<>();
  private final List<String> mapButtonIds = new ArrayList<>();
  private final List<TextButton> modeButtons = new ArrayList<>();
  private final List<GameMode> modeButtonModes = new ArrayList<>();

  private Table table;
  private TextButton startButton;

  private String selectedMapId;
  private GameMode selectedGameMode;

  public GameSetupScreen(Main game) {
    super(game);
  }

  @Override
  protected void buildUi() {
    recreateBackgroundRenderer();

    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    createLayout();
    applyDefaultSelections();
    updateHighlights();
  }

  @Override
  protected void onResize(int width, int height) {
    generateTitleFont(
        height,
        TITLE_FONT_SIZE_RATIO,
        MIN_TITLE_FONT_SIZE,
        GREEN_ACCENT,
        TITLE_BORDER_WIDTH,
        Color.BLACK,
        false);
    if (backgroundRenderer != null) {
      backgroundRenderer.resize(
          stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }
    if (table != null) {
      table.invalidateHierarchy();
    }
    Gdx.input.setInputProcessor(stage);
  }

  private void createLayout() {
    createTitle();
    createMapSection();
    createModeSection();
    createActionButtons();
  }

  private void createTitle() {
    generateTitleFont(
        Gdx.graphics.getHeight(),
        TITLE_FONT_SIZE_RATIO,
        MIN_TITLE_FONT_SIZE,
        GREEN_ACCENT,
        TITLE_BORDER_WIDTH,
        Color.BLACK,
        false);
    titleLabel = new Label(TranslationKeys.GAME_SETUP_TITLE.get(), titleStyle);
    table.add(titleLabel).padBottom(TITLE_PADDING).row();
  }

  private void createMapSection() {
    mapButtons.clear();
    mapButtonIds.clear();

    Label sectionLabel = createSectionLabel(TranslationKeys.GAME_SETUP_SELECT_MAP.get());
    table.add(sectionLabel).padBottom(BUTTON_PADDING).row();

    Table row = new Table();
    for (Map.Entry<String, MapTemplate> mapEntry : getSortedMaps()) {
      String mapId = mapEntry.getKey();
      MapTemplate template = mapEntry.getValue();
      TextButton mapButton =
          createActionButton(
              template.name,
              () -> {
                selectedMapId = mapId;
                updateHighlights();
              });

      row.add(mapButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(BUTTON_CELL_PADDING);
      mapButtons.add(mapButton);
      mapButtonIds.add(mapId);
    }

    table.add(row).padBottom(SECTION_PADDING).row();
  }

  private List<Map.Entry<String, MapTemplate>> getSortedMaps() {
    List<Map.Entry<String, MapTemplate>> sortedMaps =
        new ArrayList<>(MapRegistry.getInstance().getAll().entrySet());
    sortedMaps.sort(Comparator.comparing(entry -> entry.getValue().name));
    return sortedMaps;
  }

  private void createModeSection() {
    modeButtons.clear();
    modeButtonModes.clear();

    Label sectionLabel = createSectionLabel(TranslationKeys.GAME_SETUP_SELECT_MODE.get());
    table.add(sectionLabel).padBottom(BUTTON_PADDING).row();

    for (GameMode mode : GameMode.values()) {
      TextButton modeButton = createModeButton(mode);
      table
          .add(modeButton)
          .width(BUTTON_WIDTH)
          .height(BUTTON_HEIGHT)
          .padBottom(BUTTON_PADDING)
          .row();
      modeButtons.add(modeButton);
      modeButtonModes.add(mode);
    }
  }

  private TextButton createModeButton(GameMode mode) {
    String buttonText =
        mode.isUnlocked()
            ? mode.getDisplayName()
            : String.format(
                LOCKED_LABEL_FORMAT,
                mode.getDisplayName(),
                TranslationKeys.GAME_SETUP_LOCKED.get());

    TextButton button = new TextButton(buttonText, skin);
    if (!mode.isUnlocked()) {
      button.setDisabled(true);
      button.getLabel().setColor(DISABLED_TEXT_COLOR);
      return button;
    }

    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            selectedGameMode = mode;
            updateHighlights();
          }
        });
    return button;
  }

  private Label createSectionLabel(String text) {
    Label sectionLabel = new Label(text, skin);
    sectionLabel.setColor(GREEN_ACCENT);
    return sectionLabel;
  }

  private void createActionButtons() {
    Table buttonRow = new Table();

    startButton =
        createActionButton(
            TranslationKeys.GAME_SETUP_START.get(),
            () -> GameSessionManager.getInstance().startNewGame(selectedMapId, selectedGameMode));
    startButton.setDisabled(true);
    startButton.getLabel().setColor(DISABLED_TEXT_COLOR);
    buttonRow.add(startButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(BUTTON_CELL_PADDING);

    TextButton backButton =
        createActionButton(
            TranslationKeys.GAME_SETUP_BACK.get(),
            () -> ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU));
    buttonRow.add(backButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(BUTTON_CELL_PADDING);

    table.add(buttonRow).padTop(SECTION_PADDING).row();
  }

  private void applyDefaultSelections() {
    Map<String, MapTemplate> maps = MapRegistry.getInstance().getAll();
    if (maps.size() == 1) {
      selectedMapId = maps.keySet().iterator().next();
    }
    selectedGameMode = GameMode.SURVIVAL;
  }

  private void updateHighlights() {
    updateMapHighlights();
    updateModeHighlights();
    updateStartButtonState();
  }

  private void updateMapHighlights() {
    for (int index = 0; index < mapButtons.size(); index++) {
      TextButton mapButton = mapButtons.get(index);
      String mapId = mapButtonIds.get(index);
      mapButton.getLabel().setColor(mapId.equals(selectedMapId) ? GREEN_ACCENT : Color.WHITE);
    }
  }

  private void updateModeHighlights() {
    for (int index = 0; index < modeButtons.size(); index++) {
      TextButton modeButton = modeButtons.get(index);
      GameMode mode = modeButtonModes.get(index);
      if (!mode.isUnlocked()) {
        modeButton.getLabel().setColor(DISABLED_TEXT_COLOR);
        continue;
      }

      modeButton.getLabel().setColor(mode == selectedGameMode ? GREEN_ACCENT : Color.WHITE);
    }
  }

  private void updateStartButtonState() {
    boolean canStart =
        selectedMapId != null && selectedGameMode != null && selectedGameMode.isUnlocked();
    startButton.setDisabled(!canStart);
    startButton.getLabel().setColor(canStart ? Color.WHITE : DISABLED_TEXT_COLOR);
  }
}
