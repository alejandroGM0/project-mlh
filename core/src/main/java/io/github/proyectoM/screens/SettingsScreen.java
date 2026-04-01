package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;
import io.github.proyectoM.Main;
import io.github.proyectoM.localization.LocalizationManager;
import io.github.proyectoM.localization.TranslationKeys;
import io.github.proyectoM.settings.GameSettings;
import io.github.proyectoM.ui.UISoundHelper;

/** Displays grouped game settings such as graphics, audio, controls, and language. */
public class SettingsScreen extends AbstractMenuScreen {
  private static final float SECTION_WIDTH_RATIO = 0.7f;
  private static final int SMALL_SCREEN_THRESHOLD = 800;
  private static final float PADDING = 10f;
  private static final float SECTION_PADDING = 20f;
  private static final float SLIDER_WIDTH = 250f;
  private static final float SELECT_WIDTH = 200f;
  private static final float BUTTON_WIDTH = 160f;
  private static final float BUTTON_HEIGHT = 45f;
  private static final float TITLE_PADDING = 30f;
  private static final float TITLE_FONT_SIZE_RATIO = 0.08f;
  private static final int MIN_TITLE_FONT_SIZE = 12;
  private static final float TITLE_BORDER_WIDTH = 2f;
  private static final float SECTION_TITLE_FONT_SCALE = 1.3f;
  private static final float STATUS_FONT_SCALE = 0.7f;
  private static final float SLIDER_MIN = 0f;
  private static final float SLIDER_MAX_PERCENT = 100f;
  private static final float SLIDER_STEP = 1f;
  private static final int DEFAULT_BRIGHTNESS = 50;
  private static final int DEFAULT_SENSITIVITY = 50;
  private static final float TEXT_SIZE_MIN = 75f;
  private static final float TEXT_SIZE_MAX = 150f;
  private static final float TEXT_SIZE_STEP = 5f;
  private static final int DEFAULT_TEXT_SIZE = 100;
  private static final int DEFAULT_RESOLUTION_INDEX = 2;
  private static final float STATUS_MESSAGE_DELAY_SECONDS = 3f;
  private static final float PADDING_DIVISOR = 2f;
  private static final float LABEL_VALUE_WIDTH = 50f;
  private static final float SECTION_SEPARATOR_HEIGHT = 1f;
  private static final Color GREEN_ACCENT = new Color(0f, 1f, 0f, 1f);
  private static final Color SECTION_BG = new Color(0.12f, 0.12f, 0.12f, 0.7f);
  private static final Color SEPARATOR_COLOR = new Color(0.2f, 0.2f, 0.2f, 1f);
  private static final String[] RESOLUTIONS = {
    "1280x720", "1366x768", "1920x1080", "2560x1440", "3840x2160"
  };
  private static final String LANGUAGE_ENGLISH = "English";
  private static final String LANGUAGE_SPANISH = "Español";
  private static final String LANGUAGE_FRENCH = "Français";
  private static final String LANGUAGE_CODE_ENGLISH = "en";
  private static final String LANGUAGE_CODE_SPANISH = "es";
  private static final String LANGUAGE_CODE_FRENCH = "fr";

  private final GameSettings settings;
  private Label statusLabel;
  private ScrollPane scrollPane;

  /** Creates the settings screen. */
  public SettingsScreen(Main game) {
    super(game);
    this.settings = GameSettings.getInstance();
  }

  @Override
  protected void buildUi() {
    recreateBackgroundRenderer();
    createUi();
  }

  @Override
  public void hide() {}

