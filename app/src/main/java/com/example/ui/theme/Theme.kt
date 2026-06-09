package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = WaterSecondary,
    secondary = WaterTertiary,
    tertiary = DarkWaterTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkWaterSecondary,
    onSecondary = DarkWaterSecondary,
    onSurface = Color(0xFFE2E2E6),
    onBackground = Color(0xFFE2E2E6),
    outline = Color(0xFF3F484B)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WaterPrimary,
    secondary = WaterSecondary,
    tertiary = WaterTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BentoTextDark,
    onSurface = BentoTextDark,
    outline = BentoBorderColor
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color so user theme isn't overridden
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
