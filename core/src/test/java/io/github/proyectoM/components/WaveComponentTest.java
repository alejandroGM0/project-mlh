package io.github.proyectoM.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.proyectoM.components.game.WaveComponent;
import org.junit.jupiter.api.Test;

class WaveComponentTest {

  /**
   * Verifies that a newly created WaveComponent has the correct default values.
   */
  @Test
  void newComponent_hasCorrectDefaults() {
    WaveComponent wave = new WaveComponent();

    assertEquals(0, wave.currentWave);
    assertEquals(0f, wave.timeSinceLastWave);
    assertEquals(WaveComponent.DEFAULT_TIME_BETWEEN_WAVES, wave.timeBetweenWaves);
    assertEquals(0, wave.enemiesToSpawn);
    assertFalse(wave.isWaveInProgress);
  }

  /**
   * Verifies that the wave interval time constant has the expected value.
   */
  @Test
  void defaultTimeBetweenWaves_isFiveSeconds() {
    assertEquals(5f, WaveComponent.DEFAULT_TIME_BETWEEN_WAVES);
  }

  /**
   * Verifies that reset() restores all fields to their initial values.
   */
  @Test
  void reset_restoresAllDefaults() {
    WaveComponent wave = new WaveComponent();
    wave.currentWave = 15;
    wave.timeSinceLastWave = 3.5f;
    wave.timeBetweenWaves = 10f;
    wave.enemiesToSpawn = 50;
    wave.isWaveInProgress = true;

    wave.reset();

    assertEquals(0, wave.currentWave);
    assertEquals(0f, wave.timeSinceLastWave);
    assertEquals(WaveComponent.DEFAULT_TIME_BETWEEN_WAVES, wave.timeBetweenWaves);
    assertEquals(0, wave.enemiesToSpawn);
    assertFalse(wave.isWaveInProgress);
  }

  /**
   * Verifies that a new wave start can be simulated.
   */
  @Test
  void waveTransition_canStartNewWave() {
    WaveComponent wave = new WaveComponent();

    wave.currentWave = 1;
    wave.enemiesToSpawn = 10;
    wave.isWaveInProgress = true;

    assertEquals(1, wave.currentWave);
    assertEquals(10, wave.enemiesToSpawn);
  }

  /**
   * Verifies that the inter-wave timer progression can be simulated.
   */
  @Test
  void timeBetweenWaves_canBeTracked() {
    WaveComponent wave = new WaveComponent();

    wave.timeSinceLastWave += 2.0f;
    wave.timeSinceLastWave += 1.5f;

    assertEquals(3.5f, wave.timeSinceLastWave, 0.001f);
  }
}