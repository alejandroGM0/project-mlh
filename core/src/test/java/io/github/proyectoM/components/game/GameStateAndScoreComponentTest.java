package io.github.proyectoM.components.game;

import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.ScoreComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for GameStateComponent and ScoreComponent.
 */
class GameStateAndScoreComponentTest {

    /**
     * Verifies that game state reset restores running state.
     */
    @Test
    void gameStateResetRestoresRunningState() {
        GameStateComponent component = new GameStateComponent();
        component.currentState = GameStateComponent.State.GAME_OVER;
        component.reset();
        Assertions.assertEquals(GameStateComponent.State.RUNNING, component.currentState);
    }

    /**
     * Verifies that score reset clears all tracked counters.
     */
    @Test
    void scoreResetClearsAllTrackedCounters() {
        ScoreComponent component = new ScoreComponent();
        component.score = 10;
        component.enemiesKilled = 5;
        component.timeSurvived = 20.0f;
        component.finalWave = 3;
        component.reset();
        Assertions.assertEquals(0, component.score);
        Assertions.assertEquals(0, component.enemiesKilled);
        Assertions.assertEquals(0.0f, component.timeSurvived);
        Assertions.assertEquals(0, component.finalWave);
    }
}