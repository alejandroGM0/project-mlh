package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.proyectoM.Main;
import io.github.proyectoM.audio.AudioManager;
import io.github.proyectoM.registry.BulletRegistry;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.EnemyRegistry;
import io.github.proyectoM.registry.MapRegistry;
import io.github.proyectoM.registry.VisualAssetRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.resources.Assets;

/** Displays loading progress while maps, skins, audio, and visual assets are initialized. */
public class LoadingScreen implements Screen {
  private static final float BAR_WIDTH = 600f;
  private static final float BAR_HEIGHT = 30f;
  private static final float BAR_BORDER_WIDTH = 3f;
  private static final float VIEWPORT_CENTER_RATIO = 0.5f;
  private static final float BAR_CENTER_Y_RATIO = 0.35f;
  private static final float PROGRESS_TEXT_Y_OFFSET = 60f;
  private static final float FONT_SCALE = 1.5f;
  private static final float SMOOTH_PROGRESS_ALPHA = 0.1f;
  private static final float GLOW_BASE_ALPHA = 0.3f;
  private static final float GLOW_PULSE_RANGE = 0.2f;
  private static final float GLOW_PULSE_SPEED = 4f;
  private static final float GLOW_HEIGHT_RATIO = 0.5f;
  private static final float DEFAULT_LINE_WIDTH = 1f;
  private static final int GRADIENT_STEPS = 100;
  private static final int PERCENTAGE_MULTIPLIER = 100;
  private static final String PERCENTAGE_FORMAT = "%d%%";

  private static final float BACKGROUND_DARKNESS_BASE = 0.7f;
  private static final float BACKGROUND_DARKNESS_RANGE = 0.3f;

  private static final Color BACKGROUND_COLOR = new Color(0.05f, 0.08f, 0.05f, 1f);
  private static final Color BAR_BACKGROUND_COLOR = new Color(0.1f, 0.15f, 0.1f, 1f);
  private static final Color BAR_BORDER_COLOR = new Color(0.3f, 0.5f, 0.3f, 1f);
  private static final Color BAR_FILL_COLOR = new Color(0.4f, 0.7f, 0.4f, 1f);
  private static final Color BAR_GLOW_COLOR = new Color(0.6f, 0.9f, 0.6f, 0.5f);
  private static final Color TEXT_COLOR = new Color(0.7f, 0.9f, 0.7f, 1f);

  private static final String TITLE_TEXT = "NECROPOINT";
  private static final String TITLE_FONT_PATH = "skins/pruebaInterfaz/Silkscreen-Regular.ttf";
  private static final int TITLE_FONT_SIZE = 72;
  private static final float TITLE_BORDER_WIDTH = 3f;
  private static final float TITLE_Y_RATIO = 0.65f;
  private static final float TITLE_PULSE_SPEED = 2f;
  private static final float TITLE_PULSE_AMPLITUDE = 0.05f;
  private static final Color TITLE_COLOR = new Color(0.5f, 0.8f, 0.5f, 1f);
  private static final Color TITLE_BORDER_COLOR = new Color(0.2f, 0.4f, 0.2f, 1f);
  private static final Color TITLE_GLOW_COLOR_LOADING = new Color(0.5f, 0.8f, 0.5f, 0.15f);
  private static final float TITLE_GLOW_OFFSET = 3f;

  private static final float MESSAGE_Y_OFFSET = -50f;
  private static final float MESSAGE_FADE_SPEED = 3f;
  private static final float MESSAGE_FADE_BASE = 0.7f;
  private static final float MESSAGE_FADE_RANGE = 0.3f;

  private static final float BRACKET_LENGTH = 25f;
  private static final float BRACKET_THICKNESS = 2f;
  private static final float BRACKET_MARGIN = 20f;

  private static final float SHIMMER_SPEED = 0.8f;
  private static final float SHIMMER_WIDTH_RATIO = 0.15f;
  private static final Color SHIMMER_COLOR = new Color(0.8f, 1f, 0.8f, 0.25f);

  private static final float LOADING_PHASE_1 = 0.167f;
  private static final float LOADING_PHASE_2 = 0.333f;
  private static final float LOADING_PHASE_3 = 0.500f;
  private static final float LOADING_PHASE_4 = 0.667f;
  private static final float LOADING_PHASE_5 = 0.833f;

