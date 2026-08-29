package dev.catandbunny.cloudbuddy.feature.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.GameMode
import kotlinx.coroutines.isActive

private enum class GamePhase { READY, PLAYING, PAUSED, GAME_OVER }

@Composable
fun GameScreen(
    mode: GameMode,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var resetKey by remember { mutableIntStateOf(0) }
    var lastRecordedRun by remember { mutableIntStateOf(-1) }
    var phase by remember { mutableStateOf(GamePhase.READY) }
    val engine = remember(canvasSize, mode, resetKey) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            GameEngine(canvasSize.width.toFloat(), canvasSize.height.toFloat(), mode, seed = resetKey + 42)
        } else null
    }
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(engine, phase) {
        if (engine == null || phase != GamePhase.PLAYING) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (isActive && phase == GamePhase.PLAYING) {
            val now = withFrameNanos { it }
            val alive = engine.update((now - previous) / 1_000_000_000f)
            previous = now
            frame++
            if (!alive) {
                phase = GamePhase.GAME_OVER
                if (lastRecordedRun != resetKey) {
                    onFinished(engine.score)
                    lastRecordedRun = resetKey
                }
            }
        }
    }

    val finishAndBack = {
        if (lastRecordedRun != resetKey) {
            engine?.let { onFinished(it.score) }
        }
        onBack()
    }
    val backgroundDrift = (frame % 600) / 600f
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF9ED8F5), Color(0xFFF1E7FF))),
        ),
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize().onSizeChanged { canvasSize = it }.pointerInput(engine, phase) {
                detectTapGestures {
                    when (phase) {
                        GamePhase.READY -> {
                            phase = GamePhase.PLAYING
                            engine?.flap()
                        }
                        GamePhase.PLAYING -> engine?.flap()
                        else -> Unit
                    }
                }
            },
        ) {
            val current = engine ?: return@Canvas
            repeat(7) { index ->
                drawCircle(
                    color = Color.White.copy(alpha = .24f),
                    radius = size.width * (.05f + index % 3 * .012f),
                    center = Offset(size.width * ((index * .19f + backgroundDrift * .08f) % 1f), size.height * (.14f + (index % 4) * .21f)),
                )
            }
            current.gates.forEach { gate ->
                val gapTop = gate.gapCenter - current.gapSize() / 2f
                val gapBottom = gate.gapCenter + current.gapSize() / 2f
                drawRoundRect(
                    color = Color(0xFF6676A8).copy(alpha = .72f),
                    topLeft = Offset(gate.x, 0f),
                    size = Size(current.gateWidth(), gapTop),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f),
                )
                drawRoundRect(
                    color = Color(0xFF6676A8).copy(alpha = .72f),
                    topLeft = Offset(gate.x, gapBottom),
                    size = Size(current.gateWidth(), size.height - gapBottom),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f),
                )
            }
            val cloudCenter = Offset(current.playerX(), current.playerY)
            drawCircle(Color.White, current.playerRadius(), cloudCenter)
            drawCircle(Color.White, current.playerRadius() * .72f, cloudCenter + Offset(-current.playerRadius() * .72f, current.playerRadius() * .18f))
            drawCircle(Color.White, current.playerRadius() * .65f, cloudCenter + Offset(current.playerRadius() * .72f, current.playerRadius() * .2f))
            drawCircle(Color(0xFF33415C), current.playerRadius() * .09f, cloudCenter + Offset(-current.playerRadius() * .25f, current.playerRadius() * .08f))
            drawCircle(Color(0xFF33415C), current.playerRadius() * .09f, cloudCenter + Offset(current.playerRadius() * .25f, current.playerRadius() * .08f))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(onClick = finishAndBack) { Text("← Домой") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${engine?.score ?: 0}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (mode == GameMode.CLASSIC) {
                    Text(
                        "скорость ×${"%.1f".format(engine?.speedMultiplier() ?: 1f)}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            FilledTonalButton(
                onClick = {
                    phase = if (phase == GamePhase.PAUSED) GamePhase.PLAYING else GamePhase.PAUSED
                },
                enabled = phase == GamePhase.PLAYING || phase == GamePhase.PAUSED,
            ) { Text(if (phase == GamePhase.PAUSED) "▶" else "Ⅱ") }
        }
        if (phase != GamePhase.PLAYING) {
            Card(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(.84f),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        when (phase) {
                            GamePhase.READY -> mode.title
                            GamePhase.PAUSED -> "Небо подождёт"
                            GamePhase.GAME_OVER -> "Мягкая посадка"
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        when (phase) {
                            GamePhase.READY -> mode.description + "\nНажимай на экран, чтобы подняться."
                            GamePhase.PAUSED -> "Продолжай, когда будешь готов."
                            GamePhase.GAME_OVER -> "Результат: ${engine?.score ?: 0}. Каждая попытка — часть путешествия."
                            else -> ""
                        },
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = {
                            when (phase) {
                                GamePhase.READY, GamePhase.PAUSED -> phase = GamePhase.PLAYING
                                GamePhase.GAME_OVER -> {
                                    resetKey++
                                    phase = GamePhase.READY
                                }
                                else -> Unit
                            }
                        },
                    ) { Text(if (phase == GamePhase.GAME_OVER) "Ещё раз" else "Начать") }
                }
            }
        }
        if (mode == GameMode.CALM) {
            Text(
                "Здесь нельзя проиграть · дыши в своём темпе",
                modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
