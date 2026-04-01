package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.proyectoM.Main;
import io.github.proyectoM.resources.Assets;
import io.github.proyectoM.ui.background.BackgroundRenderer;

/** Shared foundation for every menu screen: stage, skin, background, and title-font lifecycle. */
public abstract class AbstractMenuScreen implements Screen {
  protected static final String UI_SKIN_PATH = "skins/pruebaInterfaz/pruebaInterfaz.json";
  protected static final String TITLE_FONT_PATH = "skins/pruebaInterfaz/Silkscreen-Regular.ttf";

  protected final Main game;
  protected Stage stage;
  protected Skin skin;
  protected BackgroundRenderer backgroundRenderer;
  protected BitmapFont titleFont;
  protected LabelStyle titleStyle;
  protected Label titleLabel;

  protected AbstractMenuScreen(Main game) {
    this.game = game;
  }

  @Override
  public final void show() {
    initializeStage();
    buildUi();
  }

  /** Called after stage and skin are ready. Subclass builds its entire UI here. */
  protected abstract void buildUi();

  private void initializeStage() {
    stage = game.getMenuStage();
    stage.clear();
    stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    Gdx.input.setInputProcessor(stage);
    ensureSkinLoaded();
    skin = Assets.getManager().get(UI_SKIN_PATH, Skin.class);
  }

  private void ensureSkinLoaded() {
    if (Assets.getManager().isLoaded(UI_SKIN_PATH, Skin.class)) {
      return;
    }
    Assets.getManager().load(UI_SKIN_PATH, Skin.class);
    Assets.getManager().finishLoading();
  }

  @Override
  public void render(float delta) {
    clearScreen();
    renderBackground(delta);
    stage.act(delta);
    stage.draw();
  }

  /** Clears the OpenGL framebuffer. Override for a different clear color. */
  protected void clearScreen() {
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  /** Updates and renders the background. Override only if custom rendering is needed. */
  protected void renderBackground(float delta) {
    if (backgroundRenderer != null) {
      backgroundRenderer.update(delta);
      backgroundRenderer.render();
    }
  }

  @Override
  public void resize(int width, int height) {
    if (stage == null) {
      return;
    }
    stage.getViewport().update(width, height, true);
    onResize(width, height);
  }

  /** Called after the viewport is updated during a resize. Override for screen-specific logic. */
  protected void onResize(int width, int height) {}

  @Override
  public void dispose() {
    if (stage != null) {
      stage.clear();
    }
    disposeTitleFont();
    if (backgroundRenderer != null) {
      backgroundRenderer.dispose();
      backgroundRenderer = null;
    }
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  /** Recreates the animated background renderer from the current stage camera and viewport. */
  protected void recreateBackgroundRenderer() {
    if (backgroundRenderer != null) {
      backgroundRenderer.dispose();
    }
    backgroundRenderer =
        new BackgroundRenderer(
            (OrthographicCamera) stage.getCamera(),
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
  }

  /**
   * Generates a title font using FreeType with the given parameters.
   *
   * @param screenPixelHeight current screen height in pixels
   * @param sizeRatio font size as a fraction of screen height
   * @param minSize minimum font size in pixels
   * @param color font and label-style color
   * @param borderWidth outline width; zero for no border
   * @param borderColor outline color; ignored when borderWidth is zero
   * @param useMipMaps whether to enable mip-map generation and filtering
   */
  protected void generateTitleFont(
      int screenPixelHeight,
      float sizeRatio,
      int minSize,
      Color color,
      float borderWidth,
      Color borderColor,
      boolean useMipMaps) {
    disposeTitleFont();
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal(TITLE_FONT_PATH));
    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = Math.max(minSize, (int) (screenPixelHeight * sizeRatio));
    parameter.color = color;
    if (borderWidth > 0) {
      parameter.borderWidth = borderWidth;
      parameter.borderColor = borderColor;
    }
    if (useMipMaps) {
      parameter.genMipMaps = true;
      parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
      parameter.magFilter = Texture.TextureFilter.Linear;
    }
    titleFont = generator.generateFont(parameter);
    if (useMipMaps) {
      titleFont
          .getRegion()
          .getTexture()
          .setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
    }
    titleStyle = new LabelStyle(titleFont, color);
    generator.dispose();
    if (titleLabel != null) {
      titleLabel.setStyle(titleStyle);
    }
  }

  /** Disposes the title font if it exists. Safe to call when titleFont is null. */
  protected void disposeTitleFont() {
    if (titleFont != null) {
      titleFont.dispose();
      titleFont = null;
    }
  }

  /** Creates a text button that runs the given action when clicked. */
  protected TextButton createActionButton(String text, Runnable action) {
    TextButton button = new TextButton(text, skin);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }
}