  private static final String[] LOADING_MESSAGES = {
    "Waking up the undead...",
    "Loading arsenal...",
    "Preparing the apocalypse...",
    "Initializing systems...",
    "Generating maps...",
    "Activating enemy AI..."
  };

  private static final String[] MAPS = {"maps/newIsometricMap.tmx"};
  private static final String[] UI_SKINS = {"skins/pruebaInterfaz/pruebaInterfaz.json"};

  private final ScreenViewport viewport;
  private final SpriteBatch batch;
  private final BitmapFont font;
  private final GlyphLayout glyphLayout;
  private final ShapeRenderer shapeRenderer;
  private final BitmapFont titleFont;
  private final GlyphLayout titleLayout;

  private float progress = 0f;
  private float smoothProgress = 0f;
  private boolean finished = false;
  private float animationTime = 0f;

  /** Creates the loading screen and queues the assets needed before the main menu opens. */
  public LoadingScreen(Main game) {
    OrthographicCamera camera = new OrthographicCamera();
    this.viewport = new ScreenViewport(camera);
    this.batch = game.getBatch();
    this.font = new BitmapFont();
    this.font.getData().setScale(FONT_SCALE);
    this.glyphLayout = new GlyphLayout();
    this.shapeRenderer = game.getShapeRenderer();
    this.titleFont = createTitleFont();
    this.titleLayout = new GlyphLayout();

    loadEntityRegistries();
    loadMaps();
    loadUiSkins();
    loadAudioAssets();
    initializeAtlases();
  }

  private void loadEntityRegistries() {
    WeaponRegistry.getInstance().load();
    BulletRegistry.getInstance().load();
    EnemyRegistry.getInstance().load();
    CompanionRegistry.getInstance().load();
    MapRegistry.getInstance().load();
  }

  private void loadMaps() {
    for (String map : MAPS) {
      Assets.getManager().load(map, TiledMap.class);
    }
  }

  private void loadUiSkins() {
    for (String skin : UI_SKINS) {
      Assets.getManager().load(skin, Skin.class);
    }
  }

  private void loadAudioAssets() {
    AudioManager.getInstance().loadAudioAssets();
  }

  private void initializeAtlases() {
    VisualAssetRegistry.collectAndRequestAtlases(Assets.getManager());
    loadBulletSprites();
  }

  private void loadBulletSprites() {
    for (String spritePath : BulletRegistry.getInstance().getAllSpritePaths()) {
      Assets.getManager().load(spritePath, Texture.class);
    }
  }

  private void finalizeAnimations() {
    VisualAssetRegistry.loadAllAnimations(Assets.getManager());
  }

  @Override
  public void show() {
    viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
  }

  private void drawBackground() {
    Gdx.gl.glClearColor(
        BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    float viewportHeight = viewport.getWorldHeight();
    float viewportWidth = viewport.getWorldWidth();
    float stepHeight = viewportHeight / GRADIENT_STEPS;
    for (int stepIndex = 0; stepIndex < GRADIENT_STEPS; stepIndex++) {
      float ratio = stepIndex / (float) GRADIENT_STEPS;
      float darkness = BACKGROUND_DARKNESS_BASE + (ratio * BACKGROUND_DARKNESS_RANGE);
      shapeRenderer.setColor(
          BACKGROUND_COLOR.r * darkness,
          BACKGROUND_COLOR.g * darkness,
          BACKGROUND_COLOR.b * darkness,
          1f);
      shapeRenderer.rect(0f, stepIndex * stepHeight, viewportWidth, stepHeight);
    }

    shapeRenderer.end();
  }

  private void drawProgressBar() {
    float centerX = viewport.getWorldWidth() * VIEWPORT_CENTER_RATIO;
    float centerY = viewport.getWorldHeight() * BAR_CENTER_Y_RATIO;
    float barX = centerX - (BAR_WIDTH * VIEWPORT_CENTER_RATIO);
    float barY = centerY - (BAR_HEIGHT * VIEWPORT_CENTER_RATIO);

    smoothProgress = Interpolation.smooth.apply(smoothProgress, progress, SMOOTH_PROGRESS_ALPHA);

    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
    shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);

    float fillWidth = BAR_WIDTH * smoothProgress;
    shapeRenderer.setColor(BAR_FILL_COLOR);
    shapeRenderer.rect(barX, barY, fillWidth, BAR_HEIGHT);

    float glowAlpha =
        GLOW_BASE_ALPHA + (MathUtils.sin(animationTime * GLOW_PULSE_SPEED) * GLOW_PULSE_RANGE);
    shapeRenderer.setColor(BAR_GLOW_COLOR.r, BAR_GLOW_COLOR.g, BAR_GLOW_COLOR.b, glowAlpha);
    shapeRenderer.rect(barX, barY, fillWidth, BAR_HEIGHT * GLOW_HEIGHT_RATIO);
    shapeRenderer.end();

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(BAR_BORDER_WIDTH);
    shapeRenderer.setColor(BAR_BORDER_COLOR);
    shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
    shapeRenderer.end();
    Gdx.gl.glLineWidth(DEFAULT_LINE_WIDTH);
  }

