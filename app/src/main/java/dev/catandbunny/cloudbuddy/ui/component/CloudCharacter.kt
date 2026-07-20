package dev.catandbunny.cloudbuddy.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.CloudWeather
import kotlin.math.sin

@Composable
fun CloudCharacter(
    weather: CloudWeather,
    modifier: Modifier = Modifier,
    secretLevel: Int = 0,
) {
    val transition = rememberInfiniteTransition(label = "cloudFloat")
    val float by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1_800), RepeatMode.Reverse),
        label = "float",
    )
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f + float)
        drawAtmosphere(weather, center)
        val cloudColor = when (weather) {
            CloudWeather.SUNNY -> Color(0xFFFFFBF1)
            CloudWeather.SOFT -> Color.White
            CloudWeather.FOGGY -> Color(0xFFE6E9EF)
            CloudWeather.RAINY -> Color(0xFFDCE5F2)
            CloudWeather.STORMY -> Color(0xFFBAC6DA)
        }
        drawCircle(cloudColor, size.width * .19f, center + Offset(-size.width * .18f, 0f))
        drawCircle(cloudColor, size.width * .24f, center + Offset(0f, -size.height * .07f))
        drawCircle(cloudColor, size.width * .17f, center + Offset(size.width * .2f, size.height * .01f))
        drawOval(
            color = cloudColor,
            topLeft = center + Offset(-size.width * .32f, -size.height * .03f),
            size = Size(size.width * .64f, size.height * .27f),
        )
        val face = Color(0xFF34435D)
        drawCircle(face, size.width * .018f, center + Offset(-size.width * .08f, size.height * .03f))
        drawCircle(face, size.width * .018f, center + Offset(size.width * .08f, size.height * .03f))
        val smile = Path().apply {
            moveTo(center.x - size.width * .045f, center.y + size.height * .09f)
            quadraticBezierTo(
                center.x,
                center.y + size.height * .14f,
                center.x + size.width * .045f,
                center.y + size.height * .09f,
            )
        }
        drawPath(smile, face, style = androidx.compose.ui.graphics.drawscope.Stroke(size.width * .01f))
        if (secretLevel >= 3) {
            drawCircle(Color(0xFFFFD166), size.width * .025f, center + Offset(size.width * .28f, -size.height * .2f))
        }
    }
}

private fun DrawScope.drawAtmosphere(weather: CloudWeather, center: Offset) {
    if (weather == CloudWeather.SUNNY) {
        drawCircle(Color(0xFFFFD166), size.width * .14f, center + Offset(size.width * .27f, -size.height * .27f))
    }
    if (weather == CloudWeather.RAINY || weather == CloudWeather.STORMY) {
        repeat(if (weather == CloudWeather.STORMY) 5 else 3) { index ->
            val x = center.x - size.width * .2f + index * size.width * .1f
            drawLine(
                Color(0xFF69A7D8),
                Offset(x, center.y + size.height * .2f),
                Offset(x - 8f, center.y + size.height * .28f),
                strokeWidth = 5f,
            )
        }
    }
    if (weather == CloudWeather.FOGGY) {
        repeat(3) { index ->
            drawLine(
                Color.White.copy(alpha = .55f),
                Offset(size.width * .12f, center.y + index * 22f - 12f),
                Offset(size.width * .88f, center.y + index * 22f - 12f),
                strokeWidth = 8f,
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            CloudCharacter(CloudWeather.SOFT, Modifier.size(180.dp))
            CircularProgressIndicator()
            Text("Собираем облака…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
