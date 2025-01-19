package dev.kavrin.rsable.presentation.screens.client.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.RSA_BLETheme

@Composable
fun HeartRateChartWithGrid(data: List<Float>, modifier: Modifier = Modifier) {
    val path = Path()
    val pulseSize = 10.dp
    val chartSize = remember { mutableStateOf(Size.Zero) }
    val animatedProgress = remember { Animatable(0f) } // For animating path drawing
    val pulseAnimation = animateFloatAsState(
        targetValue = if (animatedProgress.value >= 1f) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f) // Reset animation progress
        animatedProgress.animateTo(
            1f,
        ) // Animate to full progress
    }

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        chartSize.value = size

        val maxHeartRate = 200f // Replace with your actual maximum heart rate value
        val minHeartRate = 0f // Replace with your actual minimum heart rate value
        val verticalStep = (maxHeartRate - minHeartRate) / 10 // Divide chart into 5 segments

        // Draw horizontal lines and labels for heart rate values
        for (i in 0..10) {
            val heartRateValue = maxHeartRate - (i * verticalStep)
            val y = calculateY2(heartRateValue, chartHeight, maxHeartRate, minHeartRate)

            drawLine(
                color = Color.Gray,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                alpha = 0.5f
            )

            // Draw heart rate value labels
            drawContext.canvas.nativeCanvas.drawText(
                heartRateValue.toInt().toString(),
                10f,
                y - 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 32f
                }
            )
        }

        // Draw vertical lines for time
        for (i in data.indices) {
            val x = calculateX2(i, data.size, chartWidth)

            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, chartHeight),
                strokeWidth = 1.dp.toPx(),
                alpha = 0.3f
            )
        }

        // Create the wave path based on animated progress
        path.moveTo(0f, chartHeight)
        val animatedDataPoints = (data.size * animatedProgress.value).toInt()

        for (i in 0 until animatedDataPoints) {
            val x = calculateX2(i, data.size, chartWidth)
            val y = calculateY2(data[i], chartHeight, maxHeartRate, minHeartRate)
            path.lineTo(x, y)
        }

        // Draw the animated path
        drawPath(
            path = path,
            color = Color.Red,
            style = Stroke(width = 4.dp.toPx())
        )

        // Highlight peaks with animated pulsing effect
        for (i in 0 until animatedDataPoints) {
            val x = calculateX2(i, data.size, chartWidth)
            val y = calculateY2(data[i], chartHeight, maxHeartRate, minHeartRate)

            // Pulsing effect for circles
            val animatedRadius = if (data[i] > 0) {

                pulseAnimation.value * pulseSize.toPx()
            } else 0f

            drawCircle(
                color = Color.Red,
                radius = animatedRadius,
                center = Offset(x, y)
            )
        }
    }
}

fun calculateX2(index: Int, dataSize: Int, chartWidth: Float): Float {
    if (index == 0) return 100f
    return index * (chartWidth / dataSize)
}

fun calculateY2(value: Float, chartHeight: Float, maxHeartRate: Float, minHeartRate: Float): Float {
    val normalizedValue = (value - minHeartRate) / (maxHeartRate - minHeartRate)
    return chartHeight * (1 - normalizedValue)
}


@Preview
@Composable
private fun HeartBeatPrev() {
    RSA_BLETheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGreen)
        ) {
            HeartRateChartWithGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                data = listOf(0f, 02f, 44f, 50f, 55f, 48f, 56f)
            )
        }
    }
}