  private void drawProgressText() {
    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();
    font.setColor(TEXT_COLOR);

    String percentText =
        String.format(PERCENTAGE_FORMAT, Math.round(progress * PERCENTAGE_MULTIPLIER));
    glyphLayout.setText(font, percentText);
    float percentX = (viewport.getWorldWidth() - glyphLayout.width) * VIEWPORT_CENTER_RATIO;
    float percentY = (viewport.getWorldHeight() * BAR_CENTER_Y_RATIO) + PROGRESS_TEXT_Y_OFFSET;
    font.draw(batch, glyphLayout, percentX, percentY);

    batch.end();
  }

  /** Generates the title font using FreeType from the Silkscreen TTF file. */
  private BitmapFont createTitleFont() {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal(TITLE_FONT_PATH));
    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = TITLE_FONT_SIZE;
    parameter.color = TITLE_COLOR;
    parameter.borderWidth = TITLE_BORDER_WIDTH;
    parameter.borderColor = TITLE_BORDER_COLOR;
    BitmapFont generated = generator.generateFont(parameter);
    generator.dispose();
    return generated;
  }

  /** Draws the game title with a pulsing scale animation and a subtle glow shadow. */
  private void drawTitle() {
    float pulse = 1f + MathUtils.sin(animationTime * TITLE_PULSE_SPEED) * TITLE_PULSE_AMPLITUDE;
    titleFont.getData().setScale(pulse);
    titleLayout.setText(titleFont, TITLE_TEXT);

    float titleX = (viewport.getWorldWidth() - titleLayout.width) * VIEWPORT_CENTER_RATIO;
    float titleY =
        viewport.getWorldHeight() * TITLE_Y_RATIO + titleLayout.height * VIEWPORT_CENTER_RATIO;

    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();

    titleFont.setColor(TITLE_GLOW_COLOR_LOADING);
    titleFont.draw(batch, TITLE_TEXT, titleX + TITLE_GLOW_OFFSET, titleY - TITLE_GLOW_OFFSET);

    titleFont.setColor(TITLE_COLOR);
    titleFont.draw(batch, TITLE_TEXT, titleX, titleY);

    titleFont.getData().setScale(1f);
    batch.end();
  }

  /** Draws a contextual loading message below the progress bar with a fading animation. */
  private void drawLoadingMessage() {
    String message = getLoadingMessage(progress);
    float alpha =
        MESSAGE_FADE_BASE + MathUtils.sin(animationTime * MESSAGE_FADE_SPEED) * MESSAGE_FADE_RANGE;

    batch.setProjectionMatrix(viewport.getCamera().combined);
    batch.begin();
    font.setColor(TEXT_COLOR.r, TEXT_COLOR.g, TEXT_COLOR.b, alpha);

    glyphLayout.setText(font, message);
    float messageX = (viewport.getWorldWidth() - glyphLayout.width) * VIEWPORT_CENTER_RATIO;
    float messageY = viewport.getWorldHeight() * BAR_CENTER_Y_RATIO + MESSAGE_Y_OFFSET;
    font.draw(batch, glyphLayout, messageX, messageY);

    batch.end();
  }

  /**
   * Returns a loading phase message based on the current progress value.
   *
   * @param currentProgress the current loading progress between 0 and 1
   * @return the corresponding phase message string
   */
  private String getLoadingMessage(float currentProgress) {
    if (currentProgress < LOADING_PHASE_1) {
      return LOADING_MESSAGES[0];
    }
    if (currentProgress < LOADING_PHASE_2) {
      return LOADING_MESSAGES[1];
    }
    if (currentProgress < LOADING_PHASE_3) {
      return LOADING_MESSAGES[2];
    }
    if (currentProgress < LOADING_PHASE_4) {
      return LOADING_MESSAGES[3];
    }
    if (currentProgress < LOADING_PHASE_5) {
      return LOADING_MESSAGES[4];
    }
    return LOADING_MESSAGES[5];
  }

  /** Draws decorative L-shaped corner brackets around the progress bar area. */
  private void drawCornerBrackets() {
    float centerX = viewport.getWorldWidth() * VIEWPORT_CENTER_RATIO;
    float centerY = viewport.getWorldHeight() * BAR_CENTER_Y_RATIO;
    float barX = centerX - (BAR_WIDTH * VIEWPORT_CENTER_RATIO);
    float barY = centerY - (BAR_HEIGHT * VIEWPORT_CENTER_RATIO);

    float left = barX - BRACKET_MARGIN;
    float right = barX + BAR_WIDTH + BRACKET_MARGIN;
    float bottom = barY - BRACKET_MARGIN;
    float top = barY + BAR_HEIGHT + BRACKET_MARGIN;

    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(BAR_BORDER_COLOR);

    shapeRenderer.rect(left, top - BRACKET_LENGTH, BRACKET_THICKNESS, BRACKET_LENGTH);
    shapeRenderer.rect(left, top - BRACKET_THICKNESS, BRACKET_LENGTH, BRACKET_THICKNESS);

    shapeRenderer.rect(
        right - BRACKET_THICKNESS, top - BRACKET_LENGTH, BRACKET_THICKNESS, BRACKET_LENGTH);
    shapeRenderer.rect(
        right - BRACKET_LENGTH, top - BRACKET_THICKNESS, BRACKET_LENGTH, BRACKET_THICKNESS);

    shapeRenderer.rect(left, bottom, BRACKET_THICKNESS, BRACKET_LENGTH);
    shapeRenderer.rect(left, bottom, BRACKET_LENGTH, BRACKET_THICKNESS);

    shapeRenderer.rect(right - BRACKET_THICKNESS, bottom, BRACKET_THICKNESS, BRACKET_LENGTH);
    shapeRenderer.rect(right - BRACKET_LENGTH, bottom, BRACKET_LENGTH, BRACKET_THICKNESS);

    shapeRenderer.end();
  }

  /** Draws a shimmer highlight that travels across the filled portion of the progress bar. */
  private void drawBarShimmer() {
    float centerX = viewport.getWorldWidth() * VIEWPORT_CENTER_RATIO;
    float centerY = viewport.getWorldHeight() * BAR_CENTER_Y_RATIO;
    float barX = centerX - (BAR_WIDTH * VIEWPORT_CENTER_RATIO);
    float barY = centerY - (BAR_HEIGHT * VIEWPORT_CENTER_RATIO);
    float fillWidth = BAR_WIDTH * smoothProgress;

    if (fillWidth <= 0) {
      return;
    }

    float shimmerProgress = (animationTime * SHIMMER_SPEED) % 1f;
    float shimmerWidth = fillWidth * SHIMMER_WIDTH_RATIO;
    float shimmerX = barX + shimmerProgress * fillWidth;
    float clampedWidth = Math.min(shimmerWidth, barX + fillWidth - shimmerX);

    if (clampedWidth <= 0) {
      return;
    }

    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(SHIMMER_COLOR);
    shapeRenderer.rect(shimmerX, barY, clampedWidth, BAR_HEIGHT);
    shapeRenderer.end();
  }

  @Override
  public void render(float delta) {
    animationTime += delta;

    if (!finished) {
      if (Assets.getManager().update()) {
        finalizeAnimations();
        finished = true;
        ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU);
        return;
      }
      progress = Assets.getManager().getProgress();
    }

    viewport.apply();
    drawBackground();
    drawTitle();
    drawCornerBrackets();
    drawProgressBar();
    drawBarShimmer();
    drawProgressText();
    drawLoadingMessage();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    font.dispose();
    titleFont.dispose();
  }
}
