package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.Main;
import io.github.proyectoM.localization.TranslationKeys;
import io.github.proyectoM.settings.GameSettings;

/** Displays the lightweight options menu for audio, graphics, and basic controls. */
public class OptionsScreen extends AbstractMenuScreen {
  private static final String PERCENTAGE_FORMAT = "%d%%";

  private static final float PADDING = 10f;
  private static final float SECTION_PADDING = 20f;
  private static final float SLIDER_WIDTH = 200f;
  private static final float VOLUME_MIN = 0f;
  private static final float VOLUME_MAX = 1f;
  private static final float VOLUME_STEP = 0.01f;
  private static final int PERCENTAGE_MULTIPLIER = 100;
  private static final int UI_COLUMNS = 2;
  private static final float BACKGROUND_CLEAR_RED = 1f;
  private static final float BUTTON_WIDTH = 120f;
  private static final float BUTTON_HEIGHT = 40f;
  private static final float TITLE_FONT_SCALE = 1.8f;
  private static final float SECTION_TITLE_FONT_SCALE = 1.2f;

  private final ScreenManager.ScreenType returnTo;
  private final GameSettings settings;

  private Table mainTable;
  private ScrollPane scrollPane;
  private Slider masterVolumeSlider;
  private Slider sfxVolumeSlider;
  private Slider musicVolumeSlider;
  private Label masterVolumeLabel;
  private Label sfxVolumeLabel;
  private Label musicVolumeLabel;
  private CheckBox fullscreenCheckbox;
  private CheckBox vsyncCheckbox;
  private Label audioTitleLabel;
  private Label graphicsTitleLabel;
  private Label controlsTitleLabel;
  private TextButton applyButton;
  private TextButton resetButton;
  private TextButton backButton;

  /** Creates the options screen, returning to the main menu when Back is pressed. */
  public OptionsScreen(Main game) {
    this(game, ScreenManager.ScreenType.MAIN_MENU);
  }

  /** Creates the options screen, returning to {@code returnTo} when Back is pressed. */
  public OptionsScreen(Main game, ScreenManager.ScreenType returnTo) {
    super(game);
    this.returnTo = returnTo;
    this.settings = GameSettings.getInstance();
  }

  @Override
  protected void buildUi() {
    recreateBackgroundRenderer();
    createLayout();
    loadCurrentSettings();
    updateLocalizedTexts();
  }

  @Override
  protected void clearScreen() {
    Gdx.gl.glClearColor(BACKGROUND_CLEAR_RED, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  protected void onResize(int width, int height) {
    if (backgroundRenderer != null) {
      backgroundRenderer.resize(
          stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }
    if (scrollPane != null) {
      scrollPane.invalidate();
      scrollPane.layout();
    }
    if (mainTable != null) {
      mainTable.invalidate();
      mainTable.layout();
    }
  }

  private void createLayout() {
    mainTable = new Table();
    mainTable.pad(PADDING);

    createTitleSection();
    createAudioSection();
    createGraphicsSection();
    createControlsSection();
    createActionButtons();

    scrollPane = new ScrollPane(mainTable, skin);
    scrollPane.setFillParent(true);
    scrollPane.setScrollingDisabled(true, false);
    stage.addActor(scrollPane);
  }

  private void createTitleSection() {
    titleLabel = new Label("", skin);
    titleLabel.setFontScale(TITLE_FONT_SCALE);
    mainTable.add(titleLabel).colspan(UI_COLUMNS).padBottom(SECTION_PADDING).row();
  }

  private void createAudioSection() {
    audioTitleLabel = createSectionTitleLabel();
    mainTable.add(audioTitleLabel).colspan(UI_COLUMNS).padTop(SECTION_PADDING).padBottom(PADDING);
    mainTable.row();

    masterVolumeSlider = createVolumeSlider(this::updateMasterVolumeLabel);
    masterVolumeLabel = createPercentageLabel();
    addVolumeSettingRow(
        TranslationKeys.SETTINGS_MASTER_VOLUME.get(), masterVolumeSlider, masterVolumeLabel);

    sfxVolumeSlider = createVolumeSlider(this::updateSfxVolumeLabel);
    sfxVolumeLabel = createPercentageLabel();
    addVolumeSettingRow(TranslationKeys.SETTINGS_SFX_VOLUME.get(), sfxVolumeSlider, sfxVolumeLabel);

    musicVolumeSlider = createVolumeSlider(this::updateMusicVolumeLabel);
    musicVolumeLabel = createPercentageLabel();
    addVolumeSettingRow(
        TranslationKeys.SETTINGS_MUSIC_VOLUME.get(), musicVolumeSlider, musicVolumeLabel);
  }

  private void addVolumeSettingRow(String labelText, Slider slider, Label valueLabel) {
    mainTable.add(createSettingLabel(labelText)).left().padRight(PADDING);
    mainTable.add(createVolumeRow(slider, valueLabel)).left().row();
  }

  private Slider createVolumeSlider(Runnable labelUpdater) {
    Slider slider = new Slider(VOLUME_MIN, VOLUME_MAX, VOLUME_STEP, false, skin);
    slider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            labelUpdater.run();
          }
        });
    return slider;
  }

  private Label createPercentageLabel() {
    return new Label(String.format(PERCENTAGE_FORMAT, PERCENTAGE_MULTIPLIER), skin);
  }

