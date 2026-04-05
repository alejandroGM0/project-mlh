package io.github.proyectoM.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.proyectoM.components.game.ScoreComponent;
import org.junit.jupiter.api.Test;

class ScoreComponentTest {

  /**
   * Verifies that a newly created ScoreComponent has all fields at zero.
   */
  @Test
  void newComponent_allFieldsAreZero() {
    ScoreComponent score = new ScoreComponent();

    assertEquals(0, score.score);
    assertEquals(0, score.enemiesKilled);
    assertEquals(0f, score.timeSurvived);
    assertEquals(0, score.finalWave);
  }

  /**
   * Verifies that reset() restores all fields to zero.
   */
  @Test
  void reset_restoresAllFieldsToZero() {
    ScoreComponent score = new ScoreComponent();
    score.score = 1000;
    score.enemiesKilled = 50;
    score.timeSurvived = 120.5f;
    score.finalWave = 10;

    score.reset();

    assertEquals(0, score.score);
    assertEquals(0, score.enemiesKilled);
    assertEquals(0f, score.timeSurvived);
    assertEquals(0, score.finalWave);
  }

  /**
   * Verifies that statistics can be accumulated incrementally.
   */
  @Test
  void statistics_canBeAccumulated() {
    ScoreComponent score = new ScoreComponent();

    score.score += 100;
    score.score += 200;
    score.enemiesKilled += 1;
    score.enemiesKilled += 1;

    assertEquals(300, score.score);
    assertEquals(2, score.enemiesKilled);
  }
}