package com.amirnourhan.goldbar.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFF2C45B)
val Dark = Color(0xFF090B12)
val Panel = Color(0xFF151925)
val Ink = Color(0xFFEFE7D0)

@Composable fun GoldBarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Dark, surface = Panel, onSurface = Ink, onPrimary = Color(0xFF211600)), typography = Typography(), content = content)
}
