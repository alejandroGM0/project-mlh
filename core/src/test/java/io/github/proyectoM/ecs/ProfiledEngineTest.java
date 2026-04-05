package io.github.proyectoM.ecs;

import com.badlogic.ashley.core.EntitySystem;
import io.github.proyectoM.ecs.ProfiledEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for ProfiledEngine.
 */
class ProfiledEngineTest {

    /**
     * Verifies that profiling enabled measures system and total times.
     */
    @Test
    void profilingEnabledMeasuresSystemAndTotalTimes() {
        ProfiledEngine engine = new ProfiledEngine();
        TrackingSystem system = new TrackingSystem();
        engine.addSystem((EntitySystem)system);
        engine.update(0.016f);
        Assertions.assertEquals(1, system.updateCalls);
        Assertions.assertTrue((engine.getTotalUpdateTime() > 0L ? 1 : 0) != 0);
        Assertions.assertTrue((engine.getPeakTotalTime() >= engine.getTotalUpdateTime() ? 1 : 0) != 0);
        Assertions.assertTrue(engine.getSystemTimes().containsKey(TrackingSystem.class));
        Assertions.assertTrue(engine.getPeakTimes().containsKey(TrackingSystem.class));
        Assertions.assertTrue((engine.getSystemTimeMs(TrackingSystem.class) >= 0.0f ? 1 : 0) != 0);
        Assertions.assertTrue((engine.getTotalUpdateTimeMs() >= 0.0f ? 1 : 0) != 0);
    }

    /**
     * Verifies that profiling disabled still updates systems without recording times.
     */
    @Test
    void profilingDisabledStillUpdatesSystemsWithoutRecordingTimes() {
        ProfiledEngine engine = new ProfiledEngine();
        TrackingSystem system = new TrackingSystem();
        engine.addSystem((EntitySystem)system);
        engine.setProfilingEnabled(false);
        engine.update(0.016f);
        Assertions.assertFalse(engine.isProfilingEnabled());
        Assertions.assertEquals(1, system.updateCalls);
        Assertions.assertEquals(0L, engine.getTotalUpdateTime());
        Assertions.assertEquals(0L, engine.getPeakTotalTime());
        Assertions.assertFalse(engine.getSystemTimes().containsKey(TrackingSystem.class));
        Assertions.assertEquals(0.0f, engine.getSystemTimeMs(TrackingSystem.class));
    }

    /**
     * Verifies that reset peak times clears peaks but keeps latest measurements.
     */
    @Test
    void resetPeakTimesClearsPeaksButKeepsLatestMeasurements() {
        ProfiledEngine engine = new ProfiledEngine();
        TrackingSystem system = new TrackingSystem();
        engine.addSystem((EntitySystem)system);
        engine.update(0.016f);
        Assertions.assertTrue(engine.getPeakTimes().containsKey(TrackingSystem.class));
        engine.resetPeakTimes();
        Assertions.assertEquals(0L, engine.getPeakTotalTime());
        Assertions.assertFalse(engine.getPeakTimes().containsKey(TrackingSystem.class));
        Assertions.assertTrue(engine.getSystemTimes().containsKey(TrackingSystem.class));
    }

    /**
     * Verifies that get system time ms returns zero for unknown system.
     */
    @Test
    void getSystemTimeMsReturnsZeroForUnknownSystem() {
        ProfiledEngine engine = new ProfiledEngine();
        Assertions.assertEquals(0.0f, engine.getSystemTimeMs(TrackingSystem.class));
    }

    private static final class TrackingSystem
    extends EntitySystem {
        private int updateCalls;

        private TrackingSystem() {
        }

        public void update(float deltaTime) {
            ++this.updateCalls;
        }
    }
}