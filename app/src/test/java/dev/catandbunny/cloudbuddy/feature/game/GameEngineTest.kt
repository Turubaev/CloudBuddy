package dev.catandbunny.cloudbuddy.feature.game

import dev.catandbunny.cloudbuddy.core.model.GameMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
}