  @Override
  protected void onResize(int width, int height) {
    if (backgroundRenderer != null) {
      backgroundRenderer.resize(
          stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }
    generateTitleFont(
        height,
        TITLE_FONT_SIZE_RATIO,
        MIN_TITLE_FONT_SIZE,
        GREEN_ACCENT,
        TITLE_BORDER_WIDTH,
        Color.BLACK,
        false);
    refreshUi();
  }

  private void createUi() {
    Table root = new Table();
    root.setFillParent(true);
    stage.addActor(root);
    createTitle();
    root.add(titleLabel).padTop(SECTION_PADDING).padBottom(TITLE_PADDING).row();
    createContent(root);
    root.add(createActionButtons()).padTop(SECTION_PADDING).padBottom(PADDING).row();
    statusLabel = new Label(TranslationKeys.SETTINGS_READY.get(), skin);
    statusLabel.setFontScale(STATUS_FONT_SCALE);
    statusLabel.setColor(Color.GRAY);
    root.add(statusLabel).padTop(PADDING);
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
    titleLabel = new Label(TranslationKeys.SETTINGS_TITLE.get(), titleStyle);
  }

  private void createContent(Table root) {
    Table contentTable = new Table();
    contentTable.pad(SECTION_PADDING);
    boolean smallScreen = Gdx.graphics.getWidth() < SMALL_SCREEN_THRESHOLD;
    if (smallScreen) {
      contentTable.defaults().space(SECTION_PADDING);
    } else {
      float sectionWidth = Gdx.graphics.getWidth() * SECTION_WIDTH_RATIO;
      contentTable.defaults().width(sectionWidth).space(SECTION_PADDING);
    }

    contentTable.add(createGraphicsSection()).row();
    contentTable.add(createAudioSection()).row();
    contentTable.add(createGameplaySection()).row();
    contentTable.add(createControlsSection()).row();
    contentTable.add(createAccessibilitySection()).row();
    contentTable.add(createLanguageSection()).row();

    scrollPane = new ScrollPane(contentTable, skin);
    ScrollPane.ScrollPaneStyle style =
        new ScrollPane.ScrollPaneStyle(skin.get(ScrollPane.ScrollPaneStyle.class));
    style.background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));
    scrollPane.setStyle(style);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);
    scrollPane.setOverscroll(false, false);
    scrollPane.setClamp(true);

    if (smallScreen) {
      root.add(scrollPane).expand().fill().pad(PADDING).row();
    } else {
      float sectionWidth = Gdx.graphics.getWidth() * SECTION_WIDTH_RATIO;
      root.add(scrollPane)
          .width(sectionWidth + SECTION_PADDING * 2)
          .expand()
          .fill()
          .pad(PADDING)
          .row();
    }
  }

  private Slider createSlider(float min, float max, float step, boolean vertical) {
    Slider slider = new Slider(min, max, step, vertical, skin);
    slider.addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.stop();
            return false;
          }

          @Override
          public void touchDragged(InputEvent event, float x, float y, int pointer) {
            event.stop();
          }
        });
    return slider;
  }

  private Table createSection(String title) {
    Table section = new Table();
    section.setBackground(skin.newDrawable("white", SECTION_BG));
    section.pad(SECTION_PADDING);
    Label sectionTitle = new Label(title, skin);
    sectionTitle.setFontScale(SECTION_TITLE_FONT_SCALE);
    sectionTitle.setColor(GREEN_ACCENT);
    section.add(sectionTitle).left().colspan(2).padBottom(PADDING).row();
    section.columnDefaults(0).left().expandX().fillX().padRight(PADDING);
    section.columnDefaults(1).right();
    return section;
  }

  private SelectBox<String> addSelectRow(
      Table section, TranslationKeys labelKey, TranslationKeys[] options, int defaultIndex) {
    Label label = new Label(labelKey.get(), skin);
    label.setColor(Color.WHITE);
    section.add(label);
    SelectBox<String> selectBox = new SelectBox<>(skin);
    String[] translatedOptions = new String[options.length];
    for (int index = 0; index < options.length; index++) {
      translatedOptions[index] = options[index].get();
    }
    selectBox.setItems(translatedOptions);
    selectBox.setSelected(translatedOptions[defaultIndex]);
    section.add(selectBox).width(SELECT_WIDTH).row();
    return selectBox;
  }

  private void addSliderRow(
      Table section,
      TranslationKeys labelKey,
      float min,
      float max,
      float step,
      float initialValue,
      Label valueLabel,
      ChangeListener listener) {
    Label label = new Label(labelKey.get(), skin);
    label.setColor(Color.WHITE);
    section.add(label);
    Table sliderTable = new Table();
    Slider slider = createSlider(min, max, step, false);
    slider.setValue(initialValue);
    if (listener != null) {
      slider.addListener(listener);
    }
    sliderTable.add(slider).width(SLIDER_WIDTH);
    sliderTable.add(valueLabel).padLeft(PADDING).width(LABEL_VALUE_WIDTH);
    section.add(sliderTable).row();
  }

  private void addCheckboxRow(
      Table section, TranslationKeys labelKey, boolean initialValue, ChangeListener listener) {
    Label label = new Label(labelKey.get(), skin);
    label.setColor(Color.WHITE);
    section.add(label);
    CheckBox checkBox = new CheckBox("", skin);
    checkBox.setChecked(initialValue);
    if (listener != null) {
      checkBox.addListener(listener);
    }
    section.add(checkBox).row();
  }

  private void addSeparator(Table table) {
    table.row();
    Label separator = new Label("", skin);
    separator.setColor(SEPARATOR_COLOR);
    table
        .add(separator)
        .colspan(2)
        .fillX()
        .height(SECTION_SEPARATOR_HEIGHT)
        .padTop(PADDING / PADDING_DIVISOR)
        .padBottom(PADDING / PADDING_DIVISOR)
        .row();
  }

  private Table createGraphicsSection() {
    Table section = createSection(TranslationKeys.SETTINGS_VIDEO.get());
    addSelectRow(
        section,
        TranslationKeys.SETTINGS_QUALITY,
        new TranslationKeys[] {
          TranslationKeys.QUALITY_LOW,
          TranslationKeys.QUALITY_MEDIUM,
          TranslationKeys.QUALITY_HIGH,
          TranslationKeys.QUALITY_ULTRA
        },
        1);
    addSeparator(section);
    Label resolutionLabel = new Label(TranslationKeys.SETTINGS_RESOLUTION.get(), skin);
    resolutionLabel.setColor(Color.WHITE);
    section.add(resolutionLabel);
    SelectBox<String> resolutionSelect = new SelectBox<>(skin);
    resolutionSelect.setItems(RESOLUTIONS);
    resolutionSelect.setSelected(RESOLUTIONS[DEFAULT_RESOLUTION_INDEX]);
    section.add(resolutionSelect).width(SELECT_WIDTH).row();
    addSeparator(section);
    Label brightnessValue = new Label(DEFAULT_BRIGHTNESS + "%", skin);
    brightnessValue.setColor(GREEN_ACCENT);
    addSliderRow(
        section,
        TranslationKeys.SETTINGS_BRIGHTNESS,
        SLIDER_MIN,
        SLIDER_MAX_PERCENT,
        SLIDER_STEP,
        DEFAULT_BRIGHTNESS,
        brightnessValue,
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            brightnessValue.setText((int) ((Slider) actor).getValue() + "%");
          }
        });
    addSeparator(section);
    addCheckboxRow(
        section,
        TranslationKeys.SETTINGS_VSYNC,
        settings.isVsync(),
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            settings.setVsync(((CheckBox) actor).isChecked());
          }
        });
    addSeparator(section);
    addCheckboxRow(
        section,
        TranslationKeys.SETTINGS_FULLSCREEN,
        settings.isFullscreen(),
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            settings.setFullscreen(((CheckBox) actor).isChecked());
          }
        });
    return section;
  }

  private Table createAudioSection() {
    Table section = createSection(TranslationKeys.SETTINGS_AUDIO.get());
    addVolumeSlider(
        section,
        TranslationKeys.SETTINGS_MASTER_VOLUME,
        settings.getMasterVolume(),
        value -> settings.setMasterVolume(value));
    addSeparator(section);
    addVolumeSlider(
        section,
        TranslationKeys.SETTINGS_MUSIC_VOLUME,
        settings.getMusicVolume(),
        value -> settings.setMusicVolume(value));
    addSeparator(section);
    addVolumeSlider(
        section,
        TranslationKeys.SETTINGS_SFX_VOLUME,
        settings.getSfxVolume(),
        value -> settings.setSfxVolume(value));
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_MUTE_ALL, false, null);
    return section;
  }

  private void addVolumeSlider(
      Table section,
      TranslationKeys labelKey,
      float initialVolume,
      java.util.function.Consumer<Float> setter) {
    Label valueLabel = new Label((int) (initialVolume * 100) + "%", skin);
    valueLabel.setColor(GREEN_ACCENT);
    addSliderRow(
        section,
        labelKey,
        SLIDER_MIN,
        SLIDER_MAX_PERCENT,
        SLIDER_STEP,
        initialVolume * SLIDER_MAX_PERCENT,
        valueLabel,
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            float value = ((Slider) actor).getValue();
            valueLabel.setText((int) value + "%");
            setter.accept(value / 100f);
          }
        });
  }

  private Table createGameplaySection() {
    Table section = createSection(TranslationKeys.SETTINGS_GAMEPLAY.get());
    addSelectRow(
        section,
        TranslationKeys.SETTINGS_DIFFICULTY,
        new TranslationKeys[] {
          TranslationKeys.DIFFICULTY_EASY,
          TranslationKeys.DIFFICULTY_NORMAL,
          TranslationKeys.DIFFICULTY_HARD,
          TranslationKeys.DIFFICULTY_EXTREME
        },
        1);
    addSeparator(section);
    Label sensitivityValue = new Label(DEFAULT_SENSITIVITY + "%", skin);
    sensitivityValue.setColor(GREEN_ACCENT);
    addSliderRow(
        section,
        TranslationKeys.SETTINGS_SENSITIVITY,
        SLIDER_MIN,
        SLIDER_MAX_PERCENT,
        SLIDER_STEP,
        DEFAULT_SENSITIVITY,
        sensitivityValue,
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            sensitivityValue.setText((int) ((Slider) actor).getValue() + "%");
          }
        });
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_AUTOSAVE, true, null);
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_SUBTITLES, true, null);
    return section;
  }

  private Table createControlsSection() {
    Table section = createSection(TranslationKeys.SETTINGS_CONTROLS.get());
    addSelectRow(
        section,
        TranslationKeys.SETTINGS_CONTROLS,
        new TranslationKeys[] {
          TranslationKeys.CONTROLS_DEFAULT,
          TranslationKeys.CONTROLS_WASD,
          TranslationKeys.CONTROLS_ARROWS,
          TranslationKeys.CONTROLS_CUSTOM
        },
        0);
    addSeparator(section);
    TextButton configKeysButton =
        new TextButton(TranslationKeys.SETTINGS_CONFIGURE_KEYS.get(), skin);
    section.add(configKeysButton).colspan(2).fillX().padTop(PADDING).row();
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_VIBRATION, true, null);
    return section;
  }

  private Table createAccessibilitySection() {
    Table section = createSection(TranslationKeys.SETTINGS_ACCESSIBILITY.get());
    addSelectRow(
        section,
        TranslationKeys.SETTINGS_COLORBLIND_MODE,
        new TranslationKeys[] {
          TranslationKeys.COLORBLIND_NONE,
          TranslationKeys.COLORBLIND_PROTANOPIA,
          TranslationKeys.COLORBLIND_DEUTERANOPIA,
          TranslationKeys.COLORBLIND_TRITANOPIA
        },
        0);
    addSeparator(section);
    Label textSizeValue = new Label(DEFAULT_TEXT_SIZE + "%", skin);
    textSizeValue.setColor(GREEN_ACCENT);
    addSliderRow(
        section,
        TranslationKeys.SETTINGS_TEXT_SIZE,
        TEXT_SIZE_MIN,
        TEXT_SIZE_MAX,
        TEXT_SIZE_STEP,
        DEFAULT_TEXT_SIZE,
        textSizeValue,
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            textSizeValue.setText((int) ((Slider) actor).getValue() + "%");
          }
        });
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_HIGH_CONTRAST, false, null);
    addSeparator(section);
    addCheckboxRow(section, TranslationKeys.SETTINGS_REDUCE_MOTION, false, null);
    return section;
  }

  private Table createLanguageSection() {
    Table section = createSection(TranslationKeys.SETTINGS_LANGUAGE.get());
    Label label = new Label(TranslationKeys.SETTINGS_LANGUAGE.get(), skin);
    label.setColor(Color.WHITE);
    section.add(label);
    SelectBox<String> languageSelect = new SelectBox<>(skin);
    languageSelect.setItems(LANGUAGE_ENGLISH, LANGUAGE_SPANISH, LANGUAGE_FRENCH);
    languageSelect.setSelected(resolveCurrentLanguageLabel());
    languageSelect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            LocalizationManager.getInstance()
                .setLanguage(getLanguageCode(languageSelect.getSelected()));
            reloadUiWithLanguage();
          }
        });
    section.add(languageSelect).width(SELECT_WIDTH).row();
    return section;
  }

  private String resolveCurrentLanguageLabel() {
    String currentLanguage = LocalizationManager.getInstance().getCurrentLanguage();
    if (currentLanguage.equals(LANGUAGE_CODE_SPANISH)) {
      return LANGUAGE_SPANISH;
    }
    if (currentLanguage.equals(LANGUAGE_CODE_FRENCH)) {
      return LANGUAGE_FRENCH;
    }
    return LANGUAGE_ENGLISH;
  }

  private String getLanguageCode(String languageName) {
    if (languageName.equals(LANGUAGE_SPANISH)) {
      return LANGUAGE_CODE_SPANISH;
    }
    if (languageName.equals(LANGUAGE_FRENCH)) {
      return LANGUAGE_CODE_FRENCH;
    }
    return LANGUAGE_CODE_ENGLISH;
  }

  private void reloadUiWithLanguage() {
    ScreenManager.getInstance().clearMenuScreenCaches();
    Gdx.app.postRunnable(
        () -> {
          stage.clear();
          createUi();
          showSuccessMessage(TranslationKeys.SETTINGS_APPLIED_MSG.get());
        });
  }

  private Table createActionButtons() {
    Table buttonTable = new Table();
    addActionButton(buttonTable, TranslationKeys.SETTINGS_APPLY.get(), this::saveSettings);
    addActionButton(buttonTable, TranslationKeys.SETTINGS_RESET.get(), this::resetSettings);
    addActionButton(buttonTable, TranslationKeys.SETTINGS_BACK.get(), this::returnToMainMenu);
    return buttonTable;
  }

  private void addActionButton(Table table, String text, Runnable action) {
    TextButton button = createActionButton(text, action);
    UISoundHelper.addButtonSounds(button);
    table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padRight(PADDING);
  }

  private void saveSettings() {
    settings.save();
    showSuccessMessage(TranslationKeys.SETTINGS_APPLIED_MSG.get());
    scheduleMessageReset();
  }

  private void showSuccessMessage(String message) {
    statusLabel.setText(message);
    statusLabel.setColor(GREEN_ACCENT);
  }

  private void scheduleMessageReset() {
    Timer.schedule(
        new Timer.Task() {
          @Override
          public void run() {
            resetStatusMessage();
          }
        },
        STATUS_MESSAGE_DELAY_SECONDS);
  }

  private void resetStatusMessage() {
    statusLabel.setText(TranslationKeys.SETTINGS_READY.get());
    statusLabel.setColor(Color.GRAY);
  }

  private void resetSettings() {
    settings.resetToDefaults();
    refreshUi();
    statusLabel.setText(TranslationKeys.SETTINGS_RESET_MSG.get());
    statusLabel.setColor(Color.YELLOW);
  }

  private void refreshUi() {
    stage.clear();
    createUi();
  }

  private void returnToMainMenu() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU);
  }
}
