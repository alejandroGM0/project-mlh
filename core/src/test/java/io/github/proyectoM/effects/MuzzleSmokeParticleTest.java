package io.github.proyectoM.effects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for MuzzleSmokeParticle smoke and spark behavior.
 */
class MuzzleSmokeParticleTest {

    /**
     * Verifies that update fades and grows smoke particles.
     */
    @Test
    void updateFadesAndGrowsSmokeParticles() {
        MuzzleSmokeParticle particle = new MuzzleSmokeParticle(0.0f, 0.0f, 0.0f, false);
        particle.velocity.set(10.0f, 20.0f);
        particle.maxLifetime = 1.0f;
        particle.lifetime = 1.0f;
        particle.size = 5.0f;
        boolean alive = particle.update(0.2f);
        Assertions.assertTrue(alive);
        Assertions.assertEquals(2.0f, particle.position.x, 1.0E-4f);
        Assertions.assertEquals(4.0f, particle.position.y, 1.0E-4f);
        Assertions.assertEquals(4.0f, particle.velocity.x, 1.0E-4f);
        Assertions.assertEquals(8.0f, particle.velocity.y, 1.0E-4f);
        Assertions.assertEquals(0.32f, particle.alpha, 1.0E-4f);
        Assertions.assertEquals(7.0f, particle.size, 1.0E-4f);
    }

    /**
     * Verifies that update fades spark particles without growing them.
     */
    @Test
    void updateFadesSparkParticlesWithoutGrowingThem() {
        MuzzleSmokeParticle particle = new MuzzleSmokeParticle(0.0f, 0.0f, 0.0f, true);
        particle.velocity.set(12.0f, 6.0f);
        particle.maxLifetime = 1.0f;
        particle.lifetime = 1.0f;
        particle.size = 2.0f;
        boolean alive = particle.update(0.25f);
        Assertions.assertTrue(alive);
        Assertions.assertEquals(3.0f, particle.position.x, 1.0E-4f);
        Assertions.assertEquals(1.5f, particle.position.y, 1.0E-4f);
        Assertions.assertEquals(3.0f, particle.velocity.x, 1.0E-4f);
        Assertions.assertEquals(1.5f, particle.velocity.y, 1.0E-4f);
        Assertions.assertEquals(0.75f, particle.alpha, 1.0E-4f);
        Assertions.assertEquals(2.0f, particle.size, 1.0E-4f);
    }

    /**
     * Verifies that update returns false when particle lifetime expires.
     */
    @Test
    void updateReturnsFalseWhenParticleLifetimeExpires() {
        MuzzleSmokeParticle particle = new MuzzleSmokeParticle(0.0f, 0.0f, 0.0f, true);
        particle.lifetime = 0.05f;
        boolean alive = particle.update(0.05f);
        Assertions.assertFalse(alive);
    }
}