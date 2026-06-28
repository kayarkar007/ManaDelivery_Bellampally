package com.example.manadeliverybellempally.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ManaRed,
    secondary = ManaGold,
    tertiary = ManaGoldDim,
    background = ManaBgPrimary,
    surface = ManaBgSecondary,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = ManaTextPrimary,
    onSurface = ManaTextPrimary,
)

@Composable
fun ManaDeliveryBellempallyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force Dark Theme for a premium aesthetic
    val colorScheme = DarkColorScheme 

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ManaTypography,
        content = content
    )
}
