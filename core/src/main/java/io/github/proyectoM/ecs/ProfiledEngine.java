package io.github.proyectoM.ecs;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.ObjectMap;

/** Extends Ashley's engine with per-system update timing and peak tracking. */
public class ProfiledEngine extends PooledEngine {
  private static final float NANOS_PER_MILLISECOND = 1_000_000f;
  private static final long RESET_TOTAL_TIME = 0L;
  private static final float NO_MEASURED_TIME_MILLISECONDS = 0f;

  private final ObjectMap<Class<? extends EntitySystem>, Long> systemTimes = new ObjectMap<>();
  private final ObjectMap<Class<? extends EntitySystem>, Long> peakTimes = new ObjectMap<>();

  private long totalUpdateTime = RESET_TOTAL_TIME;
  private long peakTotalTime = RESET_TOTAL_TIME;
  private boolean profilingEnabled = true;

  @Override
  public void update(float deltaTime) {
    if (!profilingEnabled) {
      super.update(deltaTime);
      return;
    }

    measureFullUpdate(deltaTime);
  }

  public ObjectMap<Class<? extends EntitySystem>, Long> getSystemTimes() {
    return systemTimes;
  }

  public ObjectMap<Class<? extends EntitySystem>, Long> getPeakTimes() {
    return peakTimes;
  }

  public long getTotalUpdateTime() {
    return totalUpdateTime;
  }

  public long getPeakTotalTime() {
    return peakTotalTime;
  }

  public void resetPeakTimes() {
    peakTimes.clear();
    peakTotalTime = RESET_TOTAL_TIME;
  }

  public void setProfilingEnabled(boolean enabled) {
    profilingEnabled = enabled;
  }

  public boolean isProfilingEnabled() {
    return profilingEnabled;
  }

  public float getTotalUpdateTimeMs() {
    return totalUpdateTime / NANOS_PER_MILLISECOND;
  }

  public float getSystemTimeMs(Class<? extends EntitySystem> systemClass) {
    Long time = systemTimes.get(systemClass);
    if (time == null) {
      return NO_MEASURED_TIME_MILLISECONDS;
    }
    return time / NANOS_PER_MILLISECOND;
  }

  private void measureFullUpdate(float deltaTime) {
    long startTotalTime = System.nanoTime();
    measureSystems(deltaTime);
    totalUpdateTime = System.nanoTime() - startTotalTime;
    peakTotalTime = Math.max(peakTotalTime, totalUpdateTime);
  }

  private void measureSystems(float deltaTime) {
    ImmutableArray<EntitySystem> systems = getSystems();
    for (int index = 0; index < systems.size(); index++) {
      EntitySystem system = systems.get(index);
      if (system.checkProcessing()) {
        measureSystemUpdate(system, deltaTime);
      }
    }
  }

  private void measureSystemUpdate(EntitySystem system, float deltaTime) {
    long startTime = System.nanoTime();
    system.update(deltaTime);
    long elapsedTime = System.nanoTime() - startTime;

    Class<? extends EntitySystem> systemClass = system.getClass();
    systemTimes.put(systemClass, elapsedTime);
    updateSystemPeak(systemClass, elapsedTime);
  }

  private void updateSystemPeak(Class<? extends EntitySystem> systemClass, long elapsedTime) {
    Long currentPeak = peakTimes.get(systemClass);
    if (currentPeak == null || elapsedTime > currentPeak) {
      peakTimes.put(systemClass, elapsedTime);
    }
  }
}
