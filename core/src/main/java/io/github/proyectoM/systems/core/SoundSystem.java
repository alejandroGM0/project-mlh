package io.github.proyectoM.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.audio.AudioManager;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.sound.SoundCategory;
import io.github.proyectoM.components.sound.SoundEmitterComponent;
import io.github.proyectoM.components.sound.SoundListenerComponent;
import java.util.Locale;

/** Processes sound emitters and routes playback through the audio manager. */
public class SoundSystem extends EntitySystem {
  private static final float MINIMUM_MOVEMENT_SPEED = 0.1f;
  private static final float BASE_FOOTSTEP_INTERVAL_SECONDS = 0.5f;
  private static final float MAX_FOOTSTEP_SPEED_MULTIPLIER = 2f;
  private static final float FOOTSTEP_SPEED_DIVISOR = 2f;
  private static final float MIN_PAN_VALUE = -1f;
  private static final float MAX_PAN_VALUE = 1f;
  private static final float BASE_PITCH = 1f;
  private static final float MIN_AUDIBLE_DISTANCE = 0f;
  private static final String STEP_SOUND_TOKEN = "step";
  private static final String FOOTSTEP_SOUND_TOKEN = "footstep";

  private final AudioManager audioManager;
  private final ComponentMapper<SoundEmitterComponent> soundMapper =
      ComponentMapper.getFor(SoundEmitterComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<SoundListenerComponent> listenerMapper =
      ComponentMapper.getFor(SoundListenerComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final Vector2 tempVector = new Vector2();
  private final Vector2 listenerPosition = new Vector2();

  private ImmutableArray<Entity> soundEmitters;
  private ImmutableArray<Entity> listeners;
  private boolean hasActiveListener;

  public SoundSystem(AudioManager audioManager) {
    this.audioManager = audioManager;
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    soundEmitters =
        engine.getEntitiesFor(
            Family.all(SoundEmitterComponent.class, PositionComponent.class).get());
    listeners =
        engine.getEntitiesFor(
            Family.all(SoundListenerComponent.class, PositionComponent.class).get());
  }

  @Override
  public void update(float deltaTime) {
    updateListenerState();
    processSoundEmitters(deltaTime);
  }

  private void updateListenerState() {
    hasActiveListener = false;
    for (int i = 0; i < listeners.size(); i++) {
      Entity candidate = listeners.get(i);
      SoundListenerComponent listener = listenerMapper.get(candidate);
      if (!listener.active) {
        continue;
      }

      PositionComponent position = positionMapper.get(candidate);
      listenerPosition.set(position.x, position.y);
      hasActiveListener = true;
      return;
    }
  }

  private void processSoundEmitters(float deltaTime) {
    for (int i = 0; i < soundEmitters.size(); i++) {
      Entity entity = soundEmitters.get(i);
      SoundEmitterComponent sound = soundMapper.get(entity);
      if (!sound.shouldPlay) {
        continue;
      }

      PositionComponent position = positionMapper.get(entity);
      if (sound.soundCategory == SoundCategory.AMBIENT) {
        handleAmbientSound(entity, sound, position, deltaTime);
        continue;
      }

      playEmitterSound(sound, position);
      sound.shouldPlay = false;
    }
  }

  private void handleAmbientSound(
      Entity entity, SoundEmitterComponent sound, PositionComponent position, float deltaTime) {
    if (isFootstepSound(sound.soundName)) {
      processFootsteps(entity, sound, position, deltaTime);
      return;
    }

    playEmitterSound(sound, position);
    sound.shouldPlay = false;
  }

  private boolean isFootstepSound(String soundName) {
    String normalizedSoundName = soundName.toLowerCase(Locale.ROOT);
    return normalizedSoundName.contains(STEP_SOUND_TOKEN)
        || normalizedSoundName.contains(FOOTSTEP_SOUND_TOKEN);
  }

  private void processFootsteps(
      Entity entity, SoundEmitterComponent sound, PositionComponent position, float deltaTime) {
    PhysicsComponent physics = physicsMapper.get(entity);
    if (physics == null || physics.body == null) {
      sound.timer = SoundEmitterComponent.DEFAULT_TIMER;
      return;
    }

    float speed = physics.body.getLinearVelocity().len();
    if (speed <= MINIMUM_MOVEMENT_SPEED) {
      sound.timer = SoundEmitterComponent.DEFAULT_TIMER;
      return;
    }

    if (sound.timer <= SoundEmitterComponent.DEFAULT_TIMER) {
      sound.timer = BASE_FOOTSTEP_INTERVAL_SECONDS;
    }

    sound.timer -= deltaTime;
    if (sound.timer > SoundEmitterComponent.DEFAULT_TIMER) {
      return;
    }

    playEmitterSound(sound, position);
    sound.timer = calculateFootstepInterval(speed);
  }

  private float calculateFootstepInterval(float speed) {
    float speedMultiplier = Math.min(speed / FOOTSTEP_SPEED_DIVISOR, MAX_FOOTSTEP_SPEED_MULTIPLIER);
    return BASE_FOOTSTEP_INTERVAL_SECONDS / speedMultiplier;
  }

  private void playEmitterSound(SoundEmitterComponent sound, PositionComponent position) {
    playPositionalSound(sound.soundName, position.x, position.y, sound.volume, sound);
  }

  private void playPositionalSound(
      String soundName, float x, float y, float volume, SoundEmitterComponent emitter) {
    if (!shouldUseSpatialAudio(emitter)) {
      audioManager.playSound(soundName, volume, emitter.soundCategory);
      return;
    }

    tempVector.set(x - listenerPosition.x, y - listenerPosition.y);
    float distance = tempVector.len();
    if (distance > emitter.maxDistance) {
      return;
    }

    float finalVolume = volume * calculateDistanceVolume(distance, emitter.maxDistance);
    float pan = calculatePan(tempVector.x, emitter.maxDistance);
    audioManager.playSound(soundName, finalVolume, BASE_PITCH, pan, emitter.soundCategory);
  }

  private boolean shouldUseSpatialAudio(SoundEmitterComponent emitter) {
    return emitter.is3D && hasActiveListener && emitter.maxDistance > MIN_AUDIBLE_DISTANCE;
  }

  private float calculateDistanceVolume(float distance, float maxDistance) {
    return 1f - (distance / maxDistance);
  }

  private float calculatePan(float xOffset, float maxDistance) {
    return MathUtils.clamp(xOffset / maxDistance, MIN_PAN_VALUE, MAX_PAN_VALUE);
  }
}
