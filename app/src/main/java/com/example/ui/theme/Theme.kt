package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LoveRose,
    secondary = RoastFlame,
    tertiary = AccentGold,
    background = DeepSpaceBg,
    surface = SigmaBlack,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextWhitePrimary,
    onSurface = TextWhitePrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LoveRoseLight,
    secondary = RoastFlameLight,
    tertiary = AccentGold,
    background = CleanRoseBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
