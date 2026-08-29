package dev.catandbunny.cloudbuddy.feature.game

import dev.catandbunny.cloudbuddy.core.model.GameMode
import kotlin.random.Random
import kotlin.math.pow

data class WindGate(
    var x: Float,
    val gapCenter: Float,
    var passed: Boolean = false,
)

class GameEngine(
    val width: Float,
    val height: Float,
    val mode: GameMode,
    seed: Int = 42,
) {
    private val random = Random(seed)
    private val playerX = width * .24f
    private val playerRadius = minOf(width, height) * .045f
    private val gateWidth = width * .14f
    private val gapSize = height * if (mode == GameMode.CALM) .42f else .32f
    private val baseSpeed = width * if (mode == GameMode.CALM) .17f else .25f
    private val gravity = height * if (mode == GameMode.CALM) .55f else .85f
    private val jumpVelocity = -height * if (mode == GameMode.CALM) .30f else .38f

    var playerY: Float = height * .48f
        private set
    var velocityY: Float = 0f
        private set
    var score: Int = 0
        private set
    var elapsedSeconds: Float = 0f
        private set
    val gates: MutableList<WindGate> = mutableListOf(newGate(width * .95f))

    fun flap() {
        velocityY = jumpVelocity
    }

    /** Returns false only when classic mode has ended. */
    fun update(deltaSeconds: Float): Boolean {
        val dt = deltaSeconds.coerceIn(0f, .05f)
        elapsedSeconds += dt
        velocityY += gravity * dt
        playerY += velocityY * dt

        gates.forEach { gate ->
            gate.x -= baseSpeed * speedMultiplier() * dt
            if (!gate.passed && gate.x + gateWidth < playerX) {
                gate.passed = true
                score++
            }
        }
        if (gates.last().x < width * .55f) gates += newGate(width * 1.05f)
        gates.removeAll { it.x + gateWidth < 0f }

        val hitBoundary = playerY - playerRadius < 0f || playerY + playerRadius > height
        val hitGate = gates.any { gate ->
            val overlapsX = playerX + playerRadius > gate.x && playerX - playerRadius < gate.x + gateWidth
            val gapTop = gate.gapCenter - gapSize / 2f
            val gapBottom = gate.gapCenter + gapSize / 2f
            overlapsX && (playerY - playerRadius < gapTop || playerY + playerRadius > gapBottom)
        }
        if (hitBoundary || hitGate) {
            if (mode == GameMode.CLASSIC) return false
            playerY = height * .5f
            velocityY = 0f
        }
        return true
    }

    fun playerX(): Float = playerX
    fun playerRadius(): Float = playerRadius
    fun gateWidth(): Float = gateWidth
    fun gapSize(): Float = gapSize
    fun speedMultiplier(): Float = if (mode == GameMode.CALM) 1f else {
        classicSpeedMultiplier(score, elapsedSeconds)
    }

    private fun newGate(x: Float): WindGate {
        val margin = gapSize / 2f + height * .08f
        val center = margin + random.nextFloat() * (height - margin * 2f)
        return WindGate(x, center)
    }
}

internal fun classicSpeedMultiplier(score: Int, elapsedSeconds: Float): Float {
    val scoreGrowth = .055f * score.coerceAtLeast(0).toFloat().pow(1.35f)
    val timeGrowth = .0025f * elapsedSeconds.coerceAtLeast(0f).pow(1.15f)
    return (1f + scoreGrowth + timeGrowth).coerceAtMost(3f)
}
