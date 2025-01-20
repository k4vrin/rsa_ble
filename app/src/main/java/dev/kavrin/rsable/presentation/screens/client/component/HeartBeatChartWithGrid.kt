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
    val pulseRadius = 10.dp
    val chartSize = remember { mutableStateOf(Size.Zero) }
    val pathProgress = remember { Animatable(0f) } // For animating path drawing
    val pulseAnimationState = animateFloatAsState(
        targetValue = if (pathProgress.value >= 1f) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(data) {
        pathProgress.snapTo(0f) // Reset animation progress
        pathProgress.animateTo(
            1f,
        ) // Animate to full progress
    }

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        chartSize.value = size

        val maxHeartRate = 200f
        val minHeartRate = 0f
        val verticalStep = (maxHeartRate - minHeartRate) / 10 // Divide chart into segments

        // Draw horizontal lines and labels for heart rate values
        for (i in 0..10) {
            val heartRateValue = maxHeartRate - (i * verticalStep)
            val y = calculateY(heartRateValue, chartHeight, maxHeartRate, minHeartRate)

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
            val x = calculateX(i, data.size, chartWidth)

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
        val animatedDataPoints = (data.size * pathProgress.value).toInt()

        for (i in 0 until animatedDataPoints) {
            val x = calculateX(i, data.size, chartWidth)
            val y = calculateY(data[i], chartHeight, maxHeartRate, minHeartRate)
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
            val x = calculateX(i, data.size, chartWidth)
            val y = calculateY(data[i], chartHeight, maxHeartRate, minHeartRate)

            // Pulsing effect for circles
            val animatedRadius = if (data[i] > 0) {

                pulseAnimationState.value * pulseRadius.toPx()
            } else 0f

            drawCircle(
                color = Color.Red,
                radius = animatedRadius,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Calculates the X-coordinate for a data point in a chart based on its index.
 *
 * This function determines the horizontal position (X-coordinate) of a data point within a chart,
 * given its index, the total number of data points, and the chart's width.
 *
 * The first data point (index 0) is positioned at a fixed offset (100f).
 * Subsequent data points are evenly distributed across the chart width.
 *
 * @param index The index of the data point (starting from 0).
 * @param dataSize The total number of data points in the chart.
 * @param chartWidth The width of the chart area.
 * @return The calculated X-coordinate for the data point.
 */
fun calculateX(index: Int, dataSize: Int, chartWidth: Float): Float {
    if (index == 0) return 100f
    return index * (chartWidth / dataSize)
}

/**
 * Calculates the Y-coordinate for a given heart rate value on a chart.
 *
 * This function takes a heart rate value and scales it to fit within the
 * vertical bounds of a chart. It normalizes the heart rate value based on
 * the minimum and maximum heart rate values, and then calculates the Y-coordinate
 * relative to the chart's height.
 *
 * @param value The heart rate value to be plotted on the chart.
 * @param chartHeight The total height of the chart in pixels.
 * @param maxHeartRate The maximum heart rate value expected.
 * @param minHeartRate The minimum heart rate value expected.
 * @return The calculated Y-coordinate for the given heart rate value.
 *
 * @throws IllegalArgumentException if maxHeartRate is less than or equal to minHeartRate
 *
 * @sample
 * ```kotlin
 * val yCoordinate = calculateY2(80.0f, 500.0f, 180.0f, 60.0f)
 * println("Y-coordinate: $yCoordinate")
 * ```
 */
fun calculateY(value: Float, chartHeight: Float, maxHeartRate: Float, minHeartRate: Float): Float {
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
