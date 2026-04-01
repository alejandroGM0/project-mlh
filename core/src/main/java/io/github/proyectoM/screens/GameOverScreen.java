package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.proyectoM.Main;
import io.github.proyectoM.components.game.ScoreComponent;
import io.github.proyectoM.localization.TranslationKeys;

/** Displays the post-run summary and restart options after the player is defeated. */
public class GameOverScreen extends AbstractMenuScreen {
  private static final String SURVIVAL_TIME_FORMAT = "%02d:%02d";

  private static final float BUTTON_PADDING = 15f;
  private static final float TITLE_PADDING = 30f;
  private static final float SUBTITLE_PADDING = 20f;
  private static final float STATS_ROW_PADDING = 5f;
  private static final float SECTION_PADDING = 20f;
  private static final float BUTTON_WIDTH = 250f;
  private static final float TITLE_FONT_SCALE = 2.5f;
  private static final int META_COINS_SCORE_DIVISOR = 100;
  private static final int MIN_META_COINS_EARNED = 1;
  private static final int SECONDS_PER_MINUTE = 60;

  private static final float CLEAR_COLOR_RED = 0.1f;
  private static final float CLEAR_COLOR_ALPHA = 0.9f;

  private final GameSessionManager sessionManager;
  private final ScoreComponent finalStats;

  /**
   * Creates the game-over screen.
   *
   * @param game main game instance
   * @param finalStats score and run summary to display
   */
  public GameOverScreen(Main game, ScoreComponent finalStats) {
    super(game);
    this.sessionManager = GameSessionManager.getInstance();
    this.finalStats = finalStats;
  }

  @Override
  protected void buildUi() {
    createMainTable();
  }

  @Override
  protected void clearScreen() {
    Gdx.gl.glClearColor(CLEAR_COLOR_RED, 0f, 0f, CLEAR_COLOR_ALPHA);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  private void createMainTable() {
    Table mainTable = new Table();
    mainTable.setFillParent(true);
    stage.addActor(mainTable);

    addTitle(mainTable);
    addSubtitle(mainTable);
    addStatistics(mainTable);
    addButtons(mainTable);
  }

  private void addTitle(Table mainTable) {
    Label label = new Label(TranslationKeys.MSG_GAME_OVER.get(), skin);
    label.setFontScale(TITLE_FONT_SCALE);
    label.setColor(Color.RED);
    mainTable.add(label).padBottom(TITLE_PADDING).row();
  }

  private void addSubtitle(Table mainTable) {
    Label subtitleLabel = new Label(TranslationKeys.MSG_GAME_OVER_SUBTITLE.get(), skin);
    subtitleLabel.setColor(Color.WHITE);
    mainTable.add(subtitleLabel).padBottom(SUBTITLE_PADDING).row();
  }

  private void addStatistics(Table mainTable) {
    if (finalStats == null) {
      return;
    }

    Table statsTable = new Table();
    addWaveStatistic(statsTable);
    addTimeStatistic(statsTable);
    addKillStatistic(statsTable);
    addMetaCoinsEarned(statsTable);

    mainTable.add(statsTable).padBottom(SECTION_PADDING).row();
  }

  private void addWaveStatistic(Table statsTable) {
    if (finalStats.finalWave <= 0) {
      return;
    }

    addStatisticLabel(
        statsTable, TranslationKeys.MSG_WAVE_REACHED.format(finalStats.finalWave), Color.CYAN);
  }

  private void addTimeStatistic(Table statsTable) {
    if (finalStats.timeSurvived <= 0) {
      return;
    }

    int minutes = (int) (finalStats.timeSurvived / SECONDS_PER_MINUTE);
    int seconds = (int) (finalStats.timeSurvived % SECONDS_PER_MINUTE);
    String formattedTime = String.format(SURVIVAL_TIME_FORMAT, minutes, seconds);
    addStatisticLabel(
        statsTable, TranslationKeys.MSG_SURVIVAL_TIME.format(formattedTime), Color.WHITE);
  }

  private void addKillStatistic(Table statsTable) {
    if (finalStats.enemiesKilled <= 0) {
      return;
    }

    addStatisticLabel(
        statsTable,
        TranslationKeys.MSG_ENEMIES_KILLED.format(finalStats.enemiesKilled),
        Color.YELLOW);
  }

  private void addMetaCoinsEarned(Table statsTable) {
    int metaCoinsEarned =
        Math.max(MIN_META_COINS_EARNED, finalStats.score / META_COINS_SCORE_DIVISOR);
    addStatisticLabel(
        statsTable, TranslationKeys.MSG_META_COINS_EARNED.format(metaCoinsEarned), Color.GOLD);

    Label totalCoinsLabel = new Label(TranslationKeys.MSG_TOTAL_META_COINS.get(), skin);
    totalCoinsLabel.setColor(Color.YELLOW);
    statsTable.add(totalCoinsLabel).padBottom(SECTION_PADDING).row();
  }

  private void addStatisticLabel(Table statsTable, String text, Color color) {
    Label label = new Label(text, skin);
    label.setColor(color);
    statsTable.add(label).padBottom(STATS_ROW_PADDING).row();
  }

  private void addButtons(Table mainTable) {
    addButton(mainTable, TranslationKeys.MSG_RESTART_GAME.get(), this::restartGame);
    addButton(mainTable, TranslationKeys.GAME_MAIN_MENU.get(), this::goToMainMenu);
    addButton(mainTable, TranslationKeys.GAME_EXIT.get(), this::exitGame);
  }

  private void addButton(Table mainTable, String text, Runnable action) {
    TextButton button = createActionButton(text, action);
    mainTable.add(button).width(BUTTON_WIDTH).padBottom(BUTTON_PADDING).row();
  }

  private void restartGame() {
    sessionManager.startNewGame();
  }

  private void goToMainMenu() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU);
  }

  private void exitGame() {
    Gdx.app.exit();
  }
}
