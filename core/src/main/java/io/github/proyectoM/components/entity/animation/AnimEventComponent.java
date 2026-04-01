package io.github.proyectoM.components.entity.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Stores frame-based animation event definitions and the events fired this update. */
public class AnimEventComponent implements Component, Poolable {
  public final Map<AnimEventType, Integer> eventFrames = new EnumMap<>(AnimEventType.class);
  public final List<AnimEventType> triggeredEvents = new ArrayList<>();
  public boolean endTriggered = false;

  public void defineEvent(AnimEventType type, int frameIndex) {
    eventFrames.put(type, frameIndex);
  }

  public void clearTriggered() {
    triggeredEvents.clear();
    endTriggered = false;
  }

  public boolean hasEvent(AnimEventType type) {
    if (type == AnimEventType.END) {
      return endTriggered;
    }
    return triggeredEvents.contains(type);
  }

  @Override
  public void reset() {
    eventFrames.clear();
    triggeredEvents.clear();
    endTriggered = false;
  }

  /** Enumerates the event hooks animation-driven gameplay systems can consume. */
  public enum AnimEventType {
    HIT_FRAME,
    SHOOT_FRAME,
    FOOTSTEP,
    SOUND_CUE,
    VFX_SPAWN,
    END
  }
}
