package io.github.proyectoM.effects;

import io.github.proyectoM.effects.DustParticle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for DustParticle lifecycle and movement.
 */
class DustParticleTest {

    /**
     * Verifies that update advances particle and applies fade and damping.
     */
    @Test
    void updateAdvancesParticleAndAppliesFadeAndDamping() {
        DustParticle particle = new DustParticle(0.0f, 0.0f);
        particle.velocity.set(10.0f, 20.0f);
        particle.maxLifetime = 1.0f;
        particle.lifetime = 1.0f;
        particle.size = 4.0f;
        boolean alive = particle.update(0.25f);
        Assertions.assertTrue(alive);
        Assertions.assertEquals(2.5f, particle.position.x, 1.0E-4f);
        Assertions.assertEquals(5.0f, particle.position.y, 1.0E-4f);
        Assertions.assertEquals(5.0f, particle.velocity.x, 1.0E-4f);
        Assertions.assertEquals(10.0f, particle.velocity.y, 1.0E-4f);
        Assertions.assertEquals(0.45f, particle.alpha, 1.0E-4f);
        Assertions.assertEquals(3.0f, particle.size, 1.0E-4f);
    }

    /**
     * Verifies that update returns false when lifetime expires.
     */
    @Test
    void updateReturnsFalseWhenLifetimeExpires() {
        DustParticle particle = new DustParticle(0.0f, 0.0f);
        particle.lifetime = 0.1f;
        boolean alive = particle.update(0.1f);
        Assertions.assertFalse(alive);
    }
}