  private Table createVolumeRow(Slider slider, Label valueLabel) {
    Table volumeTable = new Table();
    volumeTable.add(slider).width(SLIDER_WIDTH);
    volumeTable.add(valueLabel).padLeft(PADDING);
    return volumeTable;
  }

  private void createGraphicsSection() {
    graphicsTitleLabel = createSectionTitleLabel();
    mainTable
        .add(graphicsTitleLabel)
        .colspan(UI_COLUMNS)
        .padTop(SECTION_PADDING)
        .padBottom(PADDING);
    mainTable.row();

    fullscreenCheckbox = new CheckBox("", skin);
    addCheckboxSettingRow(TranslationKeys.SETTINGS_FULLSCREEN.get(), fullscreenCheckbox);

    vsyncCheckbox = new CheckBox("", skin);
    addCheckboxSettingRow(TranslationKeys.SETTINGS_VSYNC.get(), vsyncCheckbox);
  }

  private void addCheckboxSettingRow(String labelText, CheckBox checkBox) {
    mainTable.add(createSettingLabel(labelText)).left().padRight(PADDING);
    mainTable.add(checkBox).left().row();
  }

  private void createControlsSection() {
    controlsTitleLabel = createSectionTitleLabel();
    mainTable
        .add(controlsTitleLabel)
        .colspan(UI_COLUMNS)
        .padTop(SECTION_PADDING)
        .padBottom(PADDING);
    mainTable.row();

    Label placeholder = createSettingLabel(TranslationKeys.SETTINGS_CONTROLS_PLACEHOLDER.get());
    mainTable.add(placeholder).colspan(UI_COLUMNS).left().row();
  }

  private Label createSectionTitleLabel() {
    Label label = new Label("", skin);
    label.setFontScale(SECTION_TITLE_FONT_SCALE);
    return label;
  }

  private Label createSettingLabel(String text) {
    return new Label(text, skin);
  }

  private void createActionButtons() {
    Table buttonTable = new Table();

    applyButton = createActionButton(TranslationKeys.SETTINGS_APPLY.get(), this::applySettings);
    buttonTable.add(applyButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padRight(PADDING);

    resetButton = createActionButton(TranslationKeys.SETTINGS_RESET.get(), this::resetToDefaults);
    buttonTable.add(resetButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padRight(PADDING);

    backButton = createActionButton(TranslationKeys.SETTINGS_BACK.get(), this::goBack);
    buttonTable.add(backButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);

    mainTable.add(buttonTable).colspan(UI_COLUMNS).padTop(SECTION_PADDING);
  }

  private void loadCurrentSettings() {
    masterVolumeSlider.setValue(settings.getMasterVolume());
    sfxVolumeSlider.setValue(settings.getSfxVolume());
    musicVolumeSlider.setValue(settings.getMusicVolume());
    fullscreenCheckbox.setChecked(settings.isFullscreen());
    vsyncCheckbox.setChecked(settings.isVsync());

    updateMasterVolumeLabel();
    updateSfxVolumeLabel();
    updateMusicVolumeLabel();
  }

  private void updateMasterVolumeLabel() {
    masterVolumeLabel.setText(formatPercentage(masterVolumeSlider.getValue()));
  }

  private void updateSfxVolumeLabel() {
    sfxVolumeLabel.setText(formatPercentage(sfxVolumeSlider.getValue()));
  }

  private void updateMusicVolumeLabel() {
    musicVolumeLabel.setText(formatPercentage(musicVolumeSlider.getValue()));
  }

  private String formatPercentage(float value) {
    return String.format(PERCENTAGE_FORMAT, Math.round(value * PERCENTAGE_MULTIPLIER));
  }

  private void applySettings() {
    settings.setMasterVolume(masterVolumeSlider.getValue());
    settings.setSfxVolume(sfxVolumeSlider.getValue());
    settings.setMusicVolume(musicVolumeSlider.getValue());
    settings.setFullscreen(fullscreenCheckbox.isChecked());
    settings.setVsync(vsyncCheckbox.isChecked());
    settings.save();
  }

  private void resetToDefaults() {
    settings.resetToDefaults();
    loadCurrentSettings();
  }

  private void goBack() {
    ScreenManager.getInstance().showScreen(returnTo);
  }

  private void updateLocalizedTexts() {
    if (titleLabel != null) {
      titleLabel.setText(TranslationKeys.SETTINGS_TITLE.get());
    }
    if (audioTitleLabel != null) {
      audioTitleLabel.setText(TranslationKeys.SETTINGS_AUDIO.get());
    }
    if (graphicsTitleLabel != null) {
      graphicsTitleLabel.setText(TranslationKeys.SETTINGS_VIDEO.get());
    }
    if (controlsTitleLabel != null) {
      controlsTitleLabel.setText(TranslationKeys.SETTINGS_CONTROLS.get());
    }
    if (applyButton != null) {
      applyButton.setText(TranslationKeys.SETTINGS_APPLY.get());
    }
    if (resetButton != null) {
      resetButton.setText(TranslationKeys.SETTINGS_RESET.get());
    }
    if (backButton != null) {
      backButton.setText(TranslationKeys.SETTINGS_BACK.get());
    }
  }
}
