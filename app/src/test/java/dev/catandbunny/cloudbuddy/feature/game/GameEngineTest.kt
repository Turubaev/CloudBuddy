package dev.catandbunny.cloudbuddy.feature.game

import dev.catandbunny.cloudbuddy.core.model.GameMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {
    @Test
    fun classicModeEndsAtBoundary() {
        val engine = GameEngine(width = 1_000f, height = 1_000f, mode = GameMode.CLASSIC)
        var alive = true
        repeat(200) { alive = engine.update(.05f) }
        assertFalse(alive)
    }

    @Test
    fun calmModeNeverEndsAtBoundary() {
        val engine = GameEngine(width = 1_000f, height = 1_000f, mode = GameMode.CALM)
        var alive = true
        repeat(200) { alive = engine.update(.05f) }
        assertTrue(alive)
    }

    @Test
    fun classicSpeedGrowthAcceleratesAndIsCapped() {
        val start = classicSpeedMultiplier(score = 0, elapsedSeconds = 0f)
        val middle = classicSpeedMultiplier(score = 5, elapsedSeconds = 0f)
        val later = classicSpeedMultiplier(score = 10, elapsedSeconds = 0f)

        assertTrue(middle > start)
        assertTrue(later - middle > middle - start)
        assertEquals(3f, classicSpeedMultiplier(score = 1_000, elapsedSeconds = 10_000f), 0f)
    }

    @Test
    fun calmModeKeepsConstantSpeed() {
        val engine = GameEngine(width = 1_000f, height = 1_000f, mode = GameMode.CALM)
        repeat(20) { engine.update(.05f) }
        assertEquals(1f, engine.speedMultiplier(), 0f)
    }
}
