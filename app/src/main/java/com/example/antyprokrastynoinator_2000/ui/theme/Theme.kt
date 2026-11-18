package com.example.antyprokrastynoinator_2000.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IceBlue,
    onPrimary = NavyPrimary,
    secondary = IceBlueLight,
    onSecondary = NavyPrimary,
    background = NavyPrimary,
    onBackground = IceBlue,
    surface = NavyContainer,
    onSurface = IceBlue
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = IceBlue,
    secondary = NavyContainer,
    onSecondary = IceBlue,
    background = IceBlue,
    onBackground = NavyPrimary,
    surface = IceBlueLight,
    onSurface = NavyPrimary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun Antyprokrastynoinator_2000Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}