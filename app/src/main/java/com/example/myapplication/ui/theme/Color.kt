package com.example.myapplication.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
data class GradilyColors(
    val darkGreen: Color,
    val mediumGreen: Color,
    val lightGreen: Color,
    val accentGreen: Color,
    val surfaceGreen: Color,
    val glassBg: Color,
    val glassBgDark: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentBlue: Color,
    val blueGPA: Color,
    val accentAmber: Color,
    val accentRed: Color,
    val accentPurple: Color,
    val blackBackground: Color,
    val darkBackground: Color,
    val surface: Color
)

val DarkGradilyColors = GradilyColors(
    darkGreen = Color(0xFF0A2E14),
    mediumGreen = Color(0xFF14532D),
    lightGreen = Color(0xFF22C55E),
    accentGreen = Color(0xFF4ADE80),
    surfaceGreen = Color(0xFF166534),
    glassBg = Color(0x33FFFFFF),
    glassBgDark = Color(0x1AFFFFFF),
    glassBorder = Color(0x33FFFFFF),
    textPrimary = Color.White,
    textSecondary = Color(0xB3FFFFFF),
    textMuted = Color(0x80FFFFFF),
    accentBlue = Color(0xFF38BDF8),
    blueGPA = Color(0xFF38BDF8),
    accentAmber = Color(0xFFFBBF24),
    accentRed = Color(0xFFEF4444),
    accentPurple = Color(0xFFA78BFA),
    blackBackground = Color(0xFF000000),
    darkBackground = Color(0xFF0A0F0D),
    surface = Color(0xFF15191C)
)

val LightGradilyColors = GradilyColors(
    darkGreen = Color(0xFFF0FDF4),
    mediumGreen = Color(0xFFDCFCE7),
    lightGreen = Color(0xFF22C55E),
    accentGreen = Color(0xFF16A34A),
    surfaceGreen = Color(0xFFBBF7D0),
    glassBg = Color(0x40FFFFFF),
    glassBgDark = Color(0x99FFFFFF),
    glassBorder = Color(0x4D000000),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    accentBlue = Color(0xFF0284C7),
    blueGPA = Color(0xFF0284C7),
    accentAmber = Color(0xFFD97706),
    accentRed = Color(0xFFDC2626),
    accentPurple = Color(0xFF7C3AED),
    blackBackground = Color(0xFFFFFFFF),
    darkBackground = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF)
)

val LocalGradilyColors = compositionLocalOf { DarkGradilyColors }

object GradilyTheme {
    val colors: GradilyColors
        @androidx.compose.runtime.Composable
        get() = LocalGradilyColors.current
}