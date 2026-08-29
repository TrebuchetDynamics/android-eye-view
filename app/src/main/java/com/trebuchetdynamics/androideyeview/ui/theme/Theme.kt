package com.trebuchetdynamics.androideyeview.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val M0Colors = darkColorScheme(
    primary = Color(0xFF7CFFB2),
    onPrimary = Color(0xFF002111),
    secondary = Color(0xFF80D8FF),
    background = Color(0xFF050A0D),
    surface = Color(0xE6121B20),
    onSurface = Color(0xFFE2F1EA),
    error = Color(0xFFFF8A80),
)

@Composable
fun AndroidEyeViewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = M0Colors,
        content = content,
    )
}
