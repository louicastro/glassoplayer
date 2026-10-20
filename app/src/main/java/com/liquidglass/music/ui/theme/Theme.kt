package com.liquidglass.music.ui.theme

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalReducedMotion = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = LiquidAccentPrimary,
    secondary = LiquidAccentSecondary,
    background = Color(0xFF0A0B10),
    surface = GlassDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LiquidAccentPrimary,
    secondary = LiquidAccentSecondary,
    background = Color(0xFFF7F8FC),
    surface = GlassLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary
)

@Composable
fun LiquidMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isReducedMotion = try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        scale == 0f
    } catch (e: Exception) {
        false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalReducedMotion provides isReducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
