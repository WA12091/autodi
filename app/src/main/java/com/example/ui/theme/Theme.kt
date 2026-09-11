package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ExecutiveDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = ElectricCyan,
    secondary = RoyalBlue,
    onSecondary = PureWhite,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate200,
    tertiary = EmeraldSuccess,
    onTertiary = Slate950,
    tertiaryContainer = EmeraldDark,
    onTertiaryContainer = EmeraldSuccess,
    background = Slate950,
    onBackground = Slate200,
    surface = Slate900,
    onSurface = Slate200,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate800,
    error = RoseDanger,
    onError = PureWhite
)

private val ExecutiveLightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = CyanGlow,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = EmeraldSuccess,
    onTertiary = PureWhite,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = PureWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    error = RoseDanger,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to executive dark theme for high-tech dialer look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ExecutiveDarkColorScheme else ExecutiveLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

