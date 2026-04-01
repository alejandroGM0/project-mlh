package io.github.proyectoM.components.sound;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Describes a sound that an entity can emit. */
public class SoundEmitterComponent implements Component, Poolable {
  public static final String DEFAULT_SOUND_NAME = "";
  public static final float DEFAULT_VOLUME = 1f;
  public static final boolean DEFAULT_SPATIAL_AUDIO = true;
  public static final float DEFAULT_MAX_DISTANCE = 500f;
  public static final boolean DEFAULT_SHOULD_PLAY = false;
  public static final float DEFAULT_TIMER = 0f;

  public SoundCategory soundCategory = SoundCategory.SFX;
  public String soundName = DEFAULT_SOUND_NAME;
  public float volume = DEFAULT_VOLUME;
  public boolean is3D = DEFAULT_SPATIAL_AUDIO;
  public float maxDistance = DEFAULT_MAX_DISTANCE;
  public boolean shouldPlay = DEFAULT_SHOULD_PLAY;
  public float timer = DEFAULT_TIMER;

  public SoundEmitterComponent() {}

  public SoundEmitterComponent(String soundName, SoundCategory category) {
    configure(soundName, category, DEFAULT_VOLUME);
  }

  public SoundEmitterComponent(String soundName, SoundCategory category, float volume) {
    configure(soundName, category, volume);
  }

  public SoundEmitterComponent(
      String soundName, SoundCategory category, float volume, boolean is3D, float maxDistance) {
    configureSpatial(soundName, category, volume, is3D, maxDistance);
  }

  public void play() {
    shouldPlay = true;
  }

  public void stop() {
    shouldPlay = false;
  }

  public SoundEmitterComponent configure(String soundName, SoundCategory category, float volume) {
    this.soundName = soundName;
    soundCategory = category;
    this.volume = volume;
    shouldPlay = true;
    return this;
  }

  public SoundEmitterComponent configureSpatial(
      String soundName, SoundCategory category, float volume, boolean spatial, float distance) {
    configure(soundName, category, volume);
    is3D = spatial;
    maxDistance = distance;
    return this;
  }

  @Override
  public void reset() {
    soundCategory = SoundCategory.SFX;
    soundName = DEFAULT_SOUND_NAME;
    volume = DEFAULT_VOLUME;
    is3D = DEFAULT_SPATIAL_AUDIO;
    maxDistance = DEFAULT_MAX_DISTANCE;
    shouldPlay = DEFAULT_SHOULD_PLAY;
    timer = DEFAULT_TIMER;
  }
}
