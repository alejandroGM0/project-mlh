package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.proyectoM.Main;
import io.github.proyectoM.localization.TranslationKeys;

/** Displays the main menu and routes the player to the primary game flows. */
public class MainMenuScreen extends AbstractMenuScreen {
  private static final String GAME_VERSION = "V DEV-0.0.1";

  private static final float BUTTON_PADDING = 10f;
  private static final float TITLE_PADDING = 60f;
  private static final float BUTTON_WIDTH = 200f;
  private static final float BUTTON_HEIGHT = 50f;
  private static final float TITLE_FONT_SIZE_RATIO = 0.12f;
  private static final float VERSION_LABEL_SCALE = 0.8f;
  private static final float VERSION_LABEL_ALPHA = 0.5f;
  private static final float VERSION_LABEL_MARGIN = 10f;
  private static final float TITLE_COLOR_RED = 0.6f;
  private static final float TITLE_COLOR_GREEN = 0.8f;
  private static final float TITLE_COLOR_BLUE = 0.6f;
  private static final float FULL_ALPHA = 1f;
  private static final int MIN_TITLE_FONT_SIZE = 12;

  private Table table;
  private Label versionLabel;

  /**
   * Creates the main menu screen.
   *
   * @param game main game instance
   */
  public MainMenuScreen(Main game) {
    super(game);
  }

  @Override
  protected void buildUi() {
    recreateBackgroundRenderer();

    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    createTitle();
    createButtons();
    createVersionLabel();
  }

  private void createTitle() {
    generateTitleFont(
        Gdx.graphics.getHeight(),
        TITLE_FONT_SIZE_RATIO,
        MIN_TITLE_FONT_SIZE,
        new Color(TITLE_COLOR_RED, TITLE_COLOR_GREEN, TITLE_COLOR_BLUE, FULL_ALPHA),
        0,
        null,
        true);
    titleLabel = new Label(TranslationKeys.MENU_TITLE.get(), titleStyle);
    table.add(titleLabel).padBottom(TITLE_PADDING).row();
  }

  private void createButtons() {
    addButton(TranslationKeys.MENU_START.get(), this::startGame);
    addButton(TranslationKeys.MENU_OPTIONS.get(), this::openOptions);
    addButton(TranslationKeys.MENU_EXIT.get(), this::exitGame);
  }

  private void addButton(String text, Runnable action) {
    TextButton button = createActionButton(text, action);
    table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
  }

  private void createVersionLabel() {
    versionLabel = new Label(GAME_VERSION, skin, "default");
    versionLabel.setFontScale(VERSION_LABEL_SCALE);
    versionLabel.setColor(FULL_ALPHA, FULL_ALPHA, FULL_ALPHA, VERSION_LABEL_ALPHA);
    stage.addActor(versionLabel);
    positionVersionLabel();
  }

  private void positionVersionLabel() {
    if (versionLabel == null) {
      return;
    }

    float worldWidth = stage.getViewport().getWorldWidth();
    versionLabel.setPosition(
        worldWidth - versionLabel.getWidth() - VERSION_LABEL_MARGIN, VERSION_LABEL_MARGIN);
  }

  @Override
  protected void onResize(int width, int height) {
    generateTitleFont(
        height,
        TITLE_FONT_SIZE_RATIO,
        MIN_TITLE_FONT_SIZE,
        new Color(TITLE_COLOR_RED, TITLE_COLOR_GREEN, TITLE_COLOR_BLUE, FULL_ALPHA),
        0,
        null,
        true);

    if (backgroundRenderer != null) {
      backgroundRenderer.resize(
          stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }

    if (table != null) {
      table.invalidateHierarchy();
    }

    positionVersionLabel();
    Gdx.input.setInputProcessor(stage);
  }

  private void startGame() {
    GameSessionManager.getInstance().startNewGame();
  }

  private void openOptions() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.SETTINGS);
  }

  private void exitGame() {
    Gdx.app.exit();
  }
}
