import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.random.Random

@RequiresApi(35)
@Composable
fun GameScreen() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val screenWidth = with(density) { (screenWidthDp * this.density).toInt() }
    val screenHeight = with(density) { (screenHeightDp * this.density).toInt() }

    var lumyY by remember { mutableFloatStateOf(screenHeight / 2f) } // Начальная позиция по центру
    var lumyVelocity by remember { mutableFloatStateOf(0f) }
    val gravity = 0.5f
    val jumpStrength = -15f
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    val clouds = remember { mutableStateListOf<Cloud>() }

    // Игровой цикл
    LaunchedEffect(Unit) {
        clouds.add(Cloud(screenWidth.toFloat(), Random.nextFloat() * (screenHeight - 400f) + 200f))
        while (!isGameOver) {
            lumyVelocity += gravity
            lumyY += lumyVelocity

            if (lumyY < 0 || lumyY > screenHeight) {
                isGameOver = true
            }

            clouds.forEach { it.x -= 5f }
            if ((clouds.firstOrNull()?.x?.plus(100f) ?: 0f) < 0) {
                clouds.removeFirst()
                clouds.add(Cloud(screenWidth.toFloat(), Random.nextFloat() * (screenHeight - 400f) + 200f))
                score++
            }

            clouds.forEach { cloud ->
                if (100f in cloud.x..(cloud.x + 100f) &&
                    (lumyY < cloud.gapY - 200f || lumyY > cloud.gapY + 200f)) {
                    isGameOver = true
                }
            }

            delay(16)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { if (!isGameOver) lumyVelocity = jumpStrength }
            }
    ) {
        // Фон
        drawRect(color = Color.Cyan)

        // Lumy
        drawCircle(color = Color.White, radius = 40f, center = Offset(100f, lumyY))

        // Тучи
        clouds.forEach { cloud ->
            drawRect(
                color = Color.Gray,
                topLeft = Offset(cloud.x, 0f),
                size = androidx.compose.ui.geometry.Size(100f, cloud.gapY - 200f)
            )
            drawRect(
                color = Color.Gray,
                topLeft = Offset(cloud.x, cloud.gapY + 200f),
                size = androidx.compose.ui.geometry.Size(100f, screenHeight.toFloat() - cloud.gapY - 200f)
            )
        }

        // Счет
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = Color.Black
//                textSize = 50f
            }
            canvas.nativeCanvas.drawText("Score: $score", 50f, 100f, paint.asFrameworkPaint())
        }

        // Конец игры
        if (isGameOver) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color.Red
//                    textSize = 100f
                }
                canvas.nativeCanvas.drawText("Game Over", screenWidth / 4f, screenHeight / 2f, paint.asFrameworkPaint())
            }
        }
    }
}

data class Cloud(var x: Float, val gapY: Float)