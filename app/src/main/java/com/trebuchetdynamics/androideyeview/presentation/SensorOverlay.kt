package com.trebuchetdynamics.androideyeview.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Transparent native approximations layered above the map without consuming pointer input.
 * These effects do not inspect or transform map pixels and are never sensor measurements.
 */
@Composable
fun SensorOverlay(
    mode: SensorMode,
    modifier: Modifier = Modifier,
) {
    if (mode == SensorMode.NORMAL) return

    val attributionSafeBottomPx = with(LocalDensity.current) { 64.dp.toPx() }
    Canvas(modifier = modifier.fillMaxSize()) {
        clipRect(bottom = (size.height - attributionSafeBottomPx).coerceAtLeast(0f)) {
            when (mode) {
                SensorMode.NORMAL -> Unit
                SensorMode.CRT -> {
                    drawRect(Color(0x2200FF66))
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0x33000000),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                        )
                        y += 5f
                    }
                }
                SensorMode.NVG -> drawRect(Color(0x5520FF55))
                SensorMode.MONOCHROME -> drawRect(Color(0x443A4650))
                SensorMode.SNOW -> {
                    drawRect(Color(0x22000000))
                    val columns = 48
                    val rows = 80
                    repeat(columns * rows) { index ->
                        val column = index % columns
                        val row = index / columns
                        val hash = (index * 1103515245L + 12345L) and 0x7fffffff
                        if (hash % 5L == 0L) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.22f),
                                radius = 1.2f,
                                center = Offset(
                                    x = (column + 0.5f) * size.width / columns,
                                    y = (row + 0.5f) * size.height / rows,
                                ),
                            )
                        }
                    }
                }
                SensorMode.THERMAL_INSPIRED -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x332A0A5E),
                                Color(0x22D7263D),
                                Color(0x22FFB000),
                                Color(0x222A0A5E),
                            ),
                        ),
                    )
                }
            }
        }
    }
}
