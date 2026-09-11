package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LaboratoryDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001B24),
    primaryContainer = Color(0xFF003648),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricMagenta,
    onSecondary = Color(0xFF260032),
    secondaryContainer = Color(0xFF4A005F),
    onSecondaryContainer = ElectricMagenta,
    tertiary = ScientificAmber,
    onTertiary = Color(0xFF2E2200),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = SafetyRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Scientific simulation defaults to lab dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LaboratoryDarkColorScheme,
        typography = Typography,
        content = content
    )
}
