package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.proyectoM.Main;
import io.github.proyectoM.localization.TranslationKeys;

/** Displays the pause menu while gameplay is suspended. */
public class PauseMenuScreen extends AbstractMenuScreen {
  private static final float BUTTON_PADDING = 15f;
  private static final float TITLE_PADDING = 50f;
  private static final float BUTTON_WIDTH = 200f;
  private static final float BUTTON_HEIGHT = 45f;
  private static final float TITLE_FONT_SCALE = 2.5f;
  private static final float CLEAR_COLOR_ALPHA = 0.7f;

  private Table table;

  /**
   * Creates the pause screen.
   *
   * @param game main game instance
   * @param gameScreen paused gameplay screen, retained for API compatibility
   */
  public PauseMenuScreen(Main game, Screen gameScreen) {
    super(game);
  }

  @Override
  protected void buildUi() {
    createInterface();
  }

  @Override
  protected void clearScreen() {
    Gdx.gl.glClearColor(0f, 0f, 0f, CLEAR_COLOR_ALPHA);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  private void createInterface() {
    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    addTitle();
    addButtons();
  }

  private void addTitle() {
    Label title = new Label(TranslationKeys.GAME_PAUSED.get(), skin);
    title.setFontScale(TITLE_FONT_SCALE);
    table.add(title).padBottom(TITLE_PADDING).row();
  }

  private void addButtons() {
    addButton(TranslationKeys.GAME_RESUME.get(), this::resumeGame);
    addButton(TranslationKeys.GAME_OPTIONS.get(), this::showOptions);
    addButton(TranslationKeys.GAME_MAIN_MENU.get(), this::goToMainMenu);
    addButton(TranslationKeys.GAME_EXIT.get(), this::exitGame);
  }

  private void addButton(String text, Runnable action) {
    TextButton button = createActionButton(text, action);
    table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
  }

  private void resumeGame() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.GAME);
  }

  private void showOptions() {
    ScreenManager.getInstance().showOptionsFrom(ScreenManager.ScreenType.PAUSE);
  }

  private void goToMainMenu() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU);
  }

  private void exitGame() {
    Gdx.app.exit();
  }
}
