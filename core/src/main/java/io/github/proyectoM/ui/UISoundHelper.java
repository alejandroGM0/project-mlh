package io.github.proyectoM.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import io.github.proyectoM.audio.AudioManager;
import io.github.proyectoM.components.sound.SoundCategory;

/** A helper for managing UI sounds in a centralized way. */
public class UISoundHelper {

  // TODO(ui-sounds): Replace pistol_fire with a real UI hover sound
  private static final String BUTTON_HOVER_SOUND = "pistol_fire";
  // TODO(ui-sounds): Replace rifle_fire with a real UI click sound
  private static final String BUTTON_CLICK_SOUND = "rifle_fire";
  private static final float UI_SOUND_VOLUME = 0.15f;

  /**
   * Adds hover and click sounds to a button.
   *
   * @param button The button to add the sounds to.
   */
  public static void addButtonSounds(Button button) {
    if (button == null) {
      return;
    }

    button.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer == -1) {
              playUISound(BUTTON_HOVER_SOUND, UI_SOUND_VOLUME * 0.3f);
            }
          }

          @Override
          public boolean touchDown(
              InputEvent event, float x, float y, int pointer, int buttonIndex) {
            playUISound(BUTTON_CLICK_SOUND, UI_SOUND_VOLUME);
            return false;
          }
        });
  }

  /**
   * Plays a UI sound.
   *
   * @param soundName The name of the sound file.
   * @param volume The volume (0.0 - 1.0).
   */
  private static void playUISound(String soundName, float volume) {
    try {
      AudioManager.getInstance().playSound(soundName, volume, SoundCategory.UI);
    } catch (Exception e) {
      Gdx.app.debug("UISoundHelper", "Could not play UI sound: " + e.getMessage());
    }
  }
}